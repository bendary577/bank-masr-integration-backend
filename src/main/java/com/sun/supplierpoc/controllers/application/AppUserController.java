package com.sun.supplierpoc.controllers.application;
import com.sun.supplierpoc.services.QRCodeGenerator;
import com.sun.supplierpoc.services.SendEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = {"/Simphony"})

public class AppUserController {
    @Autowired
    QRCodeGenerator qrCodeGenerator;
    @Autowired
    SendEmailService emailService;

    private static final String QR_CODE_IMAGE_PATH = "./src/main/resources/QRCode.png";

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @GetMapping(path = "/sendQRCodeEmail")
    public void sendQRCodeEmail()
            throws Exception {
        try {
            emailService.sendNotificationMail();
        } catch (MailException e) {
            e.printStackTrace();
        }

    }

    @GetMapping(path = "/generateAndDownloadQRCode")
    public void generateAndDownloadQRCode(
            @RequestParam(name = "codeText") String codeText,
            @RequestParam(name = "width") Integer width,
            @RequestParam(name = "height") Integer height)
            throws Exception {
        qrCodeGenerator.generateQRCodeImage(codeText, width, height, QR_CODE_IMAGE_PATH);
    }

    @GetMapping(value = "/generateQRCode")
    public ResponseEntity<byte[]> generateQRCode(
            @RequestParam(name = "codeText") String codeText,
            @RequestParam(name = "width") Integer width,
            @RequestParam(name = "height") Integer height)
            throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(qrCodeGenerator.getQRCodeImage(codeText, width, height));
    }
}
