package school.hei.demo.service;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.demo.domain.dto.request.GradeCreate;
import school.hei.demo.domain.dto.request.GradeUpdate;
import school.hei.demo.domain.dto.response.GradePage;
import school.hei.demo.domain.dto.response.GradeResponse;
import school.hei.demo.domain.mappers.GradeMapper;
import school.hei.demo.endpoint.rest.controller.mapper.GradeRestMapper;
import school.hei.demo.entity.Grade;
import school.hei.demo.entity.User;
import school.hei.demo.enums.UserRole;
import school.hei.demo.exception.BadRequestException;
import school.hei.demo.exception.ForbiddenException;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.repository.CourseAssignmentRepository;
import school.hei.demo.repository.ExamRepository;
import school.hei.demo.repository.GradeHistoryRepository;
import school.hei.demo.repository.GradeRepository;
import school.hei.demo.repository.UserRepository;
import school.hei.demo.repository.model.JGradeHistory;
import school.hei.demo.validators.GradeValidator;
import school.hei.demo.validators.PaginationValidator;

@Service
@AllArgsConstructor
public class GradeService {
  private final GradeRepository repository;
  private final GradeHistoryRepository historyRepository;
  private final ExamRepository examRepository;
  private final CourseAssignmentRepository courseAssignmentRepository;
  private final UserRepository userRepository;
  private final CurrentUserService currentUserService;
  private final GradeMapper mapper;
  private final GradeRestMapper restMapper;
  private final GradeValidator validator;
  private final PaginationValidator paginationValidator;

  @Transactional(readOnly = true)
  public GradePage list(UUID courseId, UUID examId, UUID studentId, int page, int pageSize) {
    User currentUser = ensureReadAccess(courseId);

    if (currentUser.getUserRole() == UserRole.STUDENT) {
      if (studentId != null && !studentId.equals(currentUser.getId())) {
        throw new ForbiddenException("Students can only access their own grades");
      }
      studentId = currentUser.getId();
    }

    paginationValidator.validate(page, pageSize);

    ensureExamBelongsToCourse(courseId, examId);

    var pageable = org.springframework.data.domain.PageRequest.of(page, pageSize);

    var grades =
        studentId == null
            ? repository.findAllByExam_Id(examId, pageable)
            : repository.findAllByExam_IdAndStudent_Id(examId, studentId, pageable);

    var content = grades.map(mapper::toDomain);

    return new GradePage(
        content.getContent().stream().map(restMapper::toResponse).toList(),
        new school.hei.demo.domain.dto.response.PageMetadata(
            content.getNumber(),
            content.getSize(),
            (int) content.getTotalElements(),
            content.getTotalPages()));
  }

  @Transactional(readOnly = true)
  public GradeResponse get(UUID courseId, UUID examId, UUID gradeId) {
    User currentUser = ensureReadAccess(courseId);

    var grade =
        repository
            .findById(gradeId)
            .map(mapper::toDomain)
            .orElseThrow(() -> new NotFoundException("Grade " + gradeId + " not found"));

    ensureGradeBelongsToExam(courseId, examId, grade);
    if (currentUser.getUserRole() == UserRole.STUDENT
        && !currentUser.getId().equals(grade.getStudentId())) {
      throw new ForbiddenException("Students can only access their own grades");
    }

    return restMapper.toResponse(grade);
  }

  @Transactional
  public GradeResponse create(UUID courseId, UUID examId, GradeCreate request) {
    ensureWriteAccess(courseId);
    validator.validateCreate(request);

    var grade = restMapper.toDomain(request);

    ensureExamBelongsToCourse(courseId, examId);

    if (!userRepository.existsById(grade.getStudentId())) {
      throw new NotFoundException("Student " + grade.getStudentId() + " not found");
    }
    if (repository.existsByStudent_IdAndExam_Id(grade.getStudentId(), examId)) {
      throw new BadRequestException("Student already has a grade for this exam");
    }

    grade.setExamId(examId);
    grade.setUpdatedAt(Instant.now());

    var createdGrade = mapper.toDomain(repository.save(mapper.toEntity(grade)));
    historyRepository.save(
        JGradeHistory.builder()
            .gradeId(createdGrade.getId())
            .oldValue(java.math.BigDecimal.ZERO)
            .newValue(createdGrade.getValue())
            .reason("Initial grade")
            .changedBy(currentUserService.getCurrentUser().getId())
            .changedAt(Instant.now())
            .build());

    return restMapper.toResponse(createdGrade);
  }

  @Transactional
  public GradeResponse update(UUID courseId, UUID examId, UUID gradeId, GradeUpdate request) {
    ensureWriteAccess(courseId);

    validator.validateUpdate(request);

    var currentGrade =
        repository
            .findById(gradeId)
            .map(mapper::toDomain)
            .orElseThrow(() -> new NotFoundException("Grade " + gradeId + " not found"));
    ensureGradeBelongsToExam(courseId, examId, currentGrade);

    var now = Instant.now();

    historyRepository.save(
        JGradeHistory.builder()
            .gradeId(currentGrade.getId())
            .oldValue(currentGrade.getValue())
            .newValue(request.getValue())
            .reason(request.getReason())
            .changedAt(now)
            .changedBy(currentUserService.getCurrentUser().getId())
            .build());

    currentGrade.setValue(request.getValue());
    currentGrade.setUpdatedAt(now);
    return restMapper.toResponse(mapper.toDomain(repository.save(mapper.toEntity(currentGrade))));
  }

  @Transactional
  public GradeResponse delete(UUID courseId, UUID examId, UUID gradeId) {
    ensureWriteAccess(courseId);

    var grade =
        repository
            .findById(gradeId)
            .map(mapper::toDomain)
            .orElseThrow(() -> new NotFoundException("Grade " + gradeId + " not found"));
    ensureGradeBelongsToExam(courseId, examId, grade);

    historyRepository.deleteAllByGradeId(gradeId);
    repository.deleteById(gradeId);

    return restMapper.toResponse(grade);
  }

  private User ensureReadAccess(UUID courseId) {
    User currentUser = currentUserService.getCurrentUser();
    ensureTeacherAssigned(currentUser, courseId);
    return currentUser;
  }

  private void ensureWriteAccess(UUID courseId) {
    User currentUser = currentUserService.getCurrentUser();
    if (currentUser.getUserRole() == UserRole.STUDENT) {
      throw new ForbiddenException("Students have read-only access to grades");
    }
    ensureTeacherAssigned(currentUser, courseId);
  }

  private void ensureTeacherAssigned(User user, UUID courseId) {
    if (user.getUserRole() == UserRole.TEACHER
        && !courseAssignmentRepository.existsByCourse_IdAndTeacherId(courseId, user.getId())) {
      throw new ForbiddenException("Teacher is not assigned to this course");
    }
  }

  private void ensureExamBelongsToCourse(UUID courseId, UUID examId) {

    var exam =
        examRepository
            .findById(examId)
            .orElseThrow(() -> new NotFoundException("Exam " + examId + " not found"));

    if (!exam.getCourse().getId().equals(courseId)) {
      throw new NotFoundException("Exam not found for this course");
    }
  }

  private void ensureGradeBelongsToExam(UUID courseId, UUID examId, Grade grade) {
    if (!grade.getExamId().equals(examId)) {
      throw new NotFoundException("Grade not found for this exam");
    }
    ensureExamBelongsToCourse(courseId, examId);
  }
}
