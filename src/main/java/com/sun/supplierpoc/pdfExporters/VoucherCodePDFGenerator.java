package com.sun.supplierpoc.pdfExporters;


import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.simphony.redeemVoucher.Voucher;
import org.springframework.stereotype.Component;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

@Component
public class VoucherCodePDFGenerator {

    public void generatePdfReport(Account account, Voucher voucher, String qrCodePath, HttpServletResponse response) {

        Document document = new Document();

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);

            document.open();
            addLogo(document, account.getImageUrl());
            addDocTitle(document, voucher);
            addQRCode(document, qrCodePath);
            addFooter(document, voucher);
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

    private void addLogo(Document document, String imageUrl) {
        try {
            Image img = Image.getInstance("D:/PdfReportRepo/img_JTO_logo.jpg");
            img.scalePercent(30, 20);
            img.setAlignment(Element.ALIGN_RIGHT);
            document.add(img);
        } catch (DocumentException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void addDocTitle(Document document, Voucher voucher) throws DocumentException {
        Paragraph p1 = new Paragraph();
        leaveEmptyLine(p1, 1);
        p1.add(new Paragraph(voucher.getName(), new Font(Font.FontFamily.COURIER, 20, Font.BOLD)));
        p1.setAlignment(Element.ALIGN_CENTER);
        leaveEmptyLine(p1, 1);
        p1.add(new Paragraph("Enjoy our voucher with a discount of up to " + voucher.getSimphonyDiscount().getDiscountRate() + "%"
                , new Font(Font.FontFamily.COURIER, 16, Font.BOLD)));
        document.add(p1);

    }

    private void addQRCode(Document document, String qrCodePath) {
        try {
            Image img = Image.getInstance(qrCodePath);
            img.scalePercent(150, 150);
            img.setAlignment(Element.ALIGN_CENTER);
            document.add(img);
        } catch (DocumentException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private void addFooter(Document document, Voucher voucher) throws DocumentException {
        Paragraph p2 = new Paragraph();
        leaveEmptyLine(p2, 3);
        p2.setAlignment(Element.ALIGN_BOTTOM);
        p2.add(new Paragraph(
                "------------------------ End Of Voucher Details ------------------------",
                new Font(Font.FontFamily.COURIER, 12, Font.BOLD)));

        document.add(p2);
    }

    private static void leaveEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }

    private String getPdfNameWithDate() {
        String localDateString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd_MMMM_yyyy"));
        return "D:/PdfReportRepo/"+"Employee-Report"+"-"+localDateString+".pdf";
    }

}
