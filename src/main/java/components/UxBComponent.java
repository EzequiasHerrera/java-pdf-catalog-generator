package components;

import static utils.PDFStyleDefaults.BLACK_COLOR;

import org.apache.poi.ss.usermodel.Cell;

import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;

import themes.Theme;
import utils.PDFUtils;

public class UxBComponent {
    public static Paragraph build(Cell cell, Theme theme) {
        try {
            String uxbText = PDFUtils.safeText(PDFUtils.getCellValue(cell), "--");

            Text valorUxb = new Text(uxbText).setBold();

            return new Paragraph("UXB: ")
                    .add(valorUxb)
                    .setFontSize(10)
                    .setFontColor(BLACK_COLOR)
                    .setMultipliedLeading(1f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMargin(0)
                    .setPadding(1);
        } catch (Exception e) {
            return new Paragraph("UXB: ")
                    .add("-")
                    .setFontSize(10)
                    .setFontColor(BLACK_COLOR)
                    .setMultipliedLeading(1f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMargin(0)
                    .setPadding(1);
        }
    }
}