package school.hei.demo.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.demo.domain.dto.response.GraduateResponse;
import school.hei.demo.domain.mappers.GraduateMapper;

@Service
@AllArgsConstructor
public class PromotionService {

  private final GraduateComputationService graduateComputationService;
  private final GraduateMapper mapper;

  public List<GraduateResponse> listGraduates(int academicYear) {
    return graduateComputationService.computeGraduates(academicYear).stream()
        .map(mapper::toResponse)
        .toList();
  }
}
