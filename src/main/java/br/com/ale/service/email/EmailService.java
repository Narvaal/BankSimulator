package br.com.ale.service.email;

public interface EmailService {
    void send(String to, String subject, String htmlBody);
}
