package school.hei.demo.domain.mappers;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.demo.entity.StudentGroupHistory;
import school.hei.demo.repository.GroupRepository;
import school.hei.demo.repository.UserRepository;
import school.hei.demo.repository.model.JStudentGroupHistory;

@Component
@AllArgsConstructor
public class StudentGroupHistoryMapper {

  private final UserRepository userRepository;
  private final GroupRepository groupRepository;

  public StudentGroupHistory toDomain(JStudentGroupHistory j) {
    if (j == null) return null;
    return StudentGroupHistory.builder()
        .id(j.getId())
        .studentId(j.getStudent().getId())
        .groupId(j.getGroup().getId())
        .startDate(j.getStartDate())
        .endDate(j.getEndDate())
        .build();
  }

  public JStudentGroupHistory toEntity(StudentGroupHistory h) {
    if (h == null) return null;
    return JStudentGroupHistory.builder()
        .id(h.getId())
        .student(userRepository.getReferenceById(h.getStudentId()))
        .group(groupRepository.getReferenceById(h.getGroupId()))
        .startDate(h.getStartDate())
        .endDate(h.getEndDate())
        .build();
  }
}
