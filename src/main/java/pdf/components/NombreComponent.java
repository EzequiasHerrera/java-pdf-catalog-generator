package pdf.components;

import org.apache.poi.ss.usermodel.Cell;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

import pdf.themes.Theme;

import utils.ExcelUtils;
import utils.PDFUtils;

public class NombreComponent {
    public static Paragraph build(Cell cell, Theme theme, float fontSize, Color fontColor) {
        try {
            String nombreValue = PDFUtils.safeText(ExcelUtils.getCellValue(cell), "[SIN NOMBRE]");

            return new Paragraph(nombreValue)
                    .simulateBold()
                    .setFontSize(fontSize)
                    .setFontColor(fontColor)
                    .setMultipliedLeading(1f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMargin(0)
                    .setPadding(2);

        } catch (Exception e) {
            return new Paragraph("[SIN NOMBRE]")
                    .simulateBold()
                    .setFontSize(fontSize)
                    .setFontColor(fontColor)
                    .setMultipliedLeading(1f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMargin(0)
                    .setPadding(2);
        }
    }
}
