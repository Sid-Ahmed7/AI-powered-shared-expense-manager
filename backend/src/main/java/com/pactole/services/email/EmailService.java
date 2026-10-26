package com.pactole.services.email;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.pactole.common.Messages;
import com.pactole.constants.CommonConstants;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final Messages messages;

    public EmailService(JavaMailSender mailSender,
                        @Value("${spring.mail.username}") String fromAddress,
                        Messages messages) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.messages = messages;
    }

    public void sendPasswordReset(String to, String name, String link) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, CommonConstants.ENCODING);
            helper.setFrom(fromAddress, "Pactole");
            helper.setTo(to);
            helper.setSubject(messages.get("email.reset.subject"));
            helper.setText(buildHtmlEmail(name, link), true);
            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send reset email to {}", to, e);
        }
    }

    private String buildHtmlEmail(String name, String link) {
        String heading    = messages.get("email.reset.heading");
        String greeting   = messages.get("email.reset.greeting", name);
        String body       = messages.get("email.reset.body", CommonConstants.TTL_HOURS);
        String button     = messages.get("email.reset.button");
        String disclaimer = messages.get("email.reset.disclaimer");
        String altLabel   = messages.get("email.reset.alt.label");
        String footer     = messages.get("email.reset.footer");

        return """
            <!DOCTYPE html>
            <html lang="fr">
            <body style="margin:0;padding:0;background:#f5f5f5;font-family:system-ui,-apple-system,sans-serif">
              <table width="100%%" cellpadding="0" cellspacing="0">
                <tr><td align="center" style="padding:40px 16px">
                  <table width="520" cellpadding="0" cellspacing="0"
                         style="background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 2px 16px rgba(0,0,0,.08)">
                    <tr><td style="background:linear-gradient(135deg,#F97316,#ea580c);padding:32px;text-align:center">
                      <span style="font-size:28px;font-weight:900;color:#fff;letter-spacing:-.03em">🔥 Pactole</span>
                    </td></tr>
                    <tr><td style="padding:36px 40px">
                      <h1 style="margin:0 0 12px;font-size:22px;font-weight:800;color:#1a1208">%s</h1>
                      <p style="margin:0 0 24px;color:#4a3f35;line-height:1.6">%s<br><br>%s</p>
                      <div style="text-align:center;margin:32px 0">
                        <a href="%s"
                           style="display:inline-block;background:#F97316;color:#fff;font-weight:700;
                                  font-size:15px;padding:14px 32px;border-radius:10px;text-decoration:none;
                                  box-shadow:0 4px 14px rgba(249,115,22,.4)">%s</a>
                      </div>
                      <p style="margin:0;color:#8c7b6e;font-size:13px;line-height:1.5">
                        %s<br><br>%s <a href="%s" style="color:#F97316">%s</a>
                      </p>
                    </td></tr>
                    <tr><td style="background:#faf8f5;padding:20px 40px;text-align:center">
                      <p style="margin:0;color:#8c7b6e;font-size:12px">%s</p>
                    </td></tr>
                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(heading, greeting, body, link, button, disclaimer, altLabel, link, link, footer);
    }
}
