package be.stijnhooft.portal.social.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@SequenceGenerator(name = "personIdSequenceGenerator",
        sequenceName = "person_id_sequence",
        initialValue = 0,
        allocationSize = 50)
@Builder(toBuilder = true)
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "personIdSequenceGenerator")
    private Long id;

    @NotNull
    private String name;

    @NotNull
    @Column(name = "color_thumbnail")
    private String colorThumbnail;

    @NotNull
    @Column(name = "sepia_thumbnail")
    private String sepiaThumbnail;

    @Column(name = "recurring_task_id")
    private Long recurringTaskId;

    @Column(name = "latest_updates")
    private String latestUpdates;

}
