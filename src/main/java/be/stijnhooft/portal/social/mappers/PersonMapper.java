package be.stijnhooft.portal.social.mappers;

import be.stijnhooft.portal.social.dtos.PersonDto;
import be.stijnhooft.portal.social.dtos.RecurringTaskDto;
import be.stijnhooft.portal.social.model.Person;
import be.stijnhooft.portal.social.services.ImageService;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class PersonMapper {

    private final ImageService imageService;

    public PersonMapper(ImageService imageService) {
        this.imageService = imageService;
    }

    public PersonDto mapToDto(@NonNull Person person, @NonNull RecurringTaskDto recurringTask) {
        return PersonDto.builder()
                .id(person.getId())
                .name(person.getName())
                .imageName(imageService.getImageUrl(person.getImageName()))
                .minNumberOfDaysBetweenContacts(recurringTask.getMinNumberOfDaysBetweenExecutions())
                .maxNumberOfDaysBetweenContacts(recurringTask.getMaxNumberOfDaysBetweenExecutions())
                .lastContact(recurringTask.getLastExecution())
                .latestUpdates(person.getLatestUpdates())
                .build();
    }

    public Person mapToModel(@NonNull PersonDto personDto, long recurringTaskId, String imageName) {
        return Person.builder()
                .id(personDto.getId())
                .name(personDto.getName())
                .imageName(imageName)
                .recurringTaskId(recurringTaskId)
                .latestUpdates(personDto.getLatestUpdates())
                .build();
    }

}
