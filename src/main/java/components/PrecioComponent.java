package components;

import static utils.PDFStyleDefaults.BLACK_COLOR;

import org.apache.poi.ss.usermodel.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.element.Text;

import themes.Theme;
import utils.PDFUtils;

public class PrecioComponent {
    public static Paragraph build(Cell cell, Theme theme) {
        try {
            String precioText = PDFUtils.safeText(PDFUtils.getCellValue(cell), "--");
            String precioValue = PDFUtils.formatPrice(precioText);

            Text precioBold = new Text("$" + precioValue).setBold();

            return new Paragraph("PRECIO: \n")
            .add(precioBold)
                    .setFontSize(10)
                    .setFontColor(BLACK_COLOR)
                    .setMultipliedLeading(1f)
                    .setPadding(1)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMargin(0);

        } catch (Exception e) {
            Text precioBold = new Text("$--").setBold();

            return new Paragraph("PRECIO: \n")
                    .add(precioBold)
                    .setFontSize(10)
                    .setFontColor(BLACK_COLOR)
                    .setMultipliedLeading(1f)
                    .setPadding(1)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMargin(0);
        }
    }
}
