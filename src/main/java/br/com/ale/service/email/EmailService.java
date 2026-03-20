package br.com.ale.service.email;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
public class EmailService {

    private final SesClient sesClient;

    public EmailService() {
        this.sesClient = SesClient.builder()
                .region(Region.US_EAST_2)
                .build();
    }

    public void send(String to, String subject, String htmlBody) {

        try {

            Destination destination = Destination.builder()
                    .toAddresses(to)
                    .build();

            Content subjectContent = Content.builder()
                    .data(subject)
                    .build();

            Content htmlContent = Content.builder()
                    .data(htmlBody)
                    .build();

            Body body = Body.builder()
                    .html(htmlContent)
                    .build();

            Message message = Message.builder()
                    .subject(subjectContent)
                    .body(body)
                    .build();

            String fromEmail = "no-reply@alessandro-bezerra.me";
            SendEmailRequest request = SendEmailRequest.builder()
                    .source(fromEmail)
                    .destination(destination)
                    .message(message)
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