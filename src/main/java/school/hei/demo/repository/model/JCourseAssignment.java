package school.hei.demo.repository.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "course_assignment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class JCourseAssignment {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_course", nullable = false)
    private JCourse course;

    @Column(name = "id_teacher", nullable = false)
    private UUID teacherId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_group", nullable = false)
    private JGroup group;
}
