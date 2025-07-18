package components;

import static utils.PDFStyleDefaults.WHITE_COLOR;

import org.apache.poi.ss.usermodel.Cell;

import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.BorderRadius;
import com.itextpdf.layout.properties.HorizontalAlignment;

import themes.Theme;
import utils.PDFUtils;

public class CodigoComponent {
    public static Paragraph build(Cell cell, Theme theme, float availableWidthSpace) {
        try {
            String codigoValue = PDFUtils.safeText(PDFUtils.getCellValue(cell), "[SIN CÓDIGO]");

            // 🔧 Si termina en ".0", lo sacamos
            if (codigoValue.matches("^\\d+\\.0$")) {
                codigoValue = codigoValue.substring(0, codigoValue.length() - 2);
            }

            return new Paragraph(codigoValue)
                    .setMargin(0)
                    .setMarginBottom(2)
                    .setBold()
                    .setFontColor(WHITE_COLOR)
                    .setBorderRadius(new BorderRadius(10))
                    .setBackgroundColor(theme.codeBackgroundColor)
                    .setWidth(availableWidthSpace)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER);
        } catch (Exception e) {
            return new Paragraph("CODIGO")
                    .setMargin(0)
                    .setMarginBottom(2)
                    .setBold()
                    .setFontColor(WHITE_COLOR)
                    .setBorderRadius(new BorderRadius(10))
                    .setBackgroundColor(theme.codeBackgroundColor)
                    .setWidth(80)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER);
        }
    }
}
