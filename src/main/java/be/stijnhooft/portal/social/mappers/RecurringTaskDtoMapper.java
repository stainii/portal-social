package be.stijnhooft.portal.social.mappers;

import be.stijnhooft.portal.social.dtos.PersonDto;
import be.stijnhooft.portal.social.dtos.RecurringTaskDto;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class RecurringTaskDtoMapper {

    public RecurringTaskDto map(@NonNull PersonDto personDto) {
        return RecurringTaskDto.builder()
                .name(personDto.getName())
                .minNumberOfDaysBetweenExecutions(personDto.getMinNumberOfDaysBetweenContacts())
                .maxNumberOfDaysBetweenExecutions(personDto.getMaxNumberOfDaysBetweenContacts())
                .lastExecution(personDto.getLastContact())
                .build();
    }

}
