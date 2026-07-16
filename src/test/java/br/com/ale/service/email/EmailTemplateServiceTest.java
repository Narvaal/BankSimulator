package br.com.ale.service.email;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EmailTemplateServiceTest {

  private final EmailTemplateService service = new EmailTemplateService();

  @Test
  void verifyTemplateShouldEmbedNameAndLink() {
    String html = service.buildVerifyEmailTemplate("John", "https://x/verify?token=1");
    assertTrue(html.contains("John"));
    assertTrue(html.contains("https://x/verify?token=1"));
  }

  @Test
  void resetTemplateShouldEmbedNameAndLink() {
    String html = service.buildResetPasswordTemplate("Jane", "https://x/reset?token=2");
    assertTrue(html.contains("Jane"));
    assertTrue(html.contains("https://x/reset?token=2"));
  }

  @Test
  void logEmailServiceShouldNotThrow() {
    new LogEmailService().send("to@test.com", "subject", "<p>hi</p>");
  }
}
