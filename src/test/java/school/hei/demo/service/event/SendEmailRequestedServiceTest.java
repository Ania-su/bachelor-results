package school.hei.demo.service.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.hei.demo.endpoint.event.model.SendEmailRequested;
import school.hei.demo.mail.Email;
import school.hei.demo.mail.Mailer;

@ExtendWith(MockitoExtension.class)
class SendEmailRequestedServiceTest {

  @Mock Mailer mailer;
  @InjectMocks SendEmailRequestedService service;

  @Test
  void accept_sendsEmailWithRecipientSubjectAndHtmlBody() {
    var event =
        SendEmailRequested.builder()
            .to("student@hei.mg")
            .subject("STD24001 transcript")
            .body("<p>Hi</p><a href=\"https://url\">Link</a>")
            .build();

    service.accept(event);

    var captor = ArgumentCaptor.forClass(Email.class);
    verify(mailer).accept(captor.capture());
    var email = captor.getValue();
    assertEquals("student@hei.mg", email.to().getAddress());
    assertEquals("STD24001 transcript", email.subject());
    assertEquals("<p>Hi</p><a href=\"https://url\">Link</a>", email.htmlBody());
    assertTrue(email.attachments().isEmpty());
  }
}
