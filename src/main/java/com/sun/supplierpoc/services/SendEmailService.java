package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.applications.ApplicationUser;
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
    public SendEmailService(JavaMailSender mailSender) {
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
        System.out.println("Finish");
    }

    public boolean sendMimeMail(String qrCodePath, String logoPath, String accountLogo, String accountName, ApplicationUser user) throws MailException {

        MimeMessage mailMessage = mailSender.createMimeMessage();

        try {

            MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage, true);

            messageHelper.setSentDate(new Date());

            messageHelper.setTo(user.getEmail());

            String mailSubject = "We crafted some offers JUST FOR YOU!";

            String mailContent =
                    "<div style='box-shadow: 0 4px 8px 0 rgba(0,0,0,0.2);transition: 0.3s; width: 100%;'>" +
                            "<br>";

            if(!logoPath.equals(Constants.GROUP_IMAGE_URL)){
                mailContent +=
                        "<img style=\"width:20%; height: 20%; margin-left: 30%; margin-right: 2%;\"" +
                                "   src='" + logoPath + "'>"  +

                                "<img style=\"width:20%; height: 20%; margin-left: auto; margin-right: auto;\"" +
                        "   src='" + accountLogo + "'>" + "<br>\n";
            }
            mailContent +=
                            "<img style=\"width:100%\"" +
                            "   src='https://vistapointe.net/images/gift-1.jpg'>" +
                            "<h3 style='text-align:center'>Hello " + user.getName() + ",</h3>" +
                            "<p style='text-align:center'>" +
                            "   Welcome to "+ accountName + " loyalty program." +
                            "   <br>" +
                            "   Please feel free to use the below QR code in each visit to enjoy your special discount." +
                            "</p>" +

                            "<p style=\"text-align: center;font-weight: bold;\"> Show this code to our staff</p>" +
                            "<img src='cid:image001' style=\"display: block;margin-left: auto; margin-right: auto;\">" +
                            "<p style='text-align:center'>" + " code : " + user.getCode() + "</p>" +
                            "<p style='text-align:center'>Look forward to seeing you at out store</p>" +
                            "<br>" +
                            "<br>" +
                            "<div style=\"display: table;margin-left: auto; margin-right: auto;\">";


            messageHelper.setSubject(mailSubject);
            messageHelper.setText(mailContent, true);

            FileSystemResource resource = new FileSystemResource(new File(qrCodePath));
            messageHelper.addInline("image001", resource);
            mailSender.send(mailMessage);

            new File(qrCodePath).delete();
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendAlertMail(Account account) throws MailException {

        MimeMessage mailMessage = mailSender.createMimeMessage();

        try {

            MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage, true);

            messageHelper.setSentDate(new Date());
            messageHelper.setTo("bfaisal@entrepreware.com");
//            messageHelper.setTo("lyoussef@entrepreware.com");

            String mailSubject = "Account Alert!";

            String mailContent =
                    "<div style='box-shadow: 0 4px 8px 0 rgba(0,0,0,0.2);transition: 0.3s; width: 100%;'>" +
                            "<br>";

            mailContent +=
                        "<img style=\"width:20%; height: 20%; margin-left: 30%; margin-right: 2%;\"" +
                                "   src='" + account.getImageUrl() + "'>"  ;

            mailContent +=
                            "<h3 style='text-align:center'>Dears,</h3>" +
                            "<p style='text-align:center'>" +
                            "   Please notify that we have notes that the account of " +account.getName() +
                            "   <br>" +
                            "   have issue in the quota of the revenue centers" +
                            "   <br>" +
                            "   Please check and solve it." +
                            "   Best Regards." +
                            "</p>" ;
;


            messageHelper.setSubject(mailSubject);
            messageHelper.setText(mailContent, true);

            mailSender.send(mailMessage);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

}
