package school.hei.demo.endpoint.rest.controller;

import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.demo.domain.dto.response.StudentTranscriptResponse;
import school.hei.demo.service.StudentTranscriptService;

@RestController
@RequestMapping("/students/{studentId}/transcript")
@AllArgsConstructor
public class StudentTranscriptController {
  private final StudentTranscriptService service;

  @GetMapping
  public StudentTranscriptResponse getTranscript(@PathVariable UUID studentId) {
    return service.get(studentId);
  }
}
