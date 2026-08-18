package school.hei.demo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.demo.domain.mappers.StudentGroupHistoryMapper;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.repository.StudentGroupHistoryRepository;
import school.hei.demo.repository.UserRepository;
import school.hei.demo.repository.model.JStudentGroupHistory;
import school.hei.demo.repository.model.JUser;

@Service
@AllArgsConstructor
public class StudentGroupHistoryService {

  private final StudentGroupHistoryRepository repository;
  private final StudentGroupHistoryMapper mapper;
  private final UserRepository userRepository;

  public List<JUser> listCurrentStudents(UUID groupId) {
    return repository.findCurrentByGroupId(groupId).stream()
        .map(JStudentGroupHistory::getStudent)
        .toList();
  }

  public void enroll(UUID studentId, UUID groupId, LocalDate startDate) {
    if (startDate == null) {
      throw new IllegalArgumentException("startDate is required");
    }

    repository
        .findCurrentByStudentId(studentId)
        .ifPresent(
            current -> {
              if (!startDate.isAfter(current.getStartDate())) {
                throw new IllegalArgumentException(
                    "startDate must be after the current enrollment's startDate");
              }
              current.setEndDate(startDate.minusDays(1));
              repository.save(current);
            });

    var domain =
        school.hei.demo.entity.StudentGroupHistory.builder()
            .studentId(studentId)
            .groupId(groupId)
            .startDate(startDate)
            .build();

    repository.save(mapper.toEntity(domain));
  }

  public void unenroll(UUID groupId, UUID studentId, LocalDate endDate) {
    var current =
        repository
            .findCurrentByStudentId(studentId)
            .filter(h -> h.getGroup().getId().equals(groupId))
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Student " + studentId + " has no current enrollment in group " + groupId));

    if (endDate.isBefore(current.getStartDate())) {
      throw new IllegalArgumentException("endDate must be on or after startDate");
    }

    current.setEndDate(endDate);
    repository.save(current);
  }
}
