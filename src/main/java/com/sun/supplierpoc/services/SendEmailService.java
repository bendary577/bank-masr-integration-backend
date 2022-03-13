package com.sun.supplierpoc.services;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.AccountEmailConfig;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.applications.ApplicationUser;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.CostCenter;
import com.sun.supplierpoc.repositories.AccountRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.*;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

@Service
public class SendEmailService {

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private JavaMailSender mailSender;

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

    public JavaMailSender javaGMailSender(Account account)
    {
        AccountEmailConfig emailConfig = account.getEmailConfig();
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//        mailSender.setHost(emailConfig.getHost());
//        mailSender.setPort(emailConfig.getPort());
//        mailSender.setUsername(emailConfig.getUsername());
//        mailSender.setPassword(emailConfig.getPassword());
        Properties props = mailSender.getJavaMailProperties();
        props.put("spring.mail.host", emailConfig.getHost());
        props.put("spring.mail.port", emailConfig.getPort());
        props.put("spring.mail.username", emailConfig.getUsername());
        props.put("spring.mail.password", emailConfig.getPassword());
        props.put("spring.mail.properties.mail.smtp.auth", "true");
        props.put("spring.mail.properties.mail.smtp.connectiontimeout", "5000");
        props.put("spring.mail.properties.mail.smtp.timeout","5000");
        props.put("spring.mail.properties.mail.smtp.writetimeout", "5000");
        props.put("spring.mail.properties.mail.smtp.starttls.enable", "true");
        return mailSender;
    }

    public boolean sendExportedSyncsMailMail(FileSystemResource f, Account account, User user, Date fromDate, Date toDate,
                                             List<CostCenter> stores, String email, List<SyncJobType> syncJobTypes)  throws MailException {

        MimeMessage mailMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage, true);
//            messageHelper.setFrom("Anyware_Software");
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            messageHelper.setSentDate(new Date());
            messageHelper.setTo(email);
//            String mailSubject = "Support Follow Up!";
            String mailSubject = getModules(syncJobTypes) + "Export is ready!";
            String mailContent =
                    "<div style=' margin-left: 1%; margin-right: 7%; width: 85%;font-size: 15px;'>" +
                            "<p style='text-align:left'>" +
                            "Dear " + user.getName()  + "<br> <br>" +
                            "<span style=' padding-left:20px'> Your request for export has been successfully done!</span><br>" +
                            "<span>for the " +
                            getModules(syncJobTypes)
                            +
                            " modules,</span> <br>" +
                            " <span>Located in " +
                            getStores(stores)
                            +
                            "</span><br>" +
                            " <span> within the date range from " + dateFormat.format(fromDate) + " to " + dateFormat.format(toDate) + " ,</span><br>" +
                            " <span> We are pleased to be associated with you." +
                            " You can contact support for any further clarifications,</span><br><br>" +
                            " Thanks and Regards,<br>" +
                            " Anyware Software<br>" +
                            "</div>";

            messageHelper.addAttachment(f.getFilename(), f);

            messageHelper.setSubject(mailSubject);
            messageHelper.setText(mailContent, true);
            mailSender.send(mailMessage);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendFailureMail(User user, String email, List<SyncJobType> syncJobTypes)  throws MailException {

        MimeMessage mailMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage, true);
            messageHelper.setSentDate(new Date());
            messageHelper.setTo(email);
            String mailSubject = getModules(syncJobTypes) + "Export is ready!";

            String mailContent =
                    "<div style=' margin-left: 1%; margin-right: 7%; width: 85%;font-size: 15px;'>" +
                            "<p style='text-align:left'>" +
                            "Dear " + user.getName()  + "<br> <br>" +

                            " <span> Your report had failed to be exported. Please login and try again or contact support for further assistance.</span><br><br>" +

                            " Thanks and Regards,<br>" +
                            " Anyware Software<br>" +
                            "</div>";

            messageHelper.setSubject(mailSubject);
            messageHelper.setText(mailContent, true);
            mailSender.send(mailMessage);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    String getModules(List<SyncJobType> syncJobTypes){
        String modules = "";
        int i = syncJobTypes.size();
        boolean start = true;
        for(SyncJobType syncJobType  : syncJobTypes){
            if(i == 0 && !start){
                modules =  "and " + modules + syncJobType.getName() + " ";
            }else{
                modules = modules + syncJobType.getName() + " ";
            }
            start = false;
            i -= 1;
        }
        return modules;
    }

    String getStores(List<CostCenter> costCenters){
        String stores = "";
        int i = costCenters.size();
        boolean start = true;
        for(CostCenter costCenter : costCenters){
            if(i == 0 && !start){
                stores =  "and " + stores + costCenter.locationName + " ";
            }else{
                stores = stores + costCenter.locationName + ", ";
            }
            start = false;
            i -= 1;
        }
        return stores;
    }

    public void sendWalletMail(String email) throws MailException {
        String messageBody = "Thanks for being with us.";
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setSubject("We crafted some offers JUST FOR YOU!");
        mailMessage.setSentDate(new Date());
        mailMessage.setText(messageBody);
        mailSender.send(mailMessage);
        System.out.println("Finish");
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

            if(!logoPath.equals("")){
                mailContent +=
                        "<img style=\"width:50%; height: 50%; margin-left: 10px;\"" +
                                "   src='" + logoPath + "'>" + "<br> <br> \n";
            }

            if(mailSubj != null && !mailSubj.equals("")){
                mailContent +=
                        "<img style=\"width:60%; height: 60%; margin-left: 10px;\"" +
                                "   src='" + mailSubj + "'>" + "<br>\n";
            }

            mailContent +=

                    "<p style='text-align:left'>" +
                            "   Dear  " + user.getName() + ",<br><br>" +
                            "</p>";
            mailContent += account.getEmailConfig().getEmailHeader();

            mailContent +=
                            "<p style=\"text-align: center ;font-weight: bold; margin-button : 10%\"> SCAN QR CODE <br> TO REDEEM OFFER</p>" +
                            "<img src='"+qrCodePath+"' style=\"display: block;margin-left: auto; margin-right: auto;\">" +
                            "<p style='text-align:center; font-weight: bold;'>" + " CODE : " + user.getCode() + "</p>" + "<br>";


            mailContent += account.getEmailConfig().getEmailFooter();
            mailContent +=
//                    "<div style=\"margin-left: 50%; margin-bottom: 5%; color: #ffffff;  "+
//                    "text-align: center;font-weight: bold; backGround-color : #ae0a3b; width : 40%; height : 25% \">" +
//                    "<a style=\"color: #ffffff; text-decoration: none;\" href='https://www.movenpick.com' >VISIT WEBSITE </a>" +
//                    "</div>"+
                            "</div>";

            messageHelper.setSubject(mailSubject);
            messageHelper.setText(mailContent, true);

            getJavaMailSender(account).send(mailMessage);

            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean sendEmaarMail(String email, List<HashMap<String, String>> responses, Account account){

        MimeMessage mailMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage, true);
            messageHelper.setSentDate(new Date());
            messageHelper.setTo(email);
            String mailSubject = "Emaar daily sales!";

            String body = "";
            String mailContent = "";

            for(HashMap response: responses){
                body = body + "Store Name : " + response.get("storeName")  + "<br>" +
                        "Store Number : " + response.get("storeNum") + " <br>"  +
                        "For Date : "  + response.get("date") + " <br>" +
                        "Result : " + response.get("Result") + " <br>" +
                        "with request body : " + response.get("requestBody")  + " <br> <br> <br>";
            }
            if(responses.size() == 0){
                mailContent =
                        "<div style=' margin-left: 1%; margin-right: 7%; width: 85%;font-size: 15px;'>" +
                                "<p style='text-align:left'>" +
                                "Dears, <br> <br>" +

                                " <span> " + account.getName() + "'s daily sales were not sent out on this day.</span><br><br>" +

                                " Thanks and Regards,<br>" +
                                " Anyware Software<br>" +
                                "</div>";
            }else{
                mailContent =
                        "<div style=' margin-left: 1%; margin-right: 7%; width: 85%;font-size: 15px;'>" +
                                "<p style='text-align:left'>" +
                                "Dears, <br> <br>" +

                                " <span> The daily sales of " + account.getName() + " has been sent with bellow data.</span><br><br>" +
                                body +
                                " Thanks and Regards,<br>" +
                                " Anyware Software<br>" +
                                "</div>";
            }

            messageHelper.setSubject(mailSubject);
            messageHelper.setText(mailContent, true);
            mailSender.send(mailMessage);

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
