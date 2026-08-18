package school.hei.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.hei.demo.entity.CourseAverageResult;
import school.hei.demo.entity.Graduate;
import school.hei.demo.enums.UserRole;
import school.hei.demo.repository.CourseSpecialtyRepository;
import school.hei.demo.repository.GradeRepository;
import school.hei.demo.repository.UserRepository;
import school.hei.demo.repository.model.JUser;

@ExtendWith(MockitoExtension.class)
class GraduateComputationServiceTest {
  @Mock UserRepository userRepository;
  @Mock CourseSpecialtyRepository courseSpecialtyRepository;
  @Mock GradeRepository gradeRepository;
  @InjectMocks GraduateComputationService service;

  private JUser student(UUID specialtyId, String reference) {
    JUser u = new JUser();
    u.setId(UUID.randomUUID());
    u.setReference(reference);
    u.setFirstName("A");
    u.setLastName("B");
    u.setSpecialtyId(specialtyId);
    u.setUserRole(UserRole.STUDENT);
    u.setEntryYear(2024);
    return u;
  }

  @Test
  void noStudents_returnsEmpty() {
    when(userRepository.findAllByUserRoleAndEntryYear(UserRole.STUDENT, 2024))
        .thenReturn(List.of());
    assertEquals(List.of(), service.computeGraduates(2024));
  }

  @Test
  void studentWithNoRequiredCourses_isSkipped() {
    UUID specialtyId = UUID.randomUUID();
    JUser s = student(specialtyId, "REF");

    when(userRepository.findAllByUserRoleAndEntryYear(UserRole.STUDENT, 2024))
        .thenReturn(List.of(s));

    when(courseSpecialtyRepository.findRequiredCourseIds(s.getId(), specialtyId))
        .thenReturn(Set.of());
    assertEquals(List.of(), service.computeGraduates(2024));
  }

  @Test
  void studentMissingSomeCourseAverages_isSkipped() {
    UUID specialtyId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();

    JUser s = student(specialtyId, "REF");

    when(userRepository.findAllByUserRoleAndEntryYear(UserRole.STUDENT, 2024))
        .thenReturn(List.of(s));

    when(courseSpecialtyRepository.findRequiredCourseIds(s.getId(), specialtyId))
        .thenReturn(Set.of(courseId));
    when(gradeRepository.findCourseAverages(s.getId(), Set.of(courseId))).thenReturn(List.of());
    assertEquals(List.of(), service.computeGraduates(2024));
  }

  @Test
  void studentWithAverageBelowThreshold_isSkipped() {
    UUID specialtyId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();

    JUser s = student(specialtyId, "REF");

    when(userRepository.findAllByUserRoleAndEntryYear(UserRole.STUDENT, 2024))
        .thenReturn(List.of(s));

    when(courseSpecialtyRepository.findRequiredCourseIds(s.getId(), specialtyId))
        .thenReturn(Set.of(courseId));
    when(gradeRepository.findCourseAverages(s.getId(), Set.of(courseId)))
        .thenReturn(
            List.of(
                CourseAverageResult.builder()
                    .courseId(courseId)
                    .credits(5)
                    .average(new BigDecimal("9"))
                    .build()));
    assertEquals(List.of(), service.computeGraduates(2024));
  }

  @Test
  void studentGraduate_usesWeightedAverageAndRanks() {
    UUID specialtyId = UUID.randomUUID();
    UUID c1 = UUID.randomUUID();
    UUID c2 = UUID.randomUUID();

    JUser s = student(specialtyId, "REF");

    when(userRepository.findAllByUserRoleAndEntryYear(UserRole.STUDENT, 2024))
        .thenReturn(List.of(s));
    when(courseSpecialtyRepository.findRequiredCourseIds(s.getId(), specialtyId))
        .thenReturn(Set.of(c1, c2));
    when(gradeRepository.findCourseAverages(s.getId(), Set.of(c1, c2)))
        .thenReturn(
            List.of(
                CourseAverageResult.builder()
                    .courseId(c1)
                    .credits(4)
                    .average(new BigDecimal("12"))
                    .build(),
                CourseAverageResult.builder()
                    .courseId(c2)
                    .credits(6)
                    .average(new BigDecimal("14"))
                    .build()));

    List<Graduate> graduates = service.computeGraduates(2024);

    assertEquals(1, graduates.size());
    Graduate g = graduates.get(0);
    assertEquals(0, g.getAverage().compareTo(new BigDecimal("13.20")));
    assertEquals(1, g.getRank());
    assertEquals("REF", g.getReference());
  }

  @Test
  void multipleGraduates_areRankedByAverageDescending() {
    UUID specialtyId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();

    JUser low = student(specialtyId, "LOW");
    JUser high = student(specialtyId, "HIGH");

    when(userRepository.findAllByUserRoleAndEntryYear(UserRole.STUDENT, 2024))
        .thenReturn(List.of(low, high));

    when(courseSpecialtyRepository.findRequiredCourseIds(low.getId(), specialtyId))
        .thenReturn(Set.of(courseId));

    when(courseSpecialtyRepository.findRequiredCourseIds(high.getId(), specialtyId))
        .thenReturn(Set.of(courseId));

    when(gradeRepository.findCourseAverages(low.getId(), Set.of(courseId)))
        .thenReturn(
            List.of(
                CourseAverageResult.builder()
                    .courseId(courseId)
                    .credits(10)
                    .average(new BigDecimal("10"))
                    .build()));

    when(gradeRepository.findCourseAverages(high.getId(), Set.of(courseId)))
        .thenReturn(
            List.of(
                CourseAverageResult.builder()
                    .courseId(courseId)
                    .credits(10)
                    .average(new BigDecimal("15"))
                    .build()));

    List<Graduate> graduates = service.computeGraduates(2024);

    assertEquals(2, graduates.size());
    assertEquals("HIGH", graduates.get(0).getReference());
    assertEquals(1, graduates.get(0).getRank());
    assertEquals("LOW", graduates.get(1).getReference());
    assertEquals(2, graduates.get(1).getRank());
  }
}
