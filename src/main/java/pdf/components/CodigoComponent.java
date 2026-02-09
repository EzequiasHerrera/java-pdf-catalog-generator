package pdf.components;

import org.apache.poi.ss.usermodel.Cell;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.BorderRadius;
import com.itextpdf.layout.properties.HorizontalAlignment;

import pdf.themes.Theme;
import utils.ExcelUtils;
import utils.PDFUtils;

import java.util.function.Consumer;

public class CodigoComponent {
    public static Paragraph build(Cell cell, Theme theme, float availableWidthSpace, float fontSize, Color fontColor, Consumer<String> log) {
        try {
            String codigoValue = PDFUtils.safeText(ExcelUtils.getCellValue(cell), "[SIN CÓDIGO]");

            if (codigoValue.matches("^\\d+\\.0$")) {
                codigoValue = codigoValue.substring(0, codigoValue.length() - 2);
            }

            return new Paragraph(codigoValue)
                    .setMargin(0)
                    .setMarginBottom(2)
                    .setFontSize(fontSize)
                    .simulateBold()
                    .setFontColor(fontColor)
                    .setBorderRadius(new BorderRadius(10))
                    .setBackgroundColor(theme.codeBackgroundColor)
                    .setWidth(availableWidthSpace)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER);
        } catch (Exception e) {
            log.accept("Error al leer código en celda: " + e.getMessage() + "\n");
            return new Paragraph("CODIGO")
                    .setMargin(0)
                    .setMarginBottom(2)
                    .setFontSize(fontSize)
                    .simulateBold()
                    .setFontColor(fontColor)
                    .setBorderRadius(new BorderRadius(10))
                    .setBackgroundColor(theme.codeBackgroundColor)
                    .setWidth(80)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER);
        }
    }
}
