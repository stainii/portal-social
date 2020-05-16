package be.stijnhooft.portal.social.dtos;

import lombok.*;

import java.time.LocalDate;

@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExecutionDto {

    @Getter
    @NonNull
    private LocalDate date;

    @Getter
    @NonNull
    private Source source;

}