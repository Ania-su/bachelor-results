package school.hei.demo.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.demo.entity.Graduate;

@Service
@AllArgsConstructor
public class PromotionService {

  private final GraduateComputationService graduateComputationService;

  public List<Graduate> listGraduates(int academicYear) {
    return graduateComputationService.computeGraduates(academicYear);
  }
}
