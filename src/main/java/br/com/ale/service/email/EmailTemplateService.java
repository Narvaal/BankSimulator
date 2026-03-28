package br.com.ale.service.email;

import org.springframework.stereotype.Service;

@Service
public class EmailTemplateService {

    public String buildVerifyEmailTemplate(String name, String link) {

        return """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; color: #1f2937;">
                
                    <h2 style="color: #111827;">Confirm your email address</h2>
                
                    <p>Hello, %s 👋</p>
                
                    <p>
                        Thank you for signing up. Please confirm your email address by clicking the button below:
                    </p>
                
                    <a href="%s"
                       style="display: inline-block;
                              margin-top: 10px;
                              padding: 12px 20px;
                              background-color: #16a34a;
                              color: white;
                              text-decoration: none;
                              border-radius: 6px;
                              font-weight: 500;">
                        Verify Email
                    </a>
                
                    <p style="margin-top: 20px;">
                        If you did not create an account, you can safely ignore this email.
                    </p>
                
                    <p style="margin-top: 30px; font-size: 12px; color: #6b7280;">
                        This link may expire for security reasons.
                    </p>
                
                </div>
                """.formatted(name, link);
    }

    public String buildResetPasswordTemplate(String name, String link) {

        return """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; color: #1f2937;">
                
                    <h2 style="color: #111827;">Reset your password</h2>
                
                    <p>Hello, %s 👋</p>
                
                    <p>
                        We received a request to reset your password. Click the button below to create a new one:
                    </p>
                
                    <a href="%s"
                       style="display: inline-block;
                              margin-top: 10px;
                              padding: 12px 20px;
                              background-color: #111827;
                              color: white;
                              text-decoration: none;
                              border-radius: 6px;
                              font-weight: 500;">
                        Reset Password
                    </a>
                
                    <p style="margin-top: 20px;">
                        If you did not request a password reset, you can safely ignore this email.
                    </p>
                
                    <p style="margin-top: 30px; font-size: 12px; color: #6b7280;">
                        This link will expire for security reasons.
                    </p>
                
                </div>
                """.formatted(name, link);
    }
}