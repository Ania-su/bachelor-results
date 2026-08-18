package school.hei.demo.service.event;

import jakarta.mail.internet.InternetAddress;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import school.hei.demo.endpoint.event.model.SendEmailRequested;
import school.hei.demo.mail.Email;
import school.hei.demo.mail.Mailer;

@Service
@AllArgsConstructor
public class SendEmailRequestedService implements Consumer<SendEmailRequested> {
  private final Mailer mailer;

  @SneakyThrows
  @Override
  public void accept(SendEmailRequested sendEmailRequested) {
    var recipientAddress = new InternetAddress(sendEmailRequested.getTo());
    mailer.accept(
        new Email(
            recipientAddress,
            List.of(),
            List.of(),
            sendEmailRequested.getSubject(),
            sendEmailRequested.getBody(),
            List.of()));
  }
}
