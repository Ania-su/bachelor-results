package school.hei.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.hei.demo.domain.dto.response.GraduateResponse;
import school.hei.demo.domain.dto.response.PromotionDownloadResponse;
import school.hei.demo.file.bucket.BucketComponent;

@ExtendWith(MockitoExtension.class)
class PromotionExcelServiceTest {
  @Mock PromotionService promotionService;
  @Mock BucketComponent bucketComponent;
  @InjectMocks PromotionExcelService service;

  @Test
  void generateGraduatesDownload_writesWorkbookAndReturnsUrl() throws Exception {
    GraduateResponse g =
        GraduateResponse.builder()
            .rank(1)
            .reference("REF1")
            .firstName("Jean")
            .lastName("Rakoto")
            .average(new BigDecimal("13.50"))
            .build();
    when(promotionService.listGraduates(2024)).thenReturn(List.of(g));
    when(bucketComponent.presign(any(String.class), any(Duration.class)))
        .thenReturn(new URL("https://bucket.s3.amazonaws.com/promotions/2024/graduates.xlsx"));

    PromotionDownloadResponse response = service.generateGraduatesDownload(2024);

    assertEquals(
        "https://bucket.s3.amazonaws.com/promotions/2024/graduates.xlsx",
        response.getDownloadUrl());

    ArgumentCaptor<File> fileCaptor = ArgumentCaptor.forClass(File.class);
    ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
    verify(bucketComponent).upload(fileCaptor.capture(), keyCaptor.capture());
    verify(bucketComponent)
        .presign(eq("promotions/2024/graduates.xlsx"), eq(Duration.ofMinutes(10)));

    assertEquals("promotions/2024/graduates.xlsx", keyCaptor.getValue());
    File uploaded = fileCaptor.getValue();
    try {
      byte[] header = Files.readAllBytes(uploaded.toPath());
      assertTrue(header.length > 0);
      assertEquals((byte) 'P', header[0]);
      assertEquals((byte) 'K', header[1]);
    } finally {
      uploaded.delete();
    }
  }

  @Test
  void generateGraduatesDownload_handlesEmptyList() throws Exception {
    when(promotionService.listGraduates(2024)).thenReturn(List.of());
    when(bucketComponent.presign(any(String.class), any(Duration.class)))
        .thenReturn(new URL("https://bucket.s3.amazonaws.com/promotions/2024/graduates.xlsx"));

    PromotionDownloadResponse response = service.generateGraduatesDownload(2024);

    assertEquals(
        "https://bucket.s3.amazonaws.com/promotions/2024/graduates.xlsx",
        response.getDownloadUrl());
  }
}
