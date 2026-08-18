package school.hei.demo.endpoint.rest.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.hei.demo.domain.dto.response.GraduateResponse;
import school.hei.demo.domain.dto.response.GroupResponse;
import school.hei.demo.domain.dto.response.PromotionDownloadResponse;
import school.hei.demo.endpoint.rest.controller.mapper.GraduateRestMapper;
import school.hei.demo.endpoint.rest.controller.mapper.GroupRestMapper;
import school.hei.demo.service.GroupService;
import school.hei.demo.service.PromotionExcelService;
import school.hei.demo.service.PromotionService;

@RestController
@RequestMapping("/promotions")
@AllArgsConstructor
public class PromotionController {

  private final GroupService groupService;
  private final GroupRestMapper groupMapper;
  private final PromotionService promotionService;
  private final PromotionExcelService promotionExcelService;
  private final GraduateRestMapper graduateMapper;

  @GetMapping
  public List<GroupResponse> listAllPromotionGroups() {
    var body = groupService.listAll().stream().map(groupMapper::toResponse).toList();
    return body;
  }

  @GetMapping("/{academicYear}/graduates")
  public List<GraduateResponse> listGraduates(@PathVariable int academicYear) {
    var body =
        promotionService.listGraduates(academicYear).stream()
            .map(graduateMapper::toResponse)
            .toList();
    return body;
  }

  @GetMapping("/{academicYear}/graduates/download")
  public PromotionDownloadResponse downloadGraduates(@PathVariable int academicYear) {
    var url = promotionExcelService.generateGraduatesDownloadUrl(academicYear);
    return PromotionDownloadResponse.builder().downloadUrl(url).build();
  }
}
