package be.stijnhooft.portal.social.mappers;

import be.stijnhooft.portal.social.dtos.PersonDto;
import be.stijnhooft.portal.social.dtos.RecurringTaskDto;
import be.stijnhooft.portal.social.model.Person;
import be.stijnhooft.portal.social.services.ImageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonMapperTest {

    @InjectMocks
    private PersonMapper personMapper;

    @Mock
    private ImageService imageService;

    @Test
    public void mapToDtoWhenSuccess() {
        var personId = 1L;
        var recurringTaskId = 2L;
        var imageName = "my_image.png";
        var imagePath = "http://localhost:3000/api/retrieve/my_image.png";
        var minNumberOfDaysBetweenExecutions = 10;
        var maxNumberOfDaysBetweenExecutions = 20;
        var lastExecution = LocalDate.now();
        var name = "Stijn";
        var latestUpdates = "hallo";

        var person = Person.builder()
                .id(personId)
                .name(name)
                .imageName(imageName)
                .recurringTaskId(recurringTaskId)
                .latestUpdates(latestUpdates)
                .build();

        var recurringTask = RecurringTaskDto.builder()
                .id(recurringTaskId)
                .minNumberOfDaysBetweenExecutions(minNumberOfDaysBetweenExecutions)
                .maxNumberOfDaysBetweenExecutions(maxNumberOfDaysBetweenExecutions)
                .lastExecution(lastExecution)
                .build();

        when(imageService.getImageUrl(imageName)).thenReturn(imagePath);

        var result = personMapper.mapToDto(person, recurringTask);

        assertEquals(personId, result.getId());
        assertEquals(name, result.getName());
        assertEquals(imagePath, result.getImageName());
        assertEquals(minNumberOfDaysBetweenExecutions, result.getMinNumberOfDaysBetweenContacts());
        assertEquals(maxNumberOfDaysBetweenExecutions, result.getMaxNumberOfDaysBetweenContacts());
        assertEquals(lastExecution, result.getLastContact());
        assertEquals(latestUpdates, result.getLatestUpdates());
    }

    @Test
    public void mapToDtoWhenPersonIsNull() {
        var recurringTask = RecurringTaskDto.builder()
                .id(2L)
                .minNumberOfDaysBetweenExecutions(10)
                .maxNumberOfDaysBetweenExecutions(20)
                .lastExecution(LocalDate.now())
                .build();

        assertThrows(NullPointerException.class, () -> personMapper.mapToDto(null, recurringTask));
    }

    @Test
    public void mapToDtoWhenRecurringTaskIsNull() {
        var person = Person.builder()
                .id(1L)
                .imageName("my_image.png")
                .recurringTaskId(2L)
                .build();

        assertThrows(NullPointerException.class, () -> personMapper.mapToDto(person, null));
    }

    @Test
    public void mapToModelWhenSuccess() {
        var personId = 1L;
        var recurringTaskId = 2L;
        var originalImageName = "my_image.png";
        var imageNameDto = "http://localhost:3000/api/retrieve/my_image.png";
        var minNumberOfDaysBetweenExecutions = 10;
        var maxNumberOfDaysBetweenExecutions = 20;
        var lastExecution = LocalDate.now();
        var name = "Stijn";
        var latestUpdates = "bla";

        var personDto = PersonDto.builder()
                .id(personId)
                .name(name)
                .imageName(imageNameDto)
                .newImageContent("my_new_image_data")
                .minNumberOfDaysBetweenContacts(minNumberOfDaysBetweenExecutions)
                .maxNumberOfDaysBetweenContacts(maxNumberOfDaysBetweenExecutions)
                .lastContact(lastExecution)
                .latestUpdates(latestUpdates)
                .build();

        var result = personMapper.mapToModel(personDto, recurringTaskId, originalImageName);
        assertEquals(personId, result.getId());
        assertEquals(personId, result.getId());
        assertEquals(name, result.getName());
        assertEquals(originalImageName, result.getImageName());
        assertEquals(recurringTaskId, result.getRecurringTaskId());
        assertEquals(latestUpdates, result.getLatestUpdates());
    }

    @Test
    public void mapToModelWhenPersonDtoIsNull() {
        assertThrows(NullPointerException.class, () -> personMapper.mapToModel(null, 1, "bla.png"));
    }

}