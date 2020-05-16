package be.stijnhooft.portal.social.mappers;

import be.stijnhooft.portal.social.dtos.PersonDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RecurringTaskDtoMapperTest {

    private RecurringTaskDtoMapper recurringTaskDtoMapper;

    @BeforeEach
    void setUp() {
        this.recurringTaskDtoMapper = new RecurringTaskDtoMapper();
    }

    @Test
    public void mapWhenSuccess() {
        var personId = 1L;
        var name = "it's me";
        var minNumberOfDaysBetweenExecutions = 10;
        var maxNumberOfDaysBetweenExecutions = 20;
        var lastExecution = LocalDate.now();

        var personDto = PersonDto.builder()
                .id(personId)
                .name(name)
                .minNumberOfDaysBetweenContacts(minNumberOfDaysBetweenExecutions)
                .maxNumberOfDaysBetweenContacts(maxNumberOfDaysBetweenExecutions)
                .lastContact(lastExecution)
                .build();

        var result = recurringTaskDtoMapper.map(personDto);
        assertNull(result.getId());
        assertEquals(name, result.getName());
        assertEquals(minNumberOfDaysBetweenExecutions, result.getMinNumberOfDaysBetweenExecutions());
        assertEquals(maxNumberOfDaysBetweenExecutions, result.getMaxNumberOfDaysBetweenExecutions());
        assertEquals(lastExecution, result.getLastExecution());
    }

    @Test
    public void mapWhenPersonDtoIsNull() {
        assertThrows(NullPointerException.class, () -> recurringTaskDtoMapper.map(null));
    }
}