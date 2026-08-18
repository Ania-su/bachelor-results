package school.hei.demo.domain.mappers;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.demo.domain.dto.request.ExamGroupRequest;
import school.hei.demo.domain.dto.response.ExamGroupResponse;
import school.hei.demo.entity.ExamGroup;
import school.hei.demo.repository.ExamRepository;
import school.hei.demo.repository.GroupRepository;
import school.hei.demo.repository.model.JExamGroup;

@Component
@AllArgsConstructor
public class ExamGroupMapper {

  private final ExamRepository examRepository;
  private final GroupRepository groupRepository;

  public ExamGroupResponse toResponse(ExamGroup eg) {
    if (eg == null) return null;
    return ExamGroupResponse.builder()
        .id(eg.getId())
        .examId(eg.getExamId())
        .groupId(eg.getGroupId())
        .build();
  }

  public ExamGroup toDomain(ExamGroupRequest request) {
    return ExamGroup.builder().groupId(request.getGroupId()).build();
  }

  public ExamGroup toDomain(JExamGroup j) {
    if (j == null) return null;
    return ExamGroup.builder()
        .id(j.getId())
        .examId(j.getExam().getId())
        .groupId(j.getGroup().getId())
        .build();
  }

  public JExamGroup toEntity(ExamGroup eg) {
    if (eg == null) return null;
    return JExamGroup.builder()
        .id(eg.getId())
        .exam(examRepository.getReferenceById(eg.getExamId()))
        .group(groupRepository.getReferenceById(eg.getGroupId()))
        .build();
  }
}
