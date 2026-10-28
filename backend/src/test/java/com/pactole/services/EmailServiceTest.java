package com.pactole.services;

import com.pactole.common.Messages;
import com.pactole.services.email.EmailService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock JavaMailSender mailSender;
    @Mock Messages       messages;
    @Mock MimeMessage    mimeMessage;

    EmailService service;

    @BeforeEach
    void setUp() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        lenient().when(messages.get(anyString())).thenReturn("");
        lenient().when(messages.get(anyString(), (Object) any())).thenReturn("");
        service = new EmailService(mailSender, "from@test.com", messages);
    }

    @Test
    void sendPasswordReset_callsMailSenderSend() {
        service.sendPasswordReset("to@test.com", "Alice", "http://localhost/reset?token=abc");
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendPasswordReset_resolvesSubjectKey() {
        service.sendPasswordReset("to@test.com", "Alice", "http://localhost/reset?token=abc");
        verify(messages).get("email.reset.subject");
    }

    @Test
    void sendPasswordReset_passesNameToGreeting() {
        service.sendPasswordReset("to@test.com", "Bob", "http://localhost/reset?token=xyz");
        verify(messages).get("email.reset.greeting", "Bob");
    }

    @Test
    void sendPasswordReset_messagingException_doesNotPropagate() throws Exception {
        doThrow(new MessagingException("SMTP error")).when(mimeMessage).setFrom(any(Address.class));
        assertThatNoException().isThrownBy(() -> service.sendPasswordReset("to@test.com", "Alice", "http://localhost/reset"));
    }
}
