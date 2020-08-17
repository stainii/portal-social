package be.stijnhooft.portal.social.dtos;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Builder
public class PersonDto {

    private Long id;

    @NotNull
    private String name;

    /**
     * Fill in this field only when you want to upload a new image.
     */
    private String newImageContent;

    private String colorThumbnail;

    private String sepiaThumbnail;

    @NotNull
    private Integer minNumberOfDaysBetweenContacts;

    @NotNull
    private Integer maxNumberOfDaysBetweenContacts;

    private LocalDate lastContact;

    private String latestUpdates;

}
