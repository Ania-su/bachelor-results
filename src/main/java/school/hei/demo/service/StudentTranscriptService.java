package school.hei.demo.service;

import static java.io.File.createTempFile;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.File;
import java.io.FileOutputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.demo.domain.dto.response.CourseAverageResponse;
import school.hei.demo.domain.dto.response.StudentTranscriptResponse;
import school.hei.demo.domain.dto.response.User;
import school.hei.demo.domain.dto.response.YearTranscriptResponse;
import school.hei.demo.domain.mappers.UserMapper;
import school.hei.demo.endpoint.event.EventProducer;
import school.hei.demo.endpoint.event.model.SendEmailRequested;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.file.bucket.BucketComponent;
import school.hei.demo.repository.GradeRepository;
import school.hei.demo.repository.UserRepository;

@Service
@AllArgsConstructor
public class StudentTranscriptService {
  private final UserRepository userRepository;
  private final GradeRepository gradeRepository;
  private final UserMapper userMapper;
  private final StudentCourseGradeService studentCourseGradeService;
  private final BucketComponent bucketComponent;
  private final TranscriptHtmlService transcriptHtmlService;
  private final EventProducer<SendEmailRequested> eventProducer;

  @Transactional(readOnly = true)
  public StudentTranscriptResponse get(UUID studentId, Integer year) {
    var user =
        userRepository
            .findById(studentId)
            .map(userMapper::toDomain)
            .orElseThrow(() -> new NotFoundException("Student " + studentId + " not found"));

    var courseData =
        gradeRepository.findCoursesWithGrades(studentId).stream()
            .map(
                course -> {
                  var courseGrade = studentCourseGradeService.get(course.getId(), studentId);
                  var hasAllGrades =
                      !courseGrade.getExams().isEmpty()
                          && gradeRepository.countByStudent_IdAndExam_Course_Id(
                                  studentId, course.getId())
                              == courseGrade.getExams().size();
                  return new CourseTranscriptData(
                      new CourseAverageResponse(
                          courseGrade.getCourse().getId(),
                          courseGrade.getCourse().getReference(),
                          courseGrade.getCourse().getTitle(),
                          courseGrade.getCourse().getSemester(),
                          courseGrade.getCourse().getCredits(),
                          courseGrade.getAverage()),
                      hasAllGrades);
                })
            .toList();

    var courses =
        courseData.stream()
            .filter(data -> year == null || (data.course().getSemester() + 1) / 2 == year)
            .map(CourseTranscriptData::course)
            .sorted(Comparator.comparing(CourseAverageResponse::getSemester))
            .toList();

    var years =
        courses.stream()
            .collect(Collectors.groupingBy(course -> (course.getSemester() + 1) / 2))
            .entrySet()
            .stream()
            .sorted(java.util.Map.Entry.comparingByKey())
            .map(
                entry -> {
                  var yearCourses = entry.getValue();
                  var totalCredits =
                      yearCourses.stream().mapToInt(CourseAverageResponse::getCredits).sum();
                  var weightedAverage =
                      totalCredits == 0
                          ? 0.0
                          : yearCourses.stream()
                                  .mapToDouble(course -> course.getAverage() * course.getCredits())
                                  .sum()
                              / totalCredits;
                  return new YearTranscriptResponse(entry.getKey(), yearCourses, weightedAverage);
                })
            .toList();

    var isOfficial =
        !courses.isEmpty()
            && courseData.stream()
                .filter(data -> year == null || (data.course().getSemester() + 1) / 2 == year)
                .allMatch(CourseTranscriptData::hasAllGrades);

    var totalCredits = courses.stream().mapToInt(CourseAverageResponse::getCredits).sum();
    var validatedCredits =
        courses.stream()
            .filter(course -> course.getAverage() >= 10)
            .mapToInt(CourseAverageResponse::getCredits)
            .sum();

    return new StudentTranscriptResponse(
        toResponse(user), years, isOfficial, totalCredits, validatedCredits);
  }

  @SneakyThrows
  public String sendTranscriptByEmail(UUID studentId, Integer year) {
    var transcript = get(studentId, year);

    var html = transcriptHtmlService.toHtml(transcript);
    var fileSuffix = ".pdf";
    var filePrefix = "transcript-" + studentId;
    var bucketKey = "transcripts/" + studentId + fileSuffix;
    var fileToUpload = createTempFile(filePrefix, fileSuffix);
    writeTranscriptIntoFile(html, fileToUpload);

    bucketComponent.upload(fileToUpload, bucketKey);
    var presignedUrl = bucketComponent.presign(bucketKey, Duration.ofMinutes(10)).toString();

    var emailBody = createEmailBody(transcript.getUser(), presignedUrl);

    var event =
        SendEmailRequested.builder()
            .to(transcript.getUser().getEmail())
            .body(emailBody)
            .subject(transcript.getUser().getReference() + " transcript")
            .build();
    eventProducer.accept(List.of(event));

    return "Check your email for your requested transcript.";
  }

  @SneakyThrows
  private void writeTranscriptIntoFile(String html, File file) {
    var builder = new PdfRendererBuilder();
    builder.useFastMode();
    builder.withHtmlContent(html, null);
    try (var outputStream = new FileOutputStream(file)) {
      builder.toStream(outputStream);
      builder.run();
    }
  }

  private String createEmailBody(User user, String url) {
    var emailBody = new StringBuilder();

    emailBody
        .append("<p>Hi, " + transcriptHtmlService.escape(user.getLastName()) + "</p>")
        .append(
            "<p>Please find below the link to the transcript you requested on "
                + LocalDate.now()
                + "</p>")
        .append("<a href=\"" + url + "\">Link to transcript</a>")
        .append("<p>This link will expire in 10 minutes</p>")
        .append("<p>Best regards</p>");

    return emailBody.toString();
  }

  private record CourseTranscriptData(CourseAverageResponse course, boolean hasAllGrades) {}

  private User toResponse(school.hei.demo.entity.User user) {
    return new User(
        user.getId(),
        user.getReference(),
        user.getFirstName(),
        user.getLastName(),
        user.getEmail(),
        user.getUserRole(),
        user.getSpecialtyId(),
        user.getEntryYear(),
        user.getCreatedAt());
  }
}
