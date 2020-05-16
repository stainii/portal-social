package be.stijnhooft.portal.social.dtos;

import lombok.*;

import java.time.LocalDate;

@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContactDto {

    @Getter
    private String latestUpdates;

    @Getter
    private LocalDate lastContact;

}