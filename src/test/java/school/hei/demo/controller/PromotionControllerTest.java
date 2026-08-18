package school.hei.demo.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import school.hei.demo.domain.dto.response.GraduateResponse;
import school.hei.demo.domain.dto.response.GroupResponse;
import school.hei.demo.domain.dto.response.PromotionDownloadResponse;
import school.hei.demo.endpoint.rest.controller.PromotionController;
import school.hei.demo.exception.GlobalExceptionHandler;
import school.hei.demo.service.GroupService;
import school.hei.demo.service.PromotionExcelService;
import school.hei.demo.service.PromotionService;

@ExtendWith(MockitoExtension.class)
class PromotionControllerTest {
  @Mock GroupService groupService;
  @Mock PromotionService promotionService;
  @Mock PromotionExcelService promotionExcelService;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(
                new PromotionController(groupService, promotionService, promotionExcelService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  void listAllPromotionGroups_returnsGroups() throws Exception {
    GroupResponse group = new GroupResponse(UUID.randomUUID(), "G1", 2024);
    when(groupService.listAll()).thenReturn(List.of(group));

    mockMvc
        .perform(get("/promotions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].reference").value("G1"));
  }

  @Test
  void listGraduates_returnsGraduates() throws Exception {
    GraduateResponse graduate =
        GraduateResponse.builder()
            .rank(1)
            .reference("REF1")
            .firstName("Jean")
            .lastName("Rakoto")
            .average(new BigDecimal("13.50"))
            .build();
    when(promotionService.listGraduates(2024)).thenReturn(List.of(graduate));

    mockMvc
        .perform(get("/promotions/{academicYear}/graduates", 2024))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].rank").value(1))
        .andExpect(jsonPath("$[0].average").value(13.50));
  }

  @Test
  void downloadGraduates_returnsUrl() throws Exception {
    PromotionDownloadResponse response =
        PromotionDownloadResponse.builder()
            .downloadUrl("https://bucket.s3.amazonaws.com/promotions/2024/graduates.xlsx")
            .build();
    when(promotionExcelService.generateGraduatesDownload(2024)).thenReturn(response);

    mockMvc
        .perform(get("/promotions/{academicYear}/graduates/download", 2024))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.downloadUrl")
                .value("https://bucket.s3.amazonaws.com/promotions/2024/graduates.xlsx"));
  }
}
