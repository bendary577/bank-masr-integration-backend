package com.sun.supplierpoc.pdfExporters;

import com.google.zxing.WriterException;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.applications.ApplicationUser;
import com.sun.supplierpoc.models.simphony.redeemVoucher.Voucher;
import com.sun.supplierpoc.services.QRCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

@Component
public class QRCodePDFGenerator {
    @Autowired
    QRCodeGenerator qrCodeGenerator;

    public void generatePdfReport(Account account, ArrayList<ApplicationUser> users,
                                  HttpServletResponse response) {

        Document document = new Document();

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            document.open();
            addDocTitle(document);
            for(ApplicationUser user : users) {

                String qrCodePath = "QRCodes/" + user.getCode() + ".png";
                try {
                    qrCodeGenerator.generateQRCodeImage(user.getCode(), 200, 200, qrCodePath);
                } catch (WriterException e) {
                    e.printStackTrace();
                }

                document.setPageSize(PageSize.LETTER);
                document.setMargins(20, 20, 20, 20);
                document.setMarginMirroring(false);

                addDocDetails(document, user);
                addQRCode(document, qrCodePath);

                try {
                    Path imagesPath = Paths.get(qrCodePath);
                    Files.delete(imagesPath);
                    System.out.println("File " + imagesPath.toAbsolutePath().toString() + " successfully removed");
                } catch (IOException e) {
                }

                document.newPage();
            }
            document.close();

            response.setHeader("Expires", "0");
            response.setHeader("Cache-Control",
                    "must-revalidate, post-check=0, pre-check=0");
            response.setHeader("Pragma", "public");
            // setting the content type
            response.setContentType("application/pdf");
            // the contentlength
            response.setContentLength(baos.size());
            // write ByteArrayOutputStream to the ServletOutputStream
            OutputStream os = response.getOutputStream();
            baos.writeTo(os);
            os.flush();
            os.close();

            System.out.println("------------------Your PDF Report is ready!-------------------------");

        } catch (FileNotFoundException | DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void addQRCode(Document document, String qrCodePath) {
        try {
            Image img = Image.getInstance(qrCodePath);
            img.scalePercent(60, 60);
            img.setAlignment(Element.ALIGN_RIGHT);
            document.add(img);

            new File(qrCodePath).delete();

        } catch (DocumentException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void addDocTitle(Document document) throws DocumentException {
        Paragraph p1 = new Paragraph();
        p1.add(new Paragraph("Reward Points Users",
                new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD)));
        p1.setAlignment(Element.ALIGN_CENTER);
        document.add(p1);
    }

    private void addDocDetails(Document document, ApplicationUser user) throws DocumentException {
        Paragraph p1 = new Paragraph();
        p1.setAlignment(Element.ALIGN_LEFT);

        p1.add(new Paragraph(" "));

        p1.add(new Paragraph(
                "User Name: " + user.getName()
                , new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));

        p1.add(new Paragraph(
                "User QR Code: " +  user.getCode()
                , new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
        p1.add(new Paragraph(
                "User Point: " +  user.getPoints() + " Point"
                , new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
        document.add(p1);
    }


}
