package br.com.ale.support;

import br.com.ale.service.email.EmailService;
import java.util.ArrayList;
import java.util.List;

/** EmailService fake que grava os envios em memória. */
public class RecordingEmailService implements EmailService {

  public record Sent(String to, String subject, String html) {}

  public final List<Sent> sent = new ArrayList<>();
  public boolean fail = false;

  @Override
  public void send(String to, String subject, String htmlBody) {
    if (fail) {
      throw new RuntimeException("smtp down");
    }
    sent.add(new Sent(to, subject, htmlBody));
  }
}
