package components;

import static utils.PDFStyleDefaults.BLACK_COLOR;

import org.apache.poi.ss.usermodel.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

import themes.Theme;

import utils.PDFUtils;

public class NombreComponent {
    public static Paragraph build(Cell cell, Theme theme) {
        try {
            String nombreValue = PDFUtils.safeText(PDFUtils.getCellValue(cell), "[SIN NOMBRE]");

            return new Paragraph(nombreValue)
                    .setBold()
                    .setFontSize(10)
                    .setFontColor(BLACK_COLOR)
                    .setMultipliedLeading(1f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMargin(0)
                    .setPadding(2);

        } catch (Exception e) {
            return new Paragraph("[SIN NOMBRE]")
                    .setBold()
                    .setFontSize(10)
                    .setFontColor(BLACK_COLOR)
                    .setMultipliedLeading(1f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMargin(0)
                    .setPadding(2);
        }
    }
}
