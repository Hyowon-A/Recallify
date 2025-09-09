package com.andy.recallify.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailerConfig {
    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername("andy.hw.ahn@gmail.com"); // must create a new email
        mailSender.setPassword("zhprqdzpsulsvodh");      // move this to somewhere private

        Properties p = mailSender.getJavaMailProperties();
        p.put("mail.transport.protocol", "smtp");          // NOT "smtps"
        p.put("mail.smtp.auth", "true");
        p.put("mail.smtp.starttls.enable", "true");
        p.put("mail.smtp.starttls.required", "true");
        p.put("mail.debug", "false");                      // set true to debug
        return mailSender;
    }
}
