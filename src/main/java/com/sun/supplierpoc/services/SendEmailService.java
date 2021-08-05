package com.sun.supplierpoc.services;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.AccountEmailConfig;
import com.sun.supplierpoc.models.applications.ApplicationUser;
import com.sun.supplierpoc.repositories.AccountRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.*;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

@Service
public class SendEmailService {

    @Autowired
    private AccountRepo accountRepo;

    public JavaMailSender getJavaMailSender(Account account)
    {

        AccountEmailConfig emailConfig = account.getEmailConfig();

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(emailConfig.getHost());
        mailSender.setPort(emailConfig.getPort());

        mailSender.setUsername(emailConfig.getUsername());
        mailSender.setPassword(emailConfig.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        return mailSender;
    }

    public void sendSimpleMail() throws MailException {
        String messageBody = "Thanks for being with us.";
        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setTo("lyoussef@entrepreware.com");
        mailMessage.setSubject("We crafted some offers JUST FOR YOU!");
        mailMessage.setSentDate(new Date());

        mailMessage.setText(messageBody);

        getJavaMailSender(new Account()).send(mailMessage);
        System.out.println("Finish");
    }

    public boolean sendMimeMail(String qrCodePath, String logoPath, String mailSubj, String accountName, ApplicationUser user, Account account) throws MailException {

        MimeMessage mailMessage = getJavaMailSender(account).createMimeMessage();
        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage, true);
            try {
                messageHelper.setFrom(account.getEmailConfig().getUsername(), accountName);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            messageHelper.setSentDate(new Date());
            messageHelper.setTo(user.getEmail());
            String mailSubject = "More rewards, just for YOU!";
            String mailContent =
                    "<div style='box-shadow: 0 4px 8px 0 rgba(0,0,0,0.2); margin-left: 7%; margin-right: 7%;padding-left: 5%;"+
                            "transition: 0.3s; width: 85%; border:2px solid #ae0a3b;\n'>" +
                    "<br>";
            mailContent +=
                    "<img style=\"width:50%; height: 50%; margin-left: 10px;\"" +
                            "   src='" + logoPath + "'>" + "<br> <br> \n";
            mailContent +=
                    "<img style=\"width:60%; height: 60%; margin-left: 10px;\"" +
                            "   src='" + mailSubj + "'>" + "<br>\n";
            mailContent +=

                    "<p style='text-align:left'>" +
                            "   Dear  " + user.getName() + ",<br><br>" +
                            "As a privileged guest, benefit from the most rewarding <br>" + "" +
                            "advantages with great discounts on laundry services or <br>" +
                            "food and beverages at participating outlets.<br>" + "</p>" +
                            "<p style=\"text-align: center ;font-weight: bold; margin-button : 10%\"> SCAN QR CODE <br> TO REDEEM OFFER</p>" +
                            "<img src='"+qrCodePath+"' style=\"display: block;margin-left: auto; margin-right: auto;\">" +
                            "<p style='text-align:center; font-weight: bold;'>" + " CODE : " + user.getCode() + "</p>" + "<br>" +
                            "<p style='text-align:left'>" +
                            "<span style='text-align:left; font-weight: bold;'> " + "PARTICIPATING OUTLETS:" + "</span> <br>" +
                            "SANKOFA RESTAURANT | ONE2ONE BAR | POOL BAR & BBQ" +
                            "<br> <br>" +
                            "CONTACT US: +233 302 611 000 / hotel.accra@movenpick.com" +
                            "</p>" +
                            "<br>" +
                            "<div style=\"margin-left: 50%; margin-bottom: 5%; color: #ffffff;  "+
                                "text-align: center;font-weight: bold; backGround-color : #ae0a3b; width : 40%; height : 25% \">" +
                            "<a style=\"color: #ffffff; text-decoration: none;\" href='https://www.movenpick.com' >VISIT WEBSITE </a>" +
                            "</div>"+ "</div>";

            messageHelper.setSubject(mailSubject);
            messageHelper.setText(mailContent, true);

            getJavaMailSender(account).send(mailMessage);

            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendWelcomeEmail(String logoPath, String mailSubj, String accountName, ApplicationUser user, Account account) throws MailException {

        MimeMessage mailMessage = getJavaMailSender(account).createMimeMessage();
        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage, true);
            try {
                messageHelper.setFrom(account.getEmailConfig().getUsername(), accountName);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            messageHelper.setSentDate(new Date());
            messageHelper.setTo(user.getEmail());
            String mailSubject = "More rewards, just for YOU!";
            String mailContent =
                    "<div style='box-shadow: 0 4px 8px 0 rgba(0,0,0,0.2); margin-left: 7%; margin-right: 7%;padding-left: 5%;"+
                            "transition: 0.3s; width: 85%; border:2px solid #ae0a3b;\n'>" +
                            "<br>";
            mailContent +=
                    "<img style=\"width:50%; height: 50%; margin-left: 10px;\"" +
                            "   src='" + logoPath + "'>" + "<br> <br> \n";
            mailContent +=
                    "<img style=\"width:60%; height: 60%; margin-left: 10px;\"" +
                            "   src='" + mailSubj + "'>" + "<br>\n";
            mailContent +=

                    "<p style='text-align:left'>" +
                            "   Dear  " + user.getName() + ",<br><br>" +
                            "As a privileged guest, benefit from the most rewarding <br>" + "" +
                            "advantages with great discounts on laundry services or <br>" +
                            "food and beverages at participating outlets.<br>" + "</p>" +
                            "<p style=\"text-align: center ;font-weight: bold; margin-button : 10%\"> SCAN QR CODE <br> TO REDEEM OFFER</p>" +
                            "<img src='' style=\"display: block;margin-left: auto; margin-right: auto;\">" +
                            "<p style='text-align:center; font-weight: bold;'>" + " CODE : " + user.getCode() + "</p>" + "<br>" +
                            "<p style='text-align:left'>" +
                            "<span style='text-align:left; font-weight: bold;'> " + "PARTICIPATING OUTLETS:" + "</span> <br>" +
                            "SANKOFA RESTAURANT | ONE2ONE BAR | POOL BAR & BBQ" +
                            "<br> <br>" +
                            "CONTACT US: +233 302 611 000 / hotel.accra@movenpick.com" +
                            "</p>" +
                            "<br>" +
                            "<div style=\"margin-left: 50%; margin-bottom: 5%; color: #ffffff;  "+
                            "text-align: center;font-weight: bold; backGround-color : #ae0a3b; width : 40%; height : 25% \">" +
                            "<a style=\"color: #ffffff; text-decoration: none;\" href='https://www.movenpick.com' >VISIT WEBSITE </a>" +
                            "</div>"+ "</div>";
            messageHelper.setSubject(mailSubject);
            messageHelper.setText(mailContent, true);
            getJavaMailSender(account).send(mailMessage);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendAlertMail(Account account) throws MailException {

        MimeMessage mailMessage = getJavaMailSender(accountRepo.findByIdAndDeleted("60759205d942776de5025f82", false).orElseThrow()).createMimeMessage();

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
                            "   src='" + account.getImageUrl() + "'>";

            mailContent +=
                    "<h3 style='text-align:center'>Dears,</h3>" +
                            "<p style='text-align:center'>" +
                            "   Please notify that we have notes that the account of " + account.getName() +
                            "   <br>" +
                            "   have issue in the quota of the revenue centers" +
                            "   <br>" +
                            "   Please check and solve it." +
                            "   Best Regards." +
                            "</p>";


            messageHelper.setSubject(mailSubject);
            messageHelper.setText(mailContent, true);

            getJavaMailSender(accountRepo.findByIdAndDeleted("60759205d942776de5025f82", false).orElseThrow()).send(mailMessage);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

}
