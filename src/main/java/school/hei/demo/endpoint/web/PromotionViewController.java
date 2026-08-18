package school.hei.demo.endpoint.web;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import school.hei.demo.repository.UserRepository;
import school.hei.demo.service.PromotionExcelService;

@Controller
@AllArgsConstructor
public class PromotionViewController {

  private final UserRepository userRepository;
  private final PromotionExcelService promotionExcelService;

  @GetMapping("/web/promotions")
  public String listPromotions(Model model) {
    model.addAttribute("years", userRepository.findDistinctStudentEntryYears());
    return "promotions";
  }

  @GetMapping("/web/promotions/{academicYear}/download")
  public String downloadGraduates(@PathVariable int academicYear) {
    String url = promotionExcelService.generateGraduatesDownloadUrl(academicYear);
    return "redirect:" + url;
  }
}
