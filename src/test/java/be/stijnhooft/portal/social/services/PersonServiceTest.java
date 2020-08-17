package be.stijnhooft.portal.social.services;

import be.stijnhooft.portal.social.dtos.*;
import be.stijnhooft.portal.social.mappers.PersonMapper;
import be.stijnhooft.portal.social.mappers.RecurringTaskDtoMapper;
import be.stijnhooft.portal.social.model.Person;
import be.stijnhooft.portal.social.repositories.PersonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @InjectMocks
    private PersonService personService;

    @Mock
    private RecurringTasksService recurringTasksService;

    @Mock
    private ImageService imageService;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private PersonMapper personMapper;

    @Mock
    private RecurringTaskDtoMapper recurringTaskDtoMapper;

    @Mock
    private SavePersonHelper savePersonHelper;

    @Test
    void findAll() {
        // arrange
        var person1 = Person.builder()
                .id(100L)
                .name("Slim Shady")
                .recurringTaskId(888L)
                .build();

        var person2 = Person.builder()
                .id(200L)
                .name("Jenny from the block")
                .recurringTaskId(999L)
                .build();

        var lastContact1 = LocalDate.now();
        var lastContact2 = LocalDate.now().minusDays(1);

        var recurringTaskForPerson1 = RecurringTaskDto.builder()
                .id(888L)
                .minNumberOfDaysBetweenExecutions(1)
                .maxNumberOfDaysBetweenExecutions(2)
                .lastExecution(lastContact1)
                .build();

        var recurringTaskForPerson2 = RecurringTaskDto.builder()
                .id(999L)
                .minNumberOfDaysBetweenExecutions(3)
                .maxNumberOfDaysBetweenExecutions(4)
                .lastExecution(lastContact2)
                .build();

        var personDto1 = PersonDto.builder()
                .id(100L)
                .name("Slim Shady")
                .minNumberOfDaysBetweenContacts(1)
                .maxNumberOfDaysBetweenContacts(2)
                .lastContact(lastContact1)
                .build();

        var personDto2 = PersonDto.builder()
                .id(200L)
                .name("Jenny from the block")
                .minNumberOfDaysBetweenContacts(3)
                .maxNumberOfDaysBetweenContacts(4)
                .lastContact(lastContact2)
                .build();

        when(personRepository.findAll()).thenReturn(List.of(person1, person2));
        when(recurringTasksService.findById(888L)).thenReturn(Optional.of(recurringTaskForPerson1));
        when(personMapper.mapToDto(person1, recurringTaskForPerson1)).thenReturn(personDto1);
        when(recurringTasksService.findById(999L)).thenReturn(Optional.of(recurringTaskForPerson2));
        when(personMapper.mapToDto(person2, recurringTaskForPerson2)).thenReturn(personDto2);

        // act
        var result = personService.findAll();

        // assert
        assertEquals(2, result.size());
        assertEquals(personDto1, result.get(0));
        assertEquals(personDto2, result.get(1));
    }

    @Test
    void findByIdWhenFound() {
        // arrange
        var person = Person.builder()
                .id(100L)
                .name("Slim Shady")
                .recurringTaskId(888L)
                .build();

        var lastContact1 = LocalDate.now();
        var recurringTaskDto = RecurringTaskDto.builder()
                .id(888L)
                .minNumberOfDaysBetweenExecutions(1)
                .maxNumberOfDaysBetweenExecutions(2)
                .lastExecution(lastContact1)
                .build();

        var personDto = PersonDto.builder()
                .id(100L)
                .name("Slim Shady")
                .minNumberOfDaysBetweenContacts(1)
                .maxNumberOfDaysBetweenContacts(2)
                .lastContact(lastContact1)
                .build();

        when(personRepository.findById(100L)).thenReturn(Optional.of(person));
        when(recurringTasksService.findById(888L)).thenReturn(Optional.of(recurringTaskDto));
        when(personMapper.mapToDto(person, recurringTaskDto)).thenReturn(personDto);

        // act
        var result = personService.findById(100);

        // assert
        assertTrue(result.isPresent());
        assertEquals(personDto, result.get());
    }

    @Test
    void findByIdWhenNotFound() {
        // arrange
        when(personRepository.findById(101L)).thenReturn(Optional.empty());

        // act
        var result = personService.findById(101);

        // assert
        assertFalse(result.isPresent());

        verify(personRepository).findById(101L);
        verifyNoMoreInteractions(personRepository, personMapper, imageService, recurringTasksService, recurringTaskDtoMapper);
    }

    @Test
    void createWhenSuccess() {
        // arrange
        var personDto = PersonDto.builder()
                .name("Stijn")
                .newImageContent("new-data")
                .minNumberOfDaysBetweenContacts(10)
                .maxNumberOfDaysBetweenContacts(20)
                .build();

        var colorThumbnail = "colorThumbnail.jpg";
        var sepiaThumbnail = "sepiaThumbnail.jpg";
        HashMap<ImageLabel, String> thumbnails = new HashMap<>();
        thumbnails.put(ImageLabel.COLOR_THUMBNAIL, colorThumbnail);
        thumbnails.put(ImageLabel.SEPIA_THUMBNAIL, sepiaThumbnail);

        var recurringTaskId = 999L;

        var recurringTaskDto = RecurringTaskDto.builder()
                .name("Stijn")
                .minNumberOfDaysBetweenExecutions(10)
                .maxNumberOfDaysBetweenExecutions(20)
                .build();

        var expectedPersonToBeCreated = Person.builder()
                .name(personDto.getName())
                .colorThumbnail(colorThumbnail)
                .sepiaThumbnail(sepiaThumbnail)
                .recurringTaskId(recurringTaskId)
                .build();

        var createdPersonDto = PersonDto.builder()
                .name("Stijn")
                .colorThumbnail(colorThumbnail)
                .sepiaThumbnail(sepiaThumbnail)
                .minNumberOfDaysBetweenContacts(10)
                .maxNumberOfDaysBetweenContacts(20)
                .build();

        var createdRecurringTaskDto = RecurringTaskDto.builder()
                .id(recurringTaskId)
                .name("Stijn")
                .minNumberOfDaysBetweenExecutions(10)
                .maxNumberOfDaysBetweenExecutions(20)
                .build();

        when(personRepository.findByName("Stijn")).thenReturn(Optional.empty());
        when(imageService.createThumbnails("new-data")).thenReturn(thumbnails);
        when(recurringTaskDtoMapper.map(personDto)).thenReturn(recurringTaskDto);
        when(recurringTasksService.create(recurringTaskDto)).thenReturn(createdRecurringTaskDto);
        when(savePersonHelper.saveAndFlushAndCommit(expectedPersonToBeCreated)).thenReturn(expectedPersonToBeCreated);
        when(personMapper.mapToDto(expectedPersonToBeCreated, createdRecurringTaskDto)).thenReturn(createdPersonDto);

        // act
        var result = personService.create(personDto);

        // assert
        verify(personRepository).findByName("Stijn");
        verify(imageService).createThumbnails("new-data");
        verify(recurringTaskDtoMapper).map(personDto);
        verify(recurringTasksService).create(recurringTaskDto);
        verify(savePersonHelper).saveAndFlushAndCommit(expectedPersonToBeCreated);
        verify(personMapper).mapToDto(expectedPersonToBeCreated, createdRecurringTaskDto);
        verifyNoMoreInteractions(personRepository, personMapper, imageService, recurringTasksService, recurringTaskDtoMapper);

        assertEquals(createdPersonDto, result);
    }

    @Test
    void createWhenPersonNameAlreadyExists() {
        // arrange
        var personDto = PersonDto.builder()
                .name("Stijn")
                .newImageContent("new-data")
                .minNumberOfDaysBetweenContacts(10)
                .maxNumberOfDaysBetweenContacts(20)
                .build();

        var existingPersonWithSameName = Person.builder()
                .name("Stijn")
                .build();

        when(personRepository.findByName("Stijn")).thenReturn(Optional.of(existingPersonWithSameName));

        // act, assert
        assertThrows(RuntimeException.class, () -> personService.create(personDto), "Person with name Stijn already exists.");

        verify(personRepository).findByName("Stijn");
        verifyNoMoreInteractions(personRepository, personMapper, imageService, recurringTasksService, recurringTaskDtoMapper);
    }

    @Test
    void createWhenImageServiceGivesError() {
        // arrange
        var personDto = PersonDto.builder()
                .name("Stijn")
                .newImageContent("new-data")
                .minNumberOfDaysBetweenContacts(10)
                .maxNumberOfDaysBetweenContacts(20)
                .build();


        when(personRepository.findByName("Stijn")).thenReturn(Optional.empty());
        when(imageService.createThumbnails("new-data")).thenThrow(new RuntimeException());

        // act
        assertThrows(RuntimeException.class, () -> personService.create(personDto));

        // assert
        verify(personRepository).findByName("Stijn");
        verify(imageService).createThumbnails("new-data");
        verifyNoMoreInteractions(personRepository, personMapper, imageService, recurringTasksService, recurringTaskDtoMapper);
    }

    @Test
    void createWhenRecurringTasksServiceGivesError() {
        // arrange
        var personDto = PersonDto.builder()
                .name("Stijn")
                .newImageContent("new-data")
                .minNumberOfDaysBetweenContacts(10)
                .maxNumberOfDaysBetweenContacts(20)
                .build();

        var colorThumbnail = "colorThumbnail.jpg";
        var sepiaThumbnail = "sepiaThumbnail.jpg";
        HashMap<ImageLabel, String> thumbnails = new HashMap<>();
        thumbnails.put(ImageLabel.COLOR_THUMBNAIL, colorThumbnail);
        thumbnails.put(ImageLabel.SEPIA_THUMBNAIL, sepiaThumbnail);

        var recurringTaskDto = RecurringTaskDto.builder()
                .name("Stijn")
                .minNumberOfDaysBetweenExecutions(10)
                .maxNumberOfDaysBetweenExecutions(20)
                .build();

        when(personRepository.findByName("Stijn")).thenReturn(Optional.empty());
        when(imageService.createThumbnails("new-data")).thenReturn(thumbnails);
        when(recurringTaskDtoMapper.map(personDto)).thenReturn(recurringTaskDto);
        when(recurringTasksService.create(recurringTaskDto)).thenThrow(new RuntimeException());

        // act
        assertThrows(RuntimeException.class, () -> personService.create(personDto));

        // assert
        verify(personRepository).findByName("Stijn");
        verify(imageService).createThumbnails("new-data");
        verify(recurringTaskDtoMapper).map(personDto);
        verify(recurringTasksService).create(recurringTaskDto);
        verify(imageService).rollbackCreateImages(thumbnails.values()); // the most important check of this test!
        verifyNoMoreInteractions(personRepository, personMapper, imageService, recurringTasksService, recurringTaskDtoMapper);
    }

    @Test
    void createWhenSavingToDatabaseGivesError() {
        // arrange
        var personDto = PersonDto.builder()
                .name("Stijn")
                .newImageContent("new-data")
                .minNumberOfDaysBetweenContacts(10)
                .maxNumberOfDaysBetweenContacts(20)
                .build();

        var colorThumbnail = "colorThumbnail.jpg";
        var sepiaThumbnail = "sepiaThumbnail.jpg";
        HashMap<ImageLabel, String> thumbnails = new HashMap<>();
        thumbnails.put(ImageLabel.COLOR_THUMBNAIL, colorThumbnail);
        thumbnails.put(ImageLabel.SEPIA_THUMBNAIL, sepiaThumbnail);

        var recurringTaskId = 999L;

        var recurringTaskDto = RecurringTaskDto.builder()
                .name("Stijn")
                .minNumberOfDaysBetweenExecutions(10)
                .maxNumberOfDaysBetweenExecutions(20)
                .build();

        var expectedPersonToBeCreated = Person.builder()
                .name(personDto.getName())
                .colorThumbnail(colorThumbnail)
                .sepiaThumbnail(sepiaThumbnail)
                .recurringTaskId(recurringTaskId)
                .build();

        var createdRecurringTaskDto = RecurringTaskDto.builder()
                .id(recurringTaskId)
                .name("Stijn")
                .minNumberOfDaysBetweenExecutions(10)
                .maxNumberOfDaysBetweenExecutions(20)
                .build();

        when(personRepository.findByName("Stijn")).thenReturn(Optional.empty());
        when(imageService.createThumbnails("new-data")).thenReturn(thumbnails);
        when(recurringTaskDtoMapper.map(personDto)).thenReturn(recurringTaskDto);
        when(recurringTasksService.create(recurringTaskDto)).thenReturn(createdRecurringTaskDto);
        when(savePersonHelper.saveAndFlushAndCommit(expectedPersonToBeCreated)).thenThrow(new RuntimeException());

        // act
        assertThrows(RuntimeException.class, () -> personService.create(personDto));

        // assert
        verify(personRepository).findByName("Stijn");
        verify(imageService).createThumbnails("new-data");
        verify(recurringTaskDtoMapper).map(personDto);
        verify(recurringTasksService).create(recurringTaskDto);
        verify(savePersonHelper).saveAndFlushAndCommit(expectedPersonToBeCreated);
        verify(imageService).rollbackCreateImages(thumbnails.values()); // very important check of this test!
        verify(recurringTasksService).rollbackCreateRecurringTask(recurringTaskId); // very important check of this test!
        verifyNoMoreInteractions(personRepository, personMapper, imageService, recurringTasksService, recurringTaskDtoMapper);
    }

    @Test
    void updateWhenSuccessAndEverythingIsUpdated() {
        // arrange
        var originalColorThumbnail = "originalColorThumbnail.jpg";
        var originalSepiaThumbnail = "originalSepiaThumbnail.jpg";
        var updatedColorThumbnail = "updatedColorThumbnail.jpg";
        var updatedSepiaThumbnail = "updatedSepiaThumbnail.jpg";

        HashMap<ImageLabel, String> updatedThumbnails = new HashMap<>();
        updatedThumbnails.put(ImageLabel.COLOR_THUMBNAIL, updatedColorThumbnail);
        updatedThumbnails.put(ImageLabel.SEPIA_THUMBNAIL, updatedSepiaThumbnail);

        var personId = 1L;
        var recurringTaskId = 999L;

        var originalPerson = Person.builder()
                .id(personId)
                .name("Stijn")
                .colorThumbnail(originalColorThumbnail)
                .sepiaThumbnail(originalSepiaThumbnail)
                .recurringTaskId(recurringTaskId)
                .build();

        var originalRecurringTaskDto = RecurringTaskDto.builder()
                .id(recurringTaskId)
                .name("Stijn")
                .minNumberOfDaysBetweenExecutions(10)
                .maxNumberOfDaysBetweenExecutions(20)
                .build();

        var updatedPersonDto = PersonDto.builder()
                .id(personId)
                .name("Tim")
                .newImageContent("new-data")
                .minNumberOfDaysBetweenContacts(5)
                .maxNumberOfDaysBetweenContacts(6)
                .build();

        var updatedPerson = Person.builder()
                .id(personId)
                .name("Tim")
                .colorThumbnail(updatedColorThumbnail)
                .sepiaThumbnail(updatedSepiaThumbnail)
                .recurringTaskId(recurringTaskId)
                .build();

        var updatedRecurringTaskDtoWithoutId = RecurringTaskDto.builder()
                .name("Tim")
                .minNumberOfDaysBetweenExecutions(5)
                .maxNumberOfDaysBetweenExecutions(6)
                .build();

        var updatedRecurringTaskDtoWithId = updatedRecurringTaskDtoWithoutId
                .toBuilder()
                .id(recurringTaskId)
                .build();

        when(personRepository.findById(personId)).thenReturn(Optional.of(originalPerson));
        when(recurringTasksService.findById(recurringTaskId)).thenReturn(Optional.of(originalRecurringTaskDto));
        when(personMapper.mapToModel(updatedPersonDto, recurringTaskId, originalColorThumbnail, originalSepiaThumbnail)).thenReturn(updatedPerson);
        when(recurringTaskDtoMapper.map(updatedPersonDto)).thenReturn(updatedRecurringTaskDtoWithoutId);
        when(imageService.createThumbnails("new-data")).thenReturn(updatedThumbnails);
        when(savePersonHelper.saveAndFlushAndCommit(updatedPerson)).thenReturn(updatedPerson);
        when(personMapper.mapToDto(updatedPerson, updatedRecurringTaskDtoWithId)).thenReturn(updatedPersonDto);

        // act
        var result = personService.update(updatedPersonDto);

        // assert
        verify(personRepository).findById(personId);
        verify(recurringTasksService).findById(recurringTaskId);
        verify(personMapper).mapToModel(updatedPersonDto, recurringTaskId, originalColorThumbnail, originalSepiaThumbnail);
        verify(recurringTaskDtoMapper).map(updatedPersonDto);
        verify(imageService).createThumbnails("new-data");
        verify(recurringTasksService).update(updatedRecurringTaskDtoWithId);
        verify(savePersonHelper).saveAndFlushAndCommit(updatedPerson);
        verify(personMapper).mapToDto(updatedPerson, updatedRecurringTaskDtoWithId);
        verify(imageService).delete(originalColorThumbnail, originalSepiaThumbnail);
        verifyNoMoreInteractions(personRepository, personMapper, imageService, recurringTasksService, recurringTaskDtoMapper);

        assertEquals(updatedPersonDto, result);
    }

    @Test
    void updateWhenSuccessAndOnlyImageIsUpdated() {
        // arrange
        var originalColorThumbnail = "originalColorThumbnail.jpg";
        var originalSepiaThumbnail = "originalSepiaThumbnail.jpg";
        var updatedColorThumbnail = "updatedColorThumbnail.jpg";
        var updatedSepiaThumbnail = "updatedSepiaThumbnail.jpg";

        HashMap<ImageLabel, String> updatedThumbnails = new HashMap<>();
        updatedThumbnails.put(ImageLabel.COLOR_THUMBNAIL, updatedColorThumbnail);
        updatedThumbnails.put(ImageLabel.SEPIA_THUMBNAIL, updatedSepiaThumbnail);

        var personId = 1L;
        var recurringTaskId = 999L;

        var originalPerson = Person.builder()
                .id(personId)
                .name("Stijn")
                .colorThumbnail(originalColorThumbnail)
                .sepiaThumbnail(originalSepiaThumbnail)
                .recurringTaskId(recurringTaskId)
                .build();

        var originalRecurringTaskDto = RecurringTaskDto.builder()
                .id(recurringTaskId)
                .name("Stijn")
                .minNumberOfDaysBetweenExecutions(10)
                .maxNumberOfDaysBetweenExecutions(20)
                .build();

        var updatedPersonDto = PersonDto.builder()
                .id(personId)
                .name("Stijn")
                .newImageContent("new-data")
                .minNumberOfDaysBetweenContacts(10)
                .maxNumberOfDaysBetweenContacts(20)
                .build();

        var updatedPerson = Person.builder()
                .id(personId)
                .name("Stijn")
                .colorThumbnail(updatedColorThumbnail)
                .sepiaThumbnail(updatedSepiaThumbnail)
                .recurringTaskId(recurringTaskId)
                .build();

        var updatedRecurringTaskDtoWithoutId = RecurringTaskDto.builder()
                .name("Stijn")
                .minNumberOfDaysBetweenExecutions(10)
                .maxNumberOfDaysBetweenExecutions(20)
                .build();

        var updatedRecurringTaskDtoWithId = updatedRecurringTaskDtoWithoutId
                .toBuilder()
                .id(recurringTaskId)
                .build();

        when(personRepository.findById(personId)).thenReturn(Optional.of(originalPerson));
        when(recurringTasksService.findById(recurringTaskId)).thenReturn(Optional.of(originalRecurringTaskDto));
        when(personMapper.mapToModel(updatedPersonDto, recurringTaskId, originalColorThumbnail, originalSepiaThumbnail)).thenReturn(updatedPerson);
        when(recurringTaskDtoMapper.map(updatedPersonDto)).thenReturn(updatedRecurringTaskDtoWithoutId);
        when(imageService.createThumbnails("new-data")).thenReturn(updatedThumbnails);
        when(savePersonHelper.saveAndFlushAndCommit(updatedPerson)).thenReturn(updatedPerson);
        when(personMapper.mapToDto(updatedPerson, updatedRecurringTaskDtoWithId)).thenReturn(updatedPersonDto);

        // act
        var result = personService.update(updatedPersonDto);

        // assert
        verify(personRepository).findById(personId);
        verify(recurringTasksService).findById(recurringTaskId);
        verify(personMapper).mapToModel(updatedPersonDto, recurringTaskId, originalColorThumbnail, originalSepiaThumbnail);
        verify(recurringTaskDtoMapper).map(updatedPersonDto);
        verify(imageService).createThumbnails("new-data");
        verify(savePersonHelper).saveAndFlushAndCommit(updatedPerson);
        verify(personMapper).mapToDto(updatedPerson, updatedRecurringTaskDtoWithId);
        verify(imageService).delete(originalColorThumbnail, originalSepiaThumbnail);
        verifyNoMoreInteractions(personRepository, personMapper, imageService, recurringTasksService, recurringTaskDtoMapper);

        assertEquals(updatedPersonDto, result);
    }

    @Test
    void updateWhenSuccessAndOnlyNameIsUpdated() {
        // arrange
        var originalColorThumbnail = "originalColorThumbnail.jpg";
        var originalSepiaThumbnail = "originalSepiaThumbnail.jpg";
        var personId = 1L;
        var recurringTaskId = 999L;

        var originalPerson = Person.builder()
                .id(personId)
                .name("Stijn")
                .colorThumbnail(originalColorThumbnail)
                .sepiaThumbnail(originalSepiaThumbnail)
                .recurringTaskId(recurringTaskId)
                .build();

        var originalRecurringTaskDto = RecurringTaskDto.builder()
                .id(recurringTaskId)
                .name("Stijn")
                .minNumberOfDaysBetweenExecutions(10)
                .maxNumberOfDaysBetweenExecutions(20)
                .build();

        var updatedPersonDto = PersonDto.builder()
                .id(personId)
                .name("Tim")
                .colorThumbnail(originalColorThumbnail)
                .sepiaThumbnail(originalSepiaThumbnail)
                .minNumberOfDaysBetweenContacts(10)
                .maxNumberOfDaysBetweenContacts(20)
                .build();

        var updatedPerson = Person.builder()
                .id(personId)
                .name("Tim")
                .colorThumbnail(originalColorThumbnail)
                .sepiaThumbnail(originalSepiaThumbnail)
                .recurringTaskId(recurringTaskId)
                .build();

        var updatedRecurringTaskDtoWithoutId = RecurringTaskDto.builder()
                .name("Tim")
                .minNumberOfDaysBetweenExecutions(10)
                .maxNumberOfDaysBetweenExecutions(20)
                .build();

        var updatedRecurringTaskDtoWithId = updatedRecurringTaskDtoWithoutId
                .toBuilder()
                .id(recurringTaskId)
                .build();

        when(personRepository.findById(personId)).thenReturn(Optional.of(originalPerson));
        when(recurringTasksService.findById(recurringTaskId)).thenReturn(Optional.of(originalRecurringTaskDto));
        when(personMapper.mapToModel(updatedPersonDto, recurringTaskId, originalColorThumbnail, originalSepiaThumbnail)).thenReturn(updatedPerson);
        when(recurringTaskDtoMapper.map(updatedPersonDto)).thenReturn(updatedRecurringTaskDtoWithoutId);
        when(savePersonHelper.saveAndFlushAndCommit(updatedPerson)).thenReturn(updatedPerson);
        when(personMapper.mapToDto(updatedPerson, updatedRecurringTaskDtoWithId)).thenReturn(updatedPersonDto);

        // act
        var result = personService.update(updatedPersonDto);

        // assert
        verify(personRepository).findById(personId);
        verify(recurringTasksService).findById(recurringTaskId);
        verify(personMapper).mapToModel(updatedPersonDto, recurringTaskId, originalColorThumbnail, originalSepiaThumbnail);
        verify(recurringTaskDtoMapper).map(updatedPersonDto);
        verify(recurringTasksService).update(updatedRecurringTaskDtoWithId);
        verify(savePersonHelper).saveAndFlushAndCommit(updatedPerson);
        verify(personMapper).mapToDto(updatedPerson, updatedRecurringTaskDtoWithId);
        verifyNoMoreInteractions(personRepository, personMapper, imageService, recurringTasksService, recurringTaskDtoMapper);

        assertEquals(updatedPersonDto, result);
    }

    @Test
    void updateWhenSuccessAndOnlyMinAndMaxDaysAreUpdated() {
        // arrange
        var originalColorThumbnail = "originalColorThumbnail.jpg";
        var originalSepiaThumbnail = "originalSepiaThumbnail.jpg";
        var personId = 1L;
        var recurringTaskId = 999L;

        var originalPerson = Person.builder()
                .id(personId)
                .name("Stijn")
                .colorThumbnail(originalColorThumbnail)
                .sepiaThumbnail(originalSepiaThumbnail)
                .recurringTaskId(recurringTaskId)
                .build();

        var originalRecurringTaskDto = RecurringTaskDto.builder()
                .id(recurringTaskId)
                .name("Stijn")
                .minNumberOfDaysBetweenExecutions(10)
                .maxNumberOfDaysBetweenExecutions(20)
                .build();

        var updatedPersonDto = PersonDto.builder()
                .id(personId)
                .name("Stijn")
                .colorThumbnail(originalColorThumbnail)
                .sepiaThumbnail(originalSepiaThumbnail)
                .minNumberOfDaysBetweenContacts(5)
                .maxNumberOfDaysBetweenContacts(6)
                .build();

        var updatedPerson = Person.builder()
                .id(personId)
                .name("Stijn")
                .colorThumbnail(originalColorThumbnail)
                .sepiaThumbnail(originalSepiaThumbnail)
                .recurringTaskId(recurringTaskId)
                .build();

        var updatedRecurringTaskDtoWithoutId = RecurringTaskDto.builder()
                .name("Stijn")
                .minNumberOfDaysBetweenExecutions(5)
                .maxNumberOfDaysBetweenExecutions(6)
                .build();

        var updatedRecurringTaskDtoWithId = updatedRecurringTaskDtoWithoutId
                .toBuilder()
                .id(recurringTaskId)
                .build();

        when(personRepository.findById(personId)).thenReturn(Optional.of(originalPerson));
        when(recurringTasksService.findById(recurringTaskId)).thenReturn(Optional.of(originalRecurringTaskDto));
        when(personMapper.mapToModel(updatedPersonDto, recurringTaskId, originalColorThumbnail, originalSepiaThumbnail)).thenReturn(updatedPerson);
        when(recurringTaskDtoMapper.map(updatedPersonDto)).thenReturn(updatedRecurringTaskDtoWithoutId);
        when(personMapper.mapToDto(updatedPerson, updatedRecurringTaskDtoWithId)).thenReturn(updatedPersonDto);

        // act
        var result = personService.update(updatedPersonDto);

        // assert
        verify(personRepository).findById(personId);
        verify(recurringTasksService).findById(recurringTaskId);
        verify(personMapper).mapToModel(updatedPersonDto, recurringTaskId, originalColorThumbnail, originalSepiaThumbnail);
        verify(recurringTaskDtoMapper).map(updatedPersonDto);
        verify(recurringTasksService).update(updatedRecurringTaskDtoWithId);
        verify(personMapper).mapToDto(updatedPerson, updatedRecurringTaskDtoWithId);
        verifyNoMoreInteractions(personRepository, personMapper, imageService, recurringTasksService, recurringTaskDtoMapper);

        assertEquals(updatedPersonDto, result);
    }

    @Test
    void updateWhenImageServiceGivesError() {
        // arrange
        var originalColorThumbnail = "originalColorThumbnail.jpg";
        var originalSepiaThumbnail = "originalSepiaThumbnail.jpg";
        var personId = 1L;
        var recurringTaskId = 999L;

        var originalPerson = Person.builder()
                .id(personId)
                .name("Stijn")
                .colorThumbnail(originalColorThumbnail)
                .sepiaThumbnail(originalSepiaThumbnail)
                .recurringTaskId(recurringTaskId)
                .build();

        var originalRecurringTaskDto = RecurringTaskDto.builder()
                .id(recurringTaskId)
                .name("Stijn")
                .minNumberOfDaysBetweenExecutions(10)
                .maxNumberOfDaysBetweenExecutions(20)
                .build();

        var updatedPersonDto = PersonDto.builder()
                .id(personId)
                .name("Tim")
                .newImageContent("new-data")
                .minNumberOfDaysBetweenContacts(5)
                .maxNumberOfDaysBetweenContacts(6)
                .build();

        var updatedPerson = Person.builder()
                .id(personId)
                .name("Tim")
                .colorThumbnail(originalColorThumbnail)
                .sepiaThumbnail(originalSepiaThumbnail)
                .recurringTaskId(recurringTaskId)
                .build();

        var updatedRecurringTaskDtoWithoutId = RecurringTaskDto.builder()
                .name("Tim")
                .minNumberOfDaysBetweenExecutions(5)
                .maxNumberOfDaysBetweenExecutions(6)
                .build();

        when(personRepository.findById(personId)).thenReturn(Optional.of(originalPerson));
        when(recurringTasksService.findById(recurringTaskId)).thenReturn(Optional.of(originalRecurringTaskDto));
        when(personMapper.mapToModel(updatedPersonDto, recurringTaskId, originalColorThumbnail, originalSepiaThumbnail)).thenReturn(updatedPerson);
        when(recurringTaskDtoMapper.map(updatedPersonDto)).thenReturn(updatedRecurringTaskDtoWithoutId);
        when(imageService.createThumbnails("new-data")).thenThrow(new RuntimeException());

        // act
        assertThrows(RuntimeException.class, () -> personService.update(updatedPersonDto));

        // assert
        verify(personRepository).findById(personId);
        verify(recurringTasksService).findById(recurringTaskId);
        verify(personMapper).mapToModel(updatedPersonDto, recurringTaskId, originalColorThumbnail, originalSepiaThumbnail);
        verify(recurringTaskDtoMapper).map(updatedPersonDto);
        verify(imageService).createThumbnails("new-data");
        verifyNoMoreInteractions(personRepository, personMapper, imageService, recurringTasksService, recurringTaskDtoMapper);
    }

    @Test
    void updateWhenRecurringTasksServiceGivesError() {
        // arrange
        var originalColorThumbnail = "originalColorThumbnail.jpg";
        var originalSepiaThumbnail = "originalSepiaThumbnail.jpg";
        var updatedColorThumbnail = "updatedColorThumbnail.jpg";
        var updatedSepiaThumbnail = "updatedSepiaThumbnail.jpg";

        HashMap<ImageLabel, String> updatedThumbnails = new HashMap<>();
        updatedThumbnails.put(ImageLabel.COLOR_THUMBNAIL, updatedColorThumbnail);
        updatedThumbnails.put(ImageLabel.SEPIA_THUMBNAIL, updatedSepiaThumbnail);

        var personId = 1L;
        var recurringTaskId = 999L;

        var originalPerson = Person.builder()
                .id(personId)
                .name("Stijn")
                .colorThumbnail(originalColorThumbnail)
                .sepiaThumbnail(originalSepiaThumbnail)
                .recurringTaskId(recurringTaskId)
                .build();

        var originalRecurringTaskDto = RecurringTaskDto.builder()
                .id(recurringTaskId)
                .name("Stijn")
                .minNumberOfDaysBetweenExecutions(10)
                .maxNumberOfDaysBetweenExecutions(20)
                .build();

        var updatedPersonDto = PersonDto.builder()
                .id(personId)
                .name("Tim")
                .newImageContent("new-data")
                .minNumberOfDaysBetweenContacts(5)
                .maxNumberOfDaysBetweenContacts(6)
                .build();

        var updatedPerson = Person.builder()
                .id(personId)
                .name("Tim")
                .colorThumbnail(updatedColorThumbnail)
                .sepiaThumbnail(updatedSepiaThumbnail)
                .recurringTaskId(recurringTaskId)
                .build();

        var updatedRecurringTaskDtoWithoutId = RecurringTaskDto.builder()
                .name("Tim")
                .minNumberOfDaysBetweenExecutions(5)
                .maxNumberOfDaysBetweenExecutions(6)
                .build();

        var updatedRecurringTaskDtoWithId = updatedRecurringTaskDtoWithoutId
                .toBuilder()
                .id(recurringTaskId)
                .build();

        when(personRepository.findById(personId)).thenReturn(Optional.of(originalPerson));
        when(recurringTasksService.findById(recurringTaskId)).thenReturn(Optional.of(originalRecurringTaskDto));
        when(personMapper.mapToModel(updatedPersonDto, recurringTaskId, originalColorThumbnail, originalSepiaThumbnail)).thenReturn(updatedPerson);
        when(recurringTaskDtoMapper.map(updatedPersonDto)).thenReturn(updatedRecurringTaskDtoWithoutId);
        when(imageService.createThumbnails("new-data")).thenReturn(updatedThumbnails);
        doThrow(new RuntimeException()).when(recurringTasksService).update(updatedRecurringTaskDtoWithId);

        // act
        assertThrows(RuntimeException.class, () -> personService.update(updatedPersonDto));

        // assert
        verify(personRepository).findById(personId);
        verify(recurringTasksService).findById(recurringTaskId);
        verify(personMapper).mapToModel(updatedPersonDto, recurringTaskId, originalColorThumbnail, originalSepiaThumbnail);
        verify(recurringTaskDtoMapper).map(updatedPersonDto);
        verify(imageService).createThumbnails("new-data");
        verify(recurringTasksService).update(updatedRecurringTaskDtoWithId);
        verify(imageService).rollbackCreateImages(updatedThumbnails.values()); // assert that rollback of image happens
        verifyNoMoreInteractions(personRepository, personMapper, imageService, recurringTasksService, recurringTaskDtoMapper);
    }

    @Test
    void updateWhenRecurringTasksServiceGivesErrorAndImageWasNotUpdated() {
        // arrange
        var originalColorThumbnail = "originalColorThumbnail.jpg";
        var originalSepiaThumbnail = "originalSepiaThumbnail.jpg";
        var personId = 1L;
        var recurringTaskId = 999L;

        var originalPerson = Person.builder()
                .id(personId)
                .name("Stijn")
                .colorThumbnail(originalColorThumbnail)
                .sepiaThumbnail(originalSepiaThumbnail)
                .recurringTaskId(recurringTaskId)
                .build();

        var originalRecurringTaskDto = RecurringTaskDto.builder()
                .id(recurringTaskId)
                .name("Stijn")
                .minNumberOfDaysBetweenExecutions(10)
                .maxNumberOfDaysBetweenExecutions(20)
                .build();

        var updatedPersonDto = PersonDto.builder()
                .id(personId)
                .name("Tim")
                .colorThumbnail(originalColorThumbnail)
                .sepiaThumbnail(originalSepiaThumbnail)
                .minNumberOfDaysBetweenContacts(5)
                .maxNumberOfDaysBetweenContacts(6)
                .build();

        var updatedPerson = Person.builder()
                .id(personId)
                .name("Tim")
                .colorThumbnail(originalColorThumbnail)
                .sepiaThumbnail(originalSepiaThumbnail)
                .recurringTaskId(recurringTaskId)
                .build();

        var updatedRecurringTaskDtoWithoutId = RecurringTaskDto.builder()
                .name("Tim")
                .minNumberOfDaysBetweenExecutions(5)
                .maxNumberOfDaysBetweenExecutions(6)
                .build();

        var updatedRecurringTaskDtoWithId = updatedRecurringTaskDtoWithoutId
                .toBuilder()
                .id(recurringTaskId)
                .build();

        when(personRepository.findById(personId)).thenReturn(Optional.of(originalPerson));
        when(recurringTasksService.findById(recurringTaskId)).thenReturn(Optional.of(originalRecurringTaskDto));
        when(personMapper.mapToModel(updatedPersonDto, recurringTaskId, originalColorThumbnail, originalSepiaThumbnail)).thenReturn(updatedPerson);
        when(recurringTaskDtoMapper.map(updatedPersonDto)).thenReturn(updatedRecurringTaskDtoWithoutId);
        doThrow(new RuntimeException()).when(recurringTasksService).update(updatedRecurringTaskDtoWithId);

        // act
        assertThrows(RuntimeException.class, () -> personService.update(updatedPersonDto));

        // assert
        verify(personRepository).findById(personId);
        verify(recurringTasksService).findById(recurringTaskId);
        verify(personMapper).mapToModel(updatedPersonDto, recurringTaskId, originalColorThumbnail, originalSepiaThumbnail);
        verify(recurringTaskDtoMapper).map(updatedPersonDto);
        verify(recurringTasksService).update(updatedRecurringTaskDtoWithId);
        verifyNoMoreInteractions(personRepository, personMapper, imageService, recurringTasksService, recurringTaskDtoMapper);
    }

    @Test
    void updateWhenSavingToDatabaseGivesError() {
        // arrange
        var originalColorThumbnail = "originalColorThumbnail.jpg";
        var originalSepiaThumbnail = "originalSepiaThumbnail.jpg";
        var updatedColorThumbnail = "updatedColorThumbnail.jpg";
        var updatedSepiaThumbnail = "updatedSepiaThumbnail.jpg";

        HashMap<ImageLabel, String> updatedThumbnails = new HashMap<>();
        updatedThumbnails.put(ImageLabel.COLOR_THUMBNAIL, updatedColorThumbnail);
        updatedThumbnails.put(ImageLabel.SEPIA_THUMBNAIL, updatedSepiaThumbnail);

        var personId = 1L;
        var recurringTaskId = 999L;

        var originalPerson = Person.builder()
                .id(personId)
                .name("Stijn")
                .colorThumbnail(originalColorThumbnail)
                .sepiaThumbnail(originalSepiaThumbnail)
                .recurringTaskId(recurringTaskId)
                .build();

        var originalRecurringTaskDto = RecurringTaskDto.builder()
                .id(recurringTaskId)
                .name("Stijn")
                .minNumberOfDaysBetweenExecutions(10)
                .maxNumberOfDaysBetweenExecutions(20)
                .build();

        var updatedPersonDto = PersonDto.builder()
                .id(personId)
                .name("Tim")
                .newImageContent("new-data")
                .minNumberOfDaysBetweenContacts(5)
                .maxNumberOfDaysBetweenContacts(6)
                .build();

        var updatedPerson = Person.builder()
                .id(personId)
                .name("Tim")
                .colorThumbnail(updatedColorThumbnail)
                .sepiaThumbnail(updatedSepiaThumbnail)
                .recurringTaskId(recurringTaskId)
                .build();

        var updatedRecurringTaskDtoWithoutId = RecurringTaskDto.builder()
                .name("Tim")
                .minNumberOfDaysBetweenExecutions(5)
                .maxNumberOfDaysBetweenExecutions(6)
                .build();

        var updatedRecurringTaskDtoWithId = updatedRecurringTaskDtoWithoutId
                .toBuilder()
                .id(recurringTaskId)
                .build();

        when(personRepository.findById(personId)).thenReturn(Optional.of(originalPerson));
        when(recurringTasksService.findById(recurringTaskId)).thenReturn(Optional.of(originalRecurringTaskDto));
        when(personMapper.mapToModel(updatedPersonDto, recurringTaskId, originalColorThumbnail, originalSepiaThumbnail)).thenReturn(updatedPerson);
        when(recurringTaskDtoMapper.map(updatedPersonDto)).thenReturn(updatedRecurringTaskDtoWithoutId);
        when(imageService.createThumbnails("new-data")).thenReturn(updatedThumbnails);
        when(savePersonHelper.saveAndFlushAndCommit(updatedPerson)).thenThrow(new RuntimeException());

        // act
        assertThrows(RuntimeException.class, () -> personService.update(updatedPersonDto));

        // assert
        verify(personRepository).findById(personId);
        verify(recurringTasksService).findById(recurringTaskId);
        verify(personMapper).mapToModel(updatedPersonDto, recurringTaskId, originalColorThumbnail, originalSepiaThumbnail);
        verify(recurringTaskDtoMapper).map(updatedPersonDto);
        verify(imageService).createThumbnails("new-data");
        verify(recurringTasksService).update(updatedRecurringTaskDtoWithId);
        verify(savePersonHelper).saveAndFlushAndCommit(updatedPerson);
        verify(imageService).rollbackCreateImages(updatedThumbnails.values());
        verify(recurringTasksService).rollbackUpdateRecurringTask(originalRecurringTaskDto);
        verifyNoMoreInteractions(personRepository, personMapper, imageService, recurringTasksService, recurringTaskDtoMapper);
    }

    @Test
    void updateWhenCleaningUpOldImageGivesError() {
        // arrange
        var originalColorThumbnail = "originalColorThumbnail.jpg";
        var originalSepiaThumbnail = "originalSepiaThumbnail.jpg";
        var updatedColorThumbnail = "updatedColorThumbnail.jpg";
        var updatedSepiaThumbnail = "updatedSepiaThumbnail.jpg";

        HashMap<ImageLabel, String> updatedThumbnails = new HashMap<>();
        updatedThumbnails.put(ImageLabel.COLOR_THUMBNAIL, updatedColorThumbnail);
        updatedThumbnails.put(ImageLabel.SEPIA_THUMBNAIL, updatedSepiaThumbnail);

        var personId = 1L;
        var recurringTaskId = 999L;

        var originalPerson = Person.builder()
                .id(personId)
                .name("Stijn")
                .colorThumbnail(originalColorThumbnail)
                .sepiaThumbnail(originalSepiaThumbnail)
                .recurringTaskId(recurringTaskId)
                .build();

        var originalRecurringTaskDto = RecurringTaskDto.builder()
                .id(recurringTaskId)
                .name("Stijn")
                .minNumberOfDaysBetweenExecutions(10)
                .maxNumberOfDaysBetweenExecutions(20)
                .build();

        var updatedPersonDto = PersonDto.builder()
                .id(personId)
                .name("Tim")
                .newImageContent("new-data")
                .minNumberOfDaysBetweenContacts(5)
                .maxNumberOfDaysBetweenContacts(6)
                .build();

        var updatedPerson = Person.builder()
                .id(personId)
                .name("Tim")
                .colorThumbnail(updatedColorThumbnail)
                .sepiaThumbnail(updatedSepiaThumbnail)
                .recurringTaskId(recurringTaskId)
                .build();

        var updatedRecurringTaskDtoWithoutId = RecurringTaskDto.builder()
                .name("Tim")
                .minNumberOfDaysBetweenExecutions(5)
                .maxNumberOfDaysBetweenExecutions(6)
                .build();

        var updatedRecurringTaskDtoWithId = updatedRecurringTaskDtoWithoutId
                .toBuilder()
                .id(recurringTaskId)
                .build();

        when(personRepository.findById(personId)).thenReturn(Optional.of(originalPerson));
        when(recurringTasksService.findById(recurringTaskId)).thenReturn(Optional.of(originalRecurringTaskDto));
        when(personMapper.mapToModel(updatedPersonDto, recurringTaskId, originalColorThumbnail, originalSepiaThumbnail)).thenReturn(updatedPerson);
        when(recurringTaskDtoMapper.map(updatedPersonDto)).thenReturn(updatedRecurringTaskDtoWithoutId);
        when(imageService.createThumbnails("new-data")).thenReturn(updatedThumbnails);
        when(savePersonHelper.saveAndFlushAndCommit(updatedPerson)).thenReturn(updatedPerson);
        when(personMapper.mapToDto(updatedPerson, updatedRecurringTaskDtoWithId)).thenReturn(updatedPersonDto);
        doThrow(new RuntimeException()).when(imageService).delete(originalColorThumbnail); // an exception is thrown, but this should not halt the flow!

        // act
        var result = personService.update(updatedPersonDto);

        // assert
        verify(personRepository).findById(personId);
        verify(recurringTasksService).findById(recurringTaskId);
        verify(personMapper).mapToModel(updatedPersonDto, recurringTaskId, originalColorThumbnail, originalSepiaThumbnail);
        verify(recurringTaskDtoMapper).map(updatedPersonDto);
        verify(imageService).createThumbnails("new-data");
        verify(recurringTasksService).update(updatedRecurringTaskDtoWithId);
        verify(savePersonHelper).saveAndFlushAndCommit(updatedPerson);
        verify(personMapper).mapToDto(updatedPerson, updatedRecurringTaskDtoWithId);
        verify(imageService).delete(originalColorThumbnail, originalSepiaThumbnail);
        verifyNoMoreInteractions(personRepository, personMapper, imageService, recurringTasksService, recurringTaskDtoMapper);

        assertEquals(updatedPersonDto, result);
    }

    @Test
    void deleteWhenSuccess() {
        // arrange
        var person = Person.builder()
                .id(10L)
                .colorThumbnail("imageName.png")
                .sepiaThumbnail("sepia-imageName.png")
                .recurringTaskId(20L)
                .build();

        when(personRepository.findById(10L)).thenReturn(Optional.of(person));

        // act
        var deleteResult = personService.delete(10);

        // assert
        verify(personRepository).findById(10L);
        verify(imageService).delete("imageName.png", "sepia-imageName.png");
        verify(recurringTasksService).deleteById(20L);
        verify(personRepository).deleteById(10L);

        assertEquals(DeleteResult.DELETED, deleteResult);
    }

    @Test
    void deleteWhenPersonNotFound() {
        // arrange
        when(personRepository.findById(10L)).thenReturn(Optional.empty());

        // act
        var deleteResult = personService.delete(10);

        // assert
        verify(personRepository).findById(10L);

        assertEquals(DeleteResult.DOES_NOT_EXIST, deleteResult);
    }

    @Test
    void addContactWhenMaximallyFilledInAndSuccess() {
        // arrange
        var date = LocalDate.now();
        var latestUpdates = "this is a test";

        var contactDto = ContactDto.builder()
                .latestUpdates(latestUpdates)
                .lastContact(date)
                .build();

        var executionDto = ExecutionDto.builder()
                .date(date)
                .source(Source.USER)
                .build();

        var person = Person.builder()
                .id(100L)
                .recurringTaskId(888L)
                .latestUpdates("old")
                .build();

        when(personRepository.findById(100L)).thenReturn(Optional.of(person));

        // act
        personService.addContact(contactDto, 100, Source.USER);

        // assert
        assertEquals(latestUpdates, person.getLatestUpdates());
        verify(personRepository).findById(100L);
        verify(recurringTasksService).addExecution(executionDto, 888L);
        verifyNoMoreInteractions(personRepository, personMapper, imageService, recurringTasksService, recurringTaskDtoMapper);
    }

    @Test
    void addContactWhenOnlyDateHasBeenFilledInAndSuccess() {
        // arrange
        var date = LocalDate.now();

        var contactDto = ContactDto.builder()
                .latestUpdates(null)
                .lastContact(date)
                .build();

        var executionDto = ExecutionDto.builder()
                .date(date)
                .source(Source.EVENT)
                .build();

        var person = Person.builder()
                .id(100L)
                .recurringTaskId(888L)
                .latestUpdates("old")
                .build();

        when(personRepository.findById(100L)).thenReturn(Optional.of(person));

        // act
        personService.addContact(contactDto, 100, Source.EVENT);

        // assert
        assertNull(person.getLatestUpdates());
        verify(personRepository).findById(100L);
        verify(recurringTasksService).addExecution(executionDto, 888L);
        verifyNoMoreInteractions(personRepository, personMapper, imageService, recurringTasksService, recurringTaskDtoMapper);
    }

    @Test
    void addContactWhenPersonDoesNotExist() {
        // arrange
        var date = LocalDate.now();

        var contactDto = ContactDto.builder()
                .lastContact(date)
                .build();

        when(personRepository.findById(101L)).thenReturn(Optional.empty());

        // act, assert
        assertThrows(RuntimeException.class,
                () -> personService.addContact(contactDto, 101L, Source.USER),
                "Person with id 101 not found.");

        // assert
        verify(personRepository).findById(101L);
        verifyNoMoreInteractions(personRepository, personMapper, imageService, recurringTasksService, recurringTaskDtoMapper);
    }

}