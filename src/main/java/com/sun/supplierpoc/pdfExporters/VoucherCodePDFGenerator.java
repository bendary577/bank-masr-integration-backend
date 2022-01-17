package com.sun.supplierpoc.pdfExporters;


import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.itextpdf.text.*;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.simphony.redeemVoucher.Voucher;
import org.springframework.stereotype.Component;

import com.itextpdf.text.pdf.PdfWriter;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

@Component
public class VoucherCodePDFGenerator {

    public void generatePdfReport(Account account, Voucher voucher, String qrCodePath, HttpServletResponse response) {

        Document document = new Document();

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);

            document.setPageSize(PageSize.LETTER);
            document.setMargins(20, 20, 20, 20);
            document.setMarginMirroring(false);

            document.open();
            addLogo(document, account.getImageUrl());
            addDocTitle(document, voucher);
            addQRCode(document, qrCodePath, voucher);
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

            URL url = new URL(imageUrl);
            BufferedImage image = ImageIO.read(url);
            File file = new File("QRCodes/img_JTO_logo.jpg");
            ImageIO.write(image, "jpg", file);

            Image img = Image.getInstance("QRCodes/img_JTO_logo.jpg");
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
        p1.add(new Paragraph("Voucher: " + voucher.getName(), new Font(Font.FontFamily.COURIER, 20, Font.BOLD)));
        p1.setAlignment(Element.ALIGN_CENTER);
        leaveEmptyLine(p1, 1);
        p1.add(new Paragraph(
                        "As a privileged guest, benefit from the most rewarding \n" +
                        "advantages with great discounts on laundry services or \n" +
                        "food and beverages at participating outlets. \n" +
                        "Enjoy our voucher with a discount of up to " + voucher.getSimphonyDiscount().getDiscountRate() + "%" + "\n"
                        , new Font(Font.FontFamily.COURIER, 15, Font.BOLD)));
        document.add(p1);

    }

    private void addQRCode(Document document, String qrCodePath, Voucher voucher) {
        try {
            Paragraph p1 = new Paragraph();
            leaveEmptyLine(p1, 1);
            leaveEmptyLine(p1, 1);
            p1.add(new Paragraph("SCAN QR CODE TO REDEEM VOUCHER \n"
                    , new Font(Font.FontFamily.COURIER, 10, Font.BOLD)));
            p1.setAlignment(Element.ALIGN_CENTER);
            document.add(p1);


            Paragraph paragraph = new Paragraph();
            paragraph.add(new Paragraph("Code: " + voucher.getVoucherCode()
                    , new Font(Font.FontFamily.COURIER, 13, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph);

            Image img = Image.getInstance(qrCodePath);
            img.scalePercent(150, 150);
            img.setAlignment(Element.ALIGN_CENTER);
            document.add(img);

            Paragraph p2 = new Paragraph();
            p2.add(new Paragraph("CONTACT US: +233 302 611 000 / hotel.accra@movenpick.com \n"
                    , new Font(Font.FontFamily.COURIER, 10, Font.BOLD)));
            p2.setAlignment(Element.ALIGN_LEFT);
            document.add(p2);

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
