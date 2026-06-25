package br.com.ale.service.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
public class EmailService {

    private final SesClient sesClient;
    private final String fromEmail;

    public EmailService(@Value("${aws.ses.from}") String fromEmail) {
        this.sesClient = SesClient.create();
        this.fromEmail = fromEmail;
    }

    public void send(String to, String subject, String htmlBody) {

        try {

            SendEmailRequest request = SendEmailRequest.builder()
                    .source(fromEmail)
                    .destination(Destination.builder().toAddresses(to).build())
                    .message(Message.builder()
                            .subject(Content.builder().data(subject).build())
                            .body(Body.builder()
                                    .html(Content.builder().data(htmlBody).build())
                                    .build())
                            .build())
                    .build();

            sesClient.sendEmail(request);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error sending email [to=" + to + "]",
                    e
            );
        }
    }
}
