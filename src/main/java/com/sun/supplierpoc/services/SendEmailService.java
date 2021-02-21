package com.sun.supplierpoc.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.*;
import java.io.File;
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

    public void sendMimeMail(String qrCodePath) throws MailException {
        MimeMessage mailMessage = mailSender.createMimeMessage();

        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage, true);

            messageHelper.setSentDate(new Date());
            messageHelper.setTo("lyoussef@entrepreware.com");

            String mailSubject = "We crafted some offers JUST FOR YOU!";
            String mailContent =
                    "<div style='box-shadow: 0 4px 8px 0 rgba(0,0,0,0.2);transition: 0.3s; width: 100%;'>" +
                    "<br>" +
                    "<img style=\"width:20%; display: block;margin-left: auto; margin-right: auto;\"" +
                    "   src='https://togo.ovio.eu/assets/media/uploads/ovio-logo.png'>" +
                    "<br>\n" +
                    "<img style=\"width:100%\"" +
                    "   src='https://vistapointe.net/images/gift-1.jpg'>" +
                    "<h3 style='text-align:center'>Hello Laura,</h3>" +
                    "<p style='text-align:center'>" +
                    "   Welcome to Ovio restaurant loyalty program." +
                    "   <br>" +
                    "   Please feel free to use the below QR code in each visit to enjoy your special discount." +
                    "</p>" +

                    "<p style=\"text-align: center;font-weight: bold;\"> Show this code to our staff</p>" +
                    "<img src='cid:image001' style=\"display: block;margin-left: auto; margin-right: auto;\">" +
                    "<p style='text-align:center'>Look forward to seeing you at out store</p>" +

                    "<br>" +
                    "<br>" +
                    "<div style=\"display: table;margin-left: auto; margin-right: auto;\">" +
                    "<div style=\"float: left; width: 10%;\">" +
                    "   <img src='https://s3-eu-west-1.amazonaws.com/wuzzuf/files/company_logo/Entrepreware-Egypt-7223.png'>\n" +
                    "</div>";

            messageHelper.setSubject(mailSubject);
            messageHelper.setText(mailContent, true);

            FileSystemResource resource = new FileSystemResource(new File(qrCodePath));
            messageHelper.addInline("image001", resource);

            mailSender.send(mailMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
