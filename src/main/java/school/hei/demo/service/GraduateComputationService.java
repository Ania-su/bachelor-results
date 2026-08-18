package school.hei.demo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.demo.entity.Graduate;
import school.hei.demo.enums.UserRole;
import school.hei.demo.repository.CourseSpecialtyRepository;
import school.hei.demo.repository.GradeRepository;
import school.hei.demo.repository.UserRepository;
import school.hei.demo.repository.model.JUser;

@Service
@AllArgsConstructor
public class GraduateComputationService {

  private final UserRepository userRepository;
  private final CourseSpecialtyRepository courseSpecialtyRepository;
  private final GradeRepository gradeRepository;

  public List<Graduate> computeGraduates(int academicYear) {
    var candidates = userRepository.findAllByUserRoleAndEntryYear(UserRole.STUDENT, academicYear);

    List<Graduate> graduates = new ArrayList<>();

    for (JUser candidate : candidates) {
      var requiredCourseIds =
          courseSpecialtyRepository.findRequiredCourseIds(
              candidate.getId(), candidate.getSpecialtyId());
      if (requiredCourseIds.isEmpty()) continue;

      var courseAverages = gradeRepository.findCourseAverages(candidate.getId(), requiredCourseIds);

      boolean coversAllCourses = courseAverages.size() == requiredCourseIds.size();
      boolean allAboveThreshold =
          courseAverages.stream().allMatch(r -> r.getAverage().compareTo(BigDecimal.TEN) >= 0);

      if (!coversAllCourses || !allAboveThreshold) continue;

      BigDecimal weightedSum = BigDecimal.ZERO;
      BigDecimal totalCredits = BigDecimal.ZERO;
      for (var r : courseAverages) {
        BigDecimal credits = BigDecimal.valueOf(r.getCredits());
        weightedSum = weightedSum.add(r.getAverage().multiply(credits));
        totalCredits = totalCredits.add(credits);
      }
      BigDecimal generalAverage = weightedSum.divide(totalCredits, 2, RoundingMode.HALF_UP);

      graduates.add(
          Graduate.builder()
              .reference(candidate.getReference())
              .firstName(candidate.getFirstName())
              .lastName(candidate.getLastName())
              .average(generalAverage)
              .build());
    }

    graduates.sort(Comparator.comparing(Graduate::getAverage).reversed());
    for (int i = 0; i < graduates.size(); i++) {
      graduates.get(i).setRank(i + 1);
    }
    return graduates;
  }
}
