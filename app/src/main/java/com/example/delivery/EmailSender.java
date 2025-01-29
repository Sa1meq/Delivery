package com.example.delivery;

import android.util.Log;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {

    private static final String SMTP_HOST = "smtp.gmail.com"; // SMTP-сервер (например, Gmail)
    private static final String SMTP_PORT = "587"; // Порт для TLS
    private static final String EMAIL_FROM = "work.appontheway@gmail.com"; // Ваш email
    private static final String EMAIL_PASSWORD = "iqnn hhaq chst wzai"; // Пароль от email

    public static void sendEmail(String toEmail, String subject, String body) {
        new Thread(() -> {
            try {
                // Настройка свойств для SMTP
                Properties properties = new Properties();
                properties.put("mail.smtp.host", SMTP_HOST);
                properties.put("mail.smtp.port", SMTP_PORT);
                properties.put("mail.smtp.auth", "true");
                properties.put("mail.smtp.starttls.enable", "true");

                // Создание сессии с аутентификацией
                Session session = Session.getInstance(properties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                    }
                });

                // Создание сообщения
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(EMAIL_FROM));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                message.setSubject(subject);
                message.setText(body);

                // Отправка сообщения
                Transport.send(message);
                Log.d("EmailSender", "Письмо успешно отправлено на " + toEmail);
            } catch (MessagingException e) {
                Log.e("EmailSender", "Ошибка отправки письма: " + e.getMessage());
            }
        }).start();
    }
}