package com.FundRaise.F25.Service;

import com.FundRaise.F25.model.Contribution;
import com.FundRaise.F25.model.Expense;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfExportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font CELL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font TOTAL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

    public byte[] generateContributionReport(String monthLabel, List<Contribution> contributions) {
        Document document = newDocument();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph title = new Paragraph(monthLabel + " Contribution Report", TITLE_FONT);
            title.setSpacingAfter(18);
            document.add(title);

            double total = 0;
            if (contributions.isEmpty()) {
                document.add(new Paragraph("No contributions recorded this month.", CELL_FONT));
            } else {
                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{3, 2, 2, 2});
                addHeaderCell(table, "Member");
                addHeaderCell(table, "Date");
                addHeaderCell(table, "Amount (Rs.)");
                addHeaderCell(table, "Status");

                for (Contribution c : contributions) {
                    String name = c.getUser() != null
                            ? c.getUser().getFirstName() + " " + c.getUser().getLastName()
                            : "Unknown";
                    table.addCell(new Phrase(name, CELL_FONT));
                    table.addCell(new Phrase(c.getCreatedAt().format(DATE_FMT), CELL_FONT));
                    table.addCell(new Phrase(String.format("%.2f", c.getAmount() == null ? 0 : c.getAmount()), CELL_FONT));
                    table.addCell(new Phrase(c.getStatus().name(), CELL_FONT));
                    if (c.getAmount() != null) total += c.getAmount();
                }
                document.add(table);
            }

            Paragraph totalPara = new Paragraph("Total contributions: Rs. " + String.format("%.2f", total), TOTAL_FONT);
            totalPara.setSpacingBefore(12);
            document.add(totalPara);

            document.close();
        } catch (DocumentException ex) {
            throw new RuntimeException("Failed to generate contribution report PDF: " + ex.getMessage(), ex);
        }
        return out.toByteArray();
    }

    public byte[] generateExpenseReport(String monthLabel, List<Expense> expenses) {
        Document document = newDocument();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph title = new Paragraph(monthLabel + " Expense Report", TITLE_FONT);
            title.setSpacingAfter(18);
            document.add(title);

            double total = 0;
            if (expenses.isEmpty()) {
                document.add(new Paragraph("No expenses logged this month.", CELL_FONT));
            } else {
                PdfPTable table = new PdfPTable(3);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{2, 5, 2});
                addHeaderCell(table, "Date");
                addHeaderCell(table, "Description");
                addHeaderCell(table, "Amount (Rs.)");

                for (Expense e : expenses) {
                    table.addCell(new Phrase(e.getCreatedAt().format(DATE_FMT), CELL_FONT));
                    table.addCell(new Phrase(e.getDescription(), CELL_FONT));
                    table.addCell(new Phrase(String.format("%.2f", e.getAmount() == null ? 0 : e.getAmount()), CELL_FONT));
                    if (e.getAmount() != null) total += e.getAmount();
                }
                document.add(table);
            }

            Paragraph totalPara = new Paragraph("Total expenses: Rs. " + String.format("%.2f", total), TOTAL_FONT);
            totalPara.setSpacingBefore(12);
            document.add(totalPara);

            document.close();
        } catch (DocumentException ex) {
            throw new RuntimeException("Failed to generate expense report PDF: " + ex.getMessage(), ex);
        }
        return out.toByteArray();
    }

    private Document newDocument() {
        return new Document(PageSize.A4, 36, 36, 54, 36);
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(new Color(230, 230, 230));
        cell.setPadding(6);
        table.addCell(cell);
    }
}