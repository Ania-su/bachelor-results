package school.hei.demo.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.demo.domain.dto.request.ExamRequest;
import school.hei.demo.domain.dto.response.ExamResponse;
import school.hei.demo.domain.mappers.ExamMapper;
import school.hei.demo.endpoint.rest.controller.mapper.ExamRestMapper;
import school.hei.demo.entity.Exam;
import school.hei.demo.entity.User;
import school.hei.demo.enums.CodeType;
import school.hei.demo.enums.UserRole;
import school.hei.demo.exception.ForbiddenException;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.repository.CourseAssignmentRepository;
import school.hei.demo.repository.CourseSpecialtyRepository;
import school.hei.demo.repository.ExamRepository;
import school.hei.demo.repository.SpecialtyRepository;
import school.hei.demo.validators.ExamValidator;

@Service
@AllArgsConstructor
public class ExamService {

  private final ExamRepository repository;
  private final ExamMapper mapper;
  private final ExamValidator validator;
  private final CurrentUserService currentUserService;
  private final CourseAssignmentRepository courseAssignmentRepository;
  private final CourseSpecialtyRepository courseSpecialtyRepository;
  private final SpecialtyRepository specialtyRepository;
  private final ExamRestMapper restMapper;

  public List<ExamResponse> listForCourse(UUID courseId) {
    User currentUser = currentUserService.getCurrentUser();
    ensureTeacherAssigned(currentUser, courseId);

    var exams = repository.findAllByCourse_Id(courseId).stream().map(mapper::toDomain).toList();
    if (currentUser.getUserRole() != UserRole.STUDENT) {
      return exams.stream().map(restMapper::toResponse).toList();
    }

    if (!isStudentAllowedOnCourse(currentUser, courseId)) {
      return List.of();
    }

    Instant entryYearStart = entryYearStart(currentUser);
    return exams.stream()
        .filter(exam -> !exam.getDateExam().isBefore(entryYearStart))
        .map(restMapper::toResponse)
        .toList();
  }

  public ExamResponse get(UUID courseId, UUID examId) {
    User currentUser = currentUserService.getCurrentUser();
    ensureTeacherAssigned(currentUser, courseId);

    var exam =
        repository
            .findById(examId)
            .map(mapper::toDomain)
            .orElseThrow(() -> new NotFoundException("Exam " + examId + " not found"));
    ensureBelongsToCourse(exam, courseId);

    if (currentUser.getUserRole() == UserRole.STUDENT
        && (!isStudentAllowedOnCourse(currentUser, courseId)
            || exam.getDateExam().isBefore(entryYearStart(currentUser)))) {
      throw new ForbiddenException("Student is not allowed to access this exam");
    }

    return restMapper.toResponse(exam);
  }

  public ExamResponse create(UUID courseId, ExamRequest request) {
    var exam = restMapper.toDomain(request);
    ensureTeacherAssigned(currentUserService.getCurrentUser(), courseId);
    exam.setCourseId(courseId);
    validator.validate(exam);
    return restMapper.toResponse(mapper.toDomain(repository.save(mapper.toEntity(exam))));
  }

  public ExamResponse update(UUID courseId, UUID examId, ExamRequest request) {
    var updated = restMapper.toDomain(request);
    ensureTeacherAssigned(currentUserService.getCurrentUser(), courseId);
    updated.setCourseId(courseId);
    validator.validate(updated);
    var existing =
        repository
            .findById(examId)
            .orElseThrow(() -> new NotFoundException("Exam " + examId + " not found"));
    ensureBelongsToCourse(mapper.toDomain(existing), courseId);

    var toSave = mapper.toEntity(updated.toBuilder().id(examId).build());
    return restMapper.toResponse(mapper.toDomain(repository.save(toSave)));
  }

  public void delete(UUID courseId, UUID examId) {
    var exam = get(courseId, examId);
    repository.deleteById(exam.getId());
  }

  private void ensureTeacherAssigned(User user, UUID courseId) {
    if (user.getUserRole() == UserRole.TEACHER
        && !courseAssignmentRepository.existsByCourse_IdAndTeacherId(courseId, user.getId())) {
      throw new ForbiddenException("Teacher is not assigned to this course");
    }
  }

  private boolean isStudentAllowedOnCourse(User student, UUID courseId) {
    if (student.getSpecialtyId() == null) {
      return false;
    }

    CodeType studentSpecialty =
        specialtyRepository
            .findById(student.getSpecialtyId())
            .map(specialty -> specialty.getCode())
            .orElse(null);
    if (studentSpecialty == null) {
      return false;
    }

    return courseSpecialtyRepository.findByCourseId(courseId).stream()
        .map(courseSpecialty -> courseSpecialty.getSpecialty().getCode())
        .anyMatch(code -> isAllowedSpecialty(studentSpecialty, code));
  }

  private boolean isAllowedSpecialty(CodeType studentSpecialty, CodeType courseSpecialty) {
    return courseSpecialty == CodeType.NONE
        || courseSpecialty == studentSpecialty && studentSpecialty != CodeType.NONE;
  }

  private Instant entryYearStart(User student) {
    if (student.getEntryYear() == null) {
      throw new ForbiddenException("Student entry year is required to access exams");
    }
    return LocalDate.of(student.getEntryYear(), 1, 1).atStartOfDay(ZoneOffset.UTC).toInstant();
  }

  private void ensureBelongsToCourse(Exam exam, UUID courseId) {
    if (!exam.getCourseId().equals(courseId)) {
      throw new NotFoundException("Exam not found for this course");
    }
  }
}
