package be.stijnhooft.portal.social.services;

import be.stijnhooft.portal.social.dtos.*;
import be.stijnhooft.portal.social.mappers.PersonMapper;
import be.stijnhooft.portal.social.mappers.RecurringTaskDtoMapper;
import be.stijnhooft.portal.social.model.Person;
import be.stijnhooft.portal.social.repositories.PersonRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

@Service
@Transactional
@Slf4j
public class PersonService {

    private final RecurringTasksService recurringTasksService;
    private final ImageService imageService;
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
    private final RecurringTaskDtoMapper recurringTaskDtoMapper;
    private final SavePersonHelper savePersonHelper;

    public PersonService(RecurringTasksService recurringTasksService, ImageService imageService, PersonRepository personRepository, PersonMapper personMapper, RecurringTaskDtoMapper recurringTaskDtoMapper, SavePersonHelper savePersonHelper) {
        this.recurringTasksService = recurringTasksService;
        this.imageService = imageService;
        this.personRepository = personRepository;
        this.personMapper = personMapper;
        this.recurringTaskDtoMapper = recurringTaskDtoMapper;
        this.savePersonHelper = savePersonHelper;
    }

    public List<PersonDto> findAll() {
        return personRepository.findAll()
                .stream()
                .map(this::enrichAndMap)
                .collect(Collectors.toList());
    }

    public Optional<PersonDto> findById(long id) {
        return personRepository.findById(id)
                .map(this::enrichAndMap);
    }

    public PersonDto create(@NonNull PersonDto personDto) {
        // verify person doesn't exist yet
        if (personRepository
                .findByName(personDto.getName())
                .isPresent()) {
            throw new IllegalArgumentException("Person with name " + personDto.getName() + " already exists");
        }

        String imageName = createImage(personDto);

        // create recurring task
        RecurringTaskDto createdRecurringTask;
        try {
            createdRecurringTask = createRecurringTask(personDto);
        } catch (RuntimeException e) {
            imageService.rollbackCreateImage(imageName);
            throw e;
        }

        // persist in database
        try {
            var person = createPerson(personDto, imageName, createdRecurringTask.getId());
            return enrichAndMap(person, createdRecurringTask);
        } catch (RuntimeException e) {
            imageService.rollbackCreateImage(imageName);
            recurringTasksService.rollbackCreateRecurringTask(createdRecurringTask.getId());
            throw e;
        }
    }

    public PersonDto update(@NonNull PersonDto updatedPersonDto) {
        // get originals
        var originalPerson = personRepository
                .findById(updatedPersonDto.getId())
                .orElseThrow(() -> new RuntimeException("Person with id " + updatedPersonDto.getId() + " does not exist."));
        var originalRecurringTaskDto = recurringTasksService
                .findById(originalPerson.getRecurringTaskId())
                .orElseThrow();

        // calculate the updated entities
        Person updatedPerson = personMapper
                .mapToModel(updatedPersonDto, originalPerson.getRecurringTaskId(), originalPerson.getImageName());

        RecurringTaskDto updatedRecurringTask = recurringTaskDtoMapper
                .map(updatedPersonDto)
                .toBuilder()
                .id(originalPerson.getRecurringTaskId())
                .build();

        // keeping information about how far we got, in case something goes wrong
        String newImageName = null;
        boolean recurringTaskUpdated = false;

        try {
            // creating a new image now, when everything goes right we'll remove the old image
            if (isNotEmpty(updatedPersonDto.getNewImageContent())) {
                newImageName = imageService.createThumbnail(updatedPersonDto.getNewImageContent());
                updatedPerson.setImageName(newImageName);
            }

            // update recurring task
            if (!originalRecurringTaskDto.equals(updatedRecurringTask)) {
                recurringTasksService.update(updatedRecurringTask);
                recurringTaskUpdated = true;
            }

            // update person
            if (!originalPerson.equals(updatedPerson)) {
                savePersonHelper.saveAndFlushAndCommit(updatedPerson);
            }
        } catch (RuntimeException e) {  // something went wrong, let's revert
            if (newImageName != null) {
                imageService.rollbackCreateImage(newImageName);
            }
            if (recurringTaskUpdated) {
                recurringTasksService.rollbackUpdateRecurringTask(originalRecurringTaskDto);
            }
            throw e;
        }

        // when everything has gone right, clean up
        try {
            if (newImageName != null) {
                imageService.delete(originalPerson.getImageName());
            }
        } catch (RuntimeException e) {
            log.warn("Something went wrong cleaning up old image after update", e);
        }

        return enrichAndMap(updatedPerson, updatedRecurringTask);
    }

    public DeleteResult delete(long id) {
        return personRepository.findById(id)
                .map(person -> {
                    personRepository.deleteById(id);
                    recurringTasksService.deleteById(person.getRecurringTaskId());
                    imageService.delete(person.getImageName());
                    return DeleteResult.DELETED;
                }).orElseGet(() -> DeleteResult.DOES_NOT_EXIST);
    }

    public void addContact(@NonNull ContactDto contact, long personId, Source source) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new RuntimeException("Person with id " + personId + " not found"));
        person.setLatestUpdates(contact.getLatestUpdates());

        if (contact.getLastContact() != null) {
            ExecutionDto execution = ExecutionDto.builder()
                    .date(contact.getLastContact())
                    .source(source)
                    .build();
            recurringTasksService.addExecution(execution, person.getRecurringTaskId());
        }
    }

    private PersonDto enrichAndMap(@NonNull Person person) {
        return recurringTasksService.findById(person.getRecurringTaskId())
                .map(recurringTask -> this.enrichAndMap(person, recurringTask))
                .orElseThrow(() -> new RuntimeException("Recurring task with id " + person.getRecurringTaskId() + " not found!"));
    }

    private PersonDto enrichAndMap(@NonNull Person person, @NonNull RecurringTaskDto recurringTask) {
        return personMapper.mapToDto(person, recurringTask);
    }

    private RecurringTaskDto createRecurringTask(@NonNull PersonDto personDto) {
        var recurringTaskDto = recurringTaskDtoMapper.map(personDto);
        // TODO: should not throw an error when the recurring task with that name already exists, but should link the person to the existing recurring task
        return recurringTasksService.create(recurringTaskDto);
    }

    private String createImage(@NonNull PersonDto personDto) {
        var imageContent = personDto.getNewImageContent();
        return imageService.createThumbnail(imageContent);
    }

    private Person createPerson(@NonNull PersonDto personDto, @NonNull String imageName, long recurringTaskId) {
        var person = Person.builder()
                .name(personDto.getName())
                .imageName(imageName)
                .recurringTaskId(recurringTaskId)
                .build();
        return savePersonHelper.saveAndFlushAndCommit(person);
    }

}
