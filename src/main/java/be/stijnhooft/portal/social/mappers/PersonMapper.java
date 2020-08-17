package be.stijnhooft.portal.social.mappers;

import be.stijnhooft.portal.social.dtos.PersonDto;
import be.stijnhooft.portal.social.dtos.RecurringTaskDto;
import be.stijnhooft.portal.social.model.Person;
import be.stijnhooft.portal.social.services.ImageService;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class PersonMapper {

    public PersonDto mapToDto(@NonNull Person person, @NonNull RecurringTaskDto recurringTask) {
        return PersonDto.builder()
                .id(person.getId())
                .name(person.getName())
                .colorThumbnail(person.getColorThumbnail())
                .sepiaThumbnail(person.getSepiaThumbnail())
                .minNumberOfDaysBetweenContacts(recurringTask.getMinNumberOfDaysBetweenExecutions())
                .maxNumberOfDaysBetweenContacts(recurringTask.getMaxNumberOfDaysBetweenExecutions())
                .lastContact(recurringTask.getLastExecution())
                .latestUpdates(person.getLatestUpdates())
                .build();
    }

    public Person mapToModel(@NonNull PersonDto personDto, long recurringTaskId, String colorThumbnailName, String sepiaThumbnailName) {
        return Person.builder()
                .id(personDto.getId())
                .name(personDto.getName())
                .colorThumbnail(colorThumbnailName)
                .sepiaThumbnail(sepiaThumbnailName)
                .recurringTaskId(recurringTaskId)
                .latestUpdates(personDto.getLatestUpdates())
                .build();
    }

}
