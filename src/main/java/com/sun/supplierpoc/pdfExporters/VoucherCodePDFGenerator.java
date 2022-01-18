package com.sun.supplierpoc.pdfExporters;


import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.itextpdf.text.*;
import com.itextpdf.text.html.WebColors;

import com.itextpdf.text.pdf.draw.VerticalPositionMark;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.simphony.redeemVoucher.Voucher;
import org.springframework.stereotype.Component;

import com.itextpdf.text.pdf.PdfWriter;


import javax.servlet.http.HttpServletResponse;

@Component
public class VoucherCodePDFGenerator {

    public void generatePdfReport(Account account, Voucher voucher, String qrCodePath, HttpServletResponse response) {

        Document document = new Document();

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, baos);
//            writer.setPageEvent(new PDFBackground());

            document.setPageSize(PageSize.LETTER);
            document.setMargins(20, 20, 20, 20);
            document.setMarginMirroring(false);

            document.open();
            addLogo(document, account.getImageUrl(), voucher);
            addDocTitle(document, voucher);
            addDocDetails(document, voucher);
            addQRCode(document, qrCodePath, voucher);
//            addFooter(document, voucher);
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

    private void addLogo(Document document, String imageUrl, Voucher voucher) {
        try {

//            Chunk glue = new Chunk(new VerticalPositionMark());
//            Paragraph p = new Paragraph("Text to the left");
//            p.add(new Chunk(glue));
//            p.add("Text to the right");
//            document.add(p);

            Chunk glue = new Chunk(new VerticalPositionMark());
            Paragraph p1 = new Paragraph();
            BaseColor myColorpan = WebColors.getRGBColor("#a29d7f");
            p1.add(new Chunk(glue));
            p1.add(new Chunk(voucher.getVoucherCode(), new Font(Font.FontFamily.COURIER, 20, Font.BOLD, myColorpan)));
            document.add(p1);

//            URL url = new URL(imageUrl);
//            BufferedImage image = ImageIO.read(url);
//            File file = new File("voucher/img_JTO_logo.jpg");
//            ImageIO.write(image, "jpg", file);

            Image img = Image.getInstance("voucher/logo.png");
            img.scalePercent(80, 80);
            img.setAlignment(Element.ALIGN_CENTER);
            document.add(img);


            Image image = Image.getInstance("voucher/voucher.png");
            image.scalePercent(90, 90);
            image.setAlignment(Element.ALIGN_CENTER);
            document.add(image);

        } catch (DocumentException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void addDocTitle(Document document, Voucher voucher) throws DocumentException {
        Paragraph p1 = new Paragraph();
        p1.add(new Paragraph(String.valueOf(voucher.getSimphonyDiscount().getDiscountRate()).substring(0, 2) + "% DISCOUNT VOUCHER",
                                new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD)));
        p1.setAlignment(Element.ALIGN_CENTER);
        p1.add(new Paragraph(
                //voucher.getName()
                        "More benefits at a " + "glance" + " with " + voucher.getSimphonyDiscount().getDiscountRate() + "%" +
                                " savings on stays and dining."
                        , new Font(Font.FontFamily.HELVETICA, 15, Font.BOLD)));
        document.add(p1);

    }


    private void addDocDetails(Document document, Voucher voucher) throws DocumentException {
        Paragraph p1 = new Paragraph();
        leaveEmptyLine(p1, 1);
        leaveEmptyLine(p1, 1);
        p1.add(new Paragraph("T&Cs:", new Font(Font.FontFamily.HELVETICA, 15, Font.BOLD)));
        p1.setAlignment(Element.ALIGN_CENTER);
        leaveEmptyLine(p1, 1);
        p1.add(new Paragraph(
                ". Physical card must be presented to redeem this offer.\n" +
                        ".  Voucher is reusable and valid till " + voucher.getEndDate() + ".\n" +
                        ".  " + String.valueOf(voucher.getSimphonyDiscount().getDiscountRate()).substring(0, 2) + "% off rooms are applicable on best available rate and subject to availability\n" +
                        ".  Voucher cannot be replaced if lost or stolen, and is not refundable or redeemable for cash.\n" +
                        ".  Voucher cannot be used in conjunction with any other offer or promotion.\n" +
                        ".  Participating dining outlets include Sankofa Restaurant, Pool Bar and Lobby Lounge.\n" +
                        ".  Limited to one voucher per dining.\n" +
                        ".  Discounts in Sankofa Restaurant are not applicable on special occasions such as Christmas eve dinner."
                , new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
        document.add(p1);

    }

    private void addQRCode(Document document, String qrCodePath, Voucher voucher) {
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
