package com.sun.supplierpoc.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

@Service
public class SendEmailService {
    private JavaMailSender javaMailSender ;

    @Autowired
    public SendEmailService(JavaMailSender javaMailSender){
        this.javaMailSender = javaMailSender;
    }

    public void sendNotificationMail() throws MailException {
        String messageBody = "Thanks for being with us.";
        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setTo("lyoussef@entrepreware.com");
        mailMessage.setSubject("We crafted some offers JUST FOR YOU!");
        mailMessage.setSentDate(new Date());

        mailMessage.setText(messageBody);

        javaMailSender.send(mailMessage);
    }

    public void sendmail() throws AddressException, MessagingException, IOException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "25");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("laurayoussef2011@gmail.com", "@Entrepreware123");
            }
        });
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("lyoussef@gmail.com", false));

        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse("laurayoussef2011@gmail.com"));
        msg.setSubject("Tutorials point email");
        msg.setContent("Tutorials point email", "text/html");
        msg.setSentDate(new Date());

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent("Tutorials point email", "text/html");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        MimeBodyPart attachPart = new MimeBodyPart();

        attachPart.attachFile("/var/tmp/image19.png");
        multipart.addBodyPart(attachPart);
        msg.setContent(multipart);
        Transport.send(msg);
    }

}
