package pdf.components;

import org.apache.poi.ss.usermodel.Cell;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.element.Text;

import pdf.themes.Theme;
import utils.ExcelUtils;
import utils.PDFUtils;

import java.util.function.Consumer;

public class PrecioComponent {
    public static Paragraph build(Cell cell, Theme theme, float fontSize, Color fontColor, Consumer<String> log) {
        try {
            String precioText = PDFUtils.safeText(ExcelUtils.getCellValue(cell), "--");
            String precioValue = PDFUtils.formatPrice(precioText);

            Text precioBold = new Text(precioValue).simulateBold();

            return new Paragraph("PRECIO: \n")
                    .add(precioBold)
                    .setFontSize(fontSize)
                    .setFontColor(fontColor)
                    .setMultipliedLeading(1f)
                    .setPadding(1)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMargin(0);

        } catch (Exception e) {
            log.accept("Error al leer precio en celda: " + e.getMessage() + "\n");
            Text precioBold = new Text("--").simulateBold();

            return new Paragraph("PRECIO: \n")
                    .add(precioBold)
                    .setFontSize(fontSize)
                    .setFontColor(fontColor)
                    .setMultipliedLeading(1f)
                    .setPadding(1)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMargin(0);
        }
    }
}
