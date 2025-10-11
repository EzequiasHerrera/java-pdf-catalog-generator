package pdf.components;

import static pdf.PDFStyleDefaults.BLACK_COLOR;

import org.apache.poi.ss.usermodel.Cell;

import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;

import pdf.themes.Theme;
import utils.ExcelUtils;
import utils.PDFUtils;

public class UxBComponent {
    public static Paragraph build(Cell cell, Theme theme) {
        try {
            String uxbText = PDFUtils.safeText(ExcelUtils.getCellValue(cell), "--");

            Text valorUxb = new Text(uxbText).simulateBold();

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