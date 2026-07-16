package br.com.ale.service.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("local")
public class LogEmailService implements EmailService {

  private static final Logger log = LoggerFactory.getLogger(LogEmailService.class);

  @Override
  public void send(String to, String subject, String htmlBody) {
    log.info("[LOCAL EMAIL] To: {} | Subject: {}", to, subject);
  }
}
