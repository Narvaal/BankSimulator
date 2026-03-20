package br.com.ale.service.email;

import org.springframework.stereotype.Service;

@Service
public class EmailTemplateService {

    public String buildVerifyEmailTemplate(String name, String link) {

        return """
                <div style="font-family: Arial; max-width: 600px; margin: auto;">
                    <h2>Confirme sua conta</h2>
                
                    <p>Olá, %s 👋</p>
                
                    <p>Clique no botão abaixo para confirmar seu e-mail:</p>
                
                    <a href="%s"
                       style="display: inline-block;
                              padding: 12px 20px;
                              background-color: #4CAF50;
                              color: white;
                              text-decoration: none;
                              border-radius: 5px;">
                        Confirmar Email
                    </a>
                
                    <p style="margin-top:20px;">
                        Se você não criou essa conta, ignore este e-mail.
                    </p>
                </div>
                """.formatted(name, link);
    }
}