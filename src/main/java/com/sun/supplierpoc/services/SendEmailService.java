package com.sun.supplierpoc.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.*;
import java.util.Date;

@Service
public class SendEmailService {
    private JavaMailSender mailSender;

    @Autowired
    public SendEmailService(JavaMailSender mailSender){
        this.mailSender = mailSender;
    }

    public void sendSimpleMail() throws MailException {
        String messageBody = "Thanks for being with us.";
        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setTo("lyoussef@entrepreware.com");
        mailMessage.setSubject("We crafted some offers JUST FOR YOU!");
        mailMessage.setSentDate(new Date());

        mailMessage.setText(messageBody);

        mailSender.send(mailMessage);
    }

    public void sendMimeMail() throws MailException {
        MimeMessage mailMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage);

        try {
            messageHelper.setSentDate(new Date());
            messageHelper.setTo("lyoussef@entrepreware.com");

            String mailSubject = "We crafted some offers JUST FOR YOU!";
            String mailContent = "<p>Hello Laura,</p>" +
                    "<br>" +
                    "<p>Thanks for being with us.</p>" +
                    "<p style=\"text-align: center;font-weight: bold;\"> Show this code to our staff</p>";

            messageHelper.setSubject(mailSubject);
            messageHelper.setText(mailContent, true);

            mailSender.send(mailMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
