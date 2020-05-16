package be.stijnhooft.portal.social.dtos;

import lombok.*;

import java.time.LocalDate;

@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class RecurringTaskDto {

    @Getter
    private Long id;

    @Getter
    @NonNull
    private String name;

    /**
     * The minimum number of days between each execution of this task.
     * It's not necessary to execute this task more regularly.
     **/
    @Getter
    private int minNumberOfDaysBetweenExecutions;

    /**
     * The maximum number of days between each execution of this task.
     * For example: Housagotchi is going to be very mad when this value gets exceeded.
     **/
    @Getter
    private int maxNumberOfDaysBetweenExecutions;

    /**
     * The date and time of the last execution of this task.
     * When never executed, this value is null
     **/
    @Getter
    private LocalDate lastExecution;

    public RecurringTaskDto() {}

    public RecurringTaskDto(String name, int minNumberOfDaysBetweenExecutions, int maxNumberOfDaysBetweenExecutions) {
        this.name = name;
        this.minNumberOfDaysBetweenExecutions = minNumberOfDaysBetweenExecutions;
        this.maxNumberOfDaysBetweenExecutions = maxNumberOfDaysBetweenExecutions;
    }

    public RecurringTaskDto(Long id, String name, int minNumberOfDaysBetweenExecutions, int maxNumberOfDaysBetweenExecutions, LocalDate lastExecution) {
        this(name, minNumberOfDaysBetweenExecutions, maxNumberOfDaysBetweenExecutions);
        this.id = id;
        this.lastExecution = lastExecution;
    }


}
