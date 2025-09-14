package com.andy.recallify.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailerConfig {
    @Bean
    public JavaMailSender mailSender(
            @Value("${SPRING_MAIL_HOST}") String host,
            @Value("${SPRING_MAIL_PORT}") int port,
            @Value("${SPRING_MAIL_USERNAME}") String user,
            @Value("${SPRING_MAIL_PASSWORD}") String pass
    ) {
        var ms = new JavaMailSenderImpl();
        ms.setHost(host); ms.setPort(port);
        ms.setUsername(user); ms.setPassword(pass);
        var p = ms.getJavaMailProperties();
        p.put("mail.smtp.auth","true");
        p.put("mail.smtp.starttls.enable","true");
        return ms;
    }
}
