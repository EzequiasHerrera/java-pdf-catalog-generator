package pdf.components;

import org.apache.poi.ss.usermodel.Cell;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;

import pdf.themes.Theme;
import utils.ExcelUtils;
import utils.PDFUtils;

public class UxBComponent {
    public static Paragraph build(Cell cell, Theme theme, float fontSize, Color fontColor) {
        try {
            String rawText = PDFUtils.safeText(ExcelUtils.getCellValue(cell), "--");

            String uxbText;
            try {
                double value = Double.parseDouble(rawText);
                if (value == (int) value) {
                    uxbText = String.valueOf((int) value);
                } else {
                    uxbText = String.valueOf(value);
                }
            } catch (NumberFormatException e) {
                uxbText = rawText;
            }

            Text valorUxb = new Text(uxbText).simulateBold();

            return new Paragraph("UxB: ")
                    .add(valorUxb)
                    .setFontSize(fontSize)
                    .setFontColor(fontColor)
                    .setMultipliedLeading(1f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMargin(0)
                    .setPadding(1);
        } catch (Exception e) {
            return new Paragraph("UxB: ")
                    .add("-")
                    .setFontSize(fontSize)
                    .setFontColor(fontColor)
                    .setMultipliedLeading(1f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMargin(0)
                    .setPadding(1);
        }
    }
}