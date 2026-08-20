package school.hei.demo.endpoint.web;

import java.io.File;
import lombok.AllArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import school.hei.demo.enums.UserRole;
import school.hei.demo.repository.UserRepository;
import school.hei.demo.service.PromotionExcelService;

@Controller
@AllArgsConstructor
public class PromotionViewController {

  private final UserRepository userRepository;
  private final PromotionExcelService promotionExcelService;

  @GetMapping("/web/promotions")
  public String listPromotions(Model model) {
    model.addAttribute("years", userRepository.findDistinctStudentEntryYears(UserRole.STUDENT));
    return "promotions";
  }

  @GetMapping("/web/promotions/{academicYear}/download")
  public ResponseEntity<Resource> downloadGraduates(@PathVariable int academicYear) {

    File file = promotionExcelService.generateGraduatesFile(academicYear);

    Resource resource = new FileSystemResource(file);

    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment()
                .filename("graduates-" + academicYear + ".xlsx")
                .build()
                .toString())
        .contentType(
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .contentLength(file.length())
        .body(resource);
  }
}
