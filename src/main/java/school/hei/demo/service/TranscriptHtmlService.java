package school.hei.demo.service;

import java.util.Locale;
import org.springframework.stereotype.Service;
import school.hei.demo.domain.dto.response.CourseAverageResponse;
import school.hei.demo.domain.dto.response.StudentTranscriptResponse;
import school.hei.demo.domain.dto.response.YearTranscriptResponse;

@Service
public class TranscriptHtmlService {

  public String toHtml(StudentTranscriptResponse transcript) {
    var user = transcript.getUser();
    var title = "Relevé de notes — " + user.getFirstName() + " " + user.getLastName();

    var html = new StringBuilder();
    html.append("<!DOCTYPE html>")
        .append("<html><head><meta charset=\"UTF-8\"/><title>")
        .append(escape(title))
        .append("</title><style>")
        .append(css())
        .append("</style></head><body>");

    html.append("<div class=\"header\">")
        .append("<h1>Relevé de notes</h1>")
        .append("<p class=\"student\">")
        .append(escape(user.getFirstName()))
        .append(" ")
        .append(escape(user.getLastName()))
        .append("</p>")
        .append("<p class=\"meta\">Référence : ")
        .append(escape(user.getReference()))
        .append("</p>")
        .append("<p class=\"meta\">Email : ")
        .append(escape(user.getEmail()))
        .append("</p>")
        .append("<p class=\"meta\">Année d'entrée : ")
        .append(user.getEntryYear())
        .append("</p>")
        .append("</div>");

    html.append("<div class=\"summary\">")
        .append(summaryCard("Crédits totaux", String.valueOf(transcript.getTotalCredits())))
        .append(summaryCard("Crédits validés", String.valueOf(transcript.getValidatedCredits())))
        .append(
            summaryCard(
                "Statut",
                Boolean.TRUE.equals(transcript.getIsOfficial()) ? "Officiel" : "Non officiel"))
        .append("</div>");

    for (YearTranscriptResponse year : transcript.getYears()) {
      html.append("<div class=\"year\">")
          .append("<h2>Année ")
          .append(year.getYear())
          .append("</h2>")
          .append("<table>")
          .append("<thead><tr>")
          .append("<th>Référence</th>")
          .append("<th>Intitulé</th>")
          .append("<th>Semestre</th>")
          .append("<th>Crédits</th>")
          .append("<th>Moyenne</th>")
          .append("</tr></thead><tbody>");

      for (CourseAverageResponse course : year.getCourses()) {
        html.append("<tr>")
            .append("<td>")
            .append(escape(course.getReference()))
            .append("</td>")
            .append("<td>")
            .append(escape(course.getTitle()))
            .append("</td>")
            .append("<td class=\"center\">")
            .append(course.getSemester())
            .append("</td>")
            .append("<td class=\"center\">")
            .append(course.getCredits())
            .append("</td>")
            .append("<td class=\"center\">")
            .append(formatAverage(course.getAverage()))
            .append("</td>")
            .append("</tr>");
      }

      html.append("</tbody></table>")
          .append("<p class=\"year-average\">Moyenne annuelle : ")
          .append(formatAverage(year.getAverageYear()))
          .append("</p>")
          .append("</div>");
    }

    html.append("</body></html>");
    return html.toString();
  }

  private String summaryCard(String label, String value) {
    return "<div class=\"card\"><span class=\"card-label\">"
        + escape(label)
        + "</span><span class=\"card-value\">"
        + escape(value)
        + "</span></div>";
  }

  private String formatAverage(Double average) {
    return average == null ? "—" : String.format(Locale.FRENCH, "%.2f", average);
  }

  public String escape(String value) {
    if (value == null) {
      return "";
    }
    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }

  private String css() {
    return """
           @page { size: A4; margin: 2cm; }
           * { box-sizing: border-box; }
           body {
             font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
             color: #1f2937;
             margin: 0;
           }
           .header {
             border-bottom: 3px solid #1d4ed8;
             padding-bottom: 16px;
             margin-bottom: 24px;
           }
           .header h1 {
             margin: 0 0 8px;
             font-size: 28px;
             color: #1d4ed8;
           }
           .header .student {
             font-size: 20px;
             font-weight: bold;
             margin: 0 0 4px;
           }
           .header .meta {
             margin: 2px 0;
             color: #4b5563;
           }
           .summary {
             display: flex;
             gap: 16px;
             margin-bottom: 32px;
           }
           .card {
             flex: 1;
             border: 1px solid #e5e7eb;
             border-radius: 8px;
             padding: 16px;
             text-align: center;
           }
           .card-label {
             display: block;
             font-size: 12px;
             text-transform: uppercase;
             letter-spacing: 1px;
             color: #6b7280;
             margin-bottom: 8px;
           }
           .card-value {
             font-size: 20px;
             font-weight: bold;
             color: #1d4ed8;
           }
           .year {
             margin-bottom: 32px;
           }
           .year h2 {
             font-size: 20px;
             color: #111827;
             border-left: 4px solid #1d4ed8;
             padding-left: 12px;
             margin: 0 0 12px;
           }
           table {
             width: 100%;
             border-collapse: collapse;
             font-size: 14px;
           }
           th, td {
             padding: 8px 10px;
             text-align: left;
             border-bottom: 1px solid #e5e7eb;
           }
           th {
             background: #f3f4f6;
             color: #374151;
             text-transform: uppercase;
             font-size: 12px;
             letter-spacing: 1px;
           }
           tr:nth-child(even) td {
             background: #fafafa;
           }
           .center {
             text-align: center;
           }
           .year-average {
             text-align: right;
             font-weight: bold;
             margin-top: 8px;
             color: #1d4ed8;
           }
           """;
  }
}
