package school.hei.demo.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "course")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class JCourse {

  @Id @GeneratedValue @EqualsAndHashCode.Include private UUID id;

  @Column(nullable = false, unique = true, length = 8)
  private String reference;

  @Column(nullable = false, length = 255)
  private String title;

  @Column(nullable = false)
  private Integer semester;

  @Column(nullable = false)
  private Integer credits;
}
