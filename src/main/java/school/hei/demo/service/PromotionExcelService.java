package school.hei.demo.service;

import static java.io.File.createTempFile;

import java.io.File;
import java.io.FileOutputStream;
import java.time.Duration;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import school.hei.demo.domain.dto.response.GraduateResponse;
import school.hei.demo.domain.dto.response.PromotionDownloadResponse;
import school.hei.demo.file.bucket.BucketComponent;

@Service
@AllArgsConstructor
public class PromotionExcelService {

  private final PromotionService promotionService;
  private final BucketComponent bucketComponent;

  @SneakyThrows
  public PromotionDownloadResponse generateGraduatesDownload(int academicYear) {
    List<GraduateResponse> graduates = promotionService.listGraduates(academicYear);

    File file = createTempFile("graduates-" + academicYear, ".xlsx");
    try (Workbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Diplomes " + academicYear);

      Row header = sheet.createRow(0);
      header.createCell(0).setCellValue("Rang");
      header.createCell(1).setCellValue("Référence");
      header.createCell(2).setCellValue("Prénom");
      header.createCell(3).setCellValue("Nom");
      header.createCell(4).setCellValue("Moyenne générale");

      int rowIndex = 1;
      for (GraduateResponse graduate : graduates) {
        Row row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue(graduate.getRank());
        row.createCell(1).setCellValue(graduate.getReference());
        row.createCell(2).setCellValue(graduate.getFirstName());
        row.createCell(3).setCellValue(graduate.getLastName());
        row.createCell(4).setCellValue(graduate.getAverage().doubleValue());
      }

      for (int i = 0; i < 5; i++) {
        sheet.autoSizeColumn(i);
      }

      try (FileOutputStream out = new FileOutputStream(file)) {
        workbook.write(out);
      }
    }

    String bucketKey = "promotions/" + academicYear + "/graduates.xlsx";
    bucketComponent.upload(file, bucketKey);
    String downloadUrl = bucketComponent.presign(bucketKey, Duration.ofMinutes(10)).toString();
    return PromotionDownloadResponse.builder().downloadUrl(downloadUrl).build();
  }
}
