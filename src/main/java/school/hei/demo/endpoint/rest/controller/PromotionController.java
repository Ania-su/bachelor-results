package school.hei.demo.endpoint.rest.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.hei.demo.domain.dto.response.GraduateResponse;
import school.hei.demo.domain.dto.response.GroupResponse;
import school.hei.demo.domain.dto.response.PromotionDownloadResponse;
import school.hei.demo.service.GroupService;
import school.hei.demo.service.PromotionExcelService;
import school.hei.demo.service.PromotionService;

@RestController
@RequestMapping("/promotions")
@AllArgsConstructor
public class PromotionController {

  private final GroupService groupService;
  private final PromotionService promotionService;
  private final PromotionExcelService promotionExcelService;

  @GetMapping
  public List<GroupResponse> listAllPromotionGroups() {
    return groupService.listAll();
  }

  @GetMapping("/{academicYear}/graduates")
  public List<GraduateResponse> listGraduates(@PathVariable int academicYear) {
    return promotionService.listGraduates(academicYear);
  }

  @GetMapping("/{academicYear}/graduates/download")
  public PromotionDownloadResponse downloadGraduates(@PathVariable int academicYear) {
    return promotionExcelService.generateGraduatesDownload(academicYear);
  }
}
