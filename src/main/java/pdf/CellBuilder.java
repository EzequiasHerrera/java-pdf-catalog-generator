package pdf;

import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import pdf.components.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import pdf.themes.Theme;

import java.io.File;

public class CellBuilder {

    public static Cell createCell(Sheet sheet, int rowNumber, boolean codigoColumn, boolean productoColumn,
                                  boolean precioColumn, boolean unidadPorBultoColumn,
                                  boolean imagenes, File carpetaImagenes, float imageSize, float pageWidth, float pageHeight,
                                  float itemsThisPage,
                                  StringBuilder log, int productsPerPage, boolean esPar, Theme theme) throws Exception {

        Row row = sheet.getRow(rowNumber);

        float availableWidthSpace = pageWidth - imageSize;

        // ⬜ CARD CONTENEDOR
        Div card = DivComponent.build(productsPerPage, theme, availableWidthSpace);

        // 🟫 LINEA DIVISORIA
        Paragraph linea = LineComponent.build();

        // 🟨 CÓDIGO
        Paragraph codigo = CodigoComponent.build(row.getCell(0), theme, availableWidthSpace);

        // 🟩 NOMBRE PRODUCTO
        Paragraph nombre = NombreComponent.build(row.getCell(1), theme);

        // 🟥 PRECIO
        Paragraph precio = PrecioComponent.build(row.getCell(2), theme);

        // 🟪 UXB
        Paragraph uxb = UxBComponent.build(row.getCell(3), theme);

        // 🖼️ IMAGEN
        Image image = ImagenComponent.build(row.getCell(0), carpetaImagenes, imageSize, log);

        // 🧱 ARMADO FINAL
        if (productsPerPage <= 4) {
            card.add(codigo);
            card.add(linea);
            card.add(nombre);
            card.add(linea);
            card.add(precio);
            card.add(linea);
            card.add(uxb);

            // Armás la tabla horizontal con imagen y texto
            Table horizontalLayout = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .useAllAvailableWidth()
                    .setBorder(Border.NO_BORDER);

            horizontalLayout.setHeight((pageHeight - 60) / (productsPerPage == 2 ? 2 : 4));

            card.setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER);

            if (esPar) {
                horizontalLayout.addCell(new Cell()
                        .add(image)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setBorder(Border.NO_BORDER));

                horizontalLayout.addCell(new Cell()
                        .add(card)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setBorder(Border.NO_BORDER));
            } else {
                horizontalLayout.addCell(new Cell()
                        .add(card)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setBorder(Border.NO_BORDER));

                horizontalLayout.addCell(new Cell()
                        .add(image)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setBorder(Border.NO_BORDER));
            }

            Cell cell = new Cell()
                    .add(horizontalLayout)
                    .setBorder(Border.NO_BORDER)
                    .setPaddingTop(2);

            if (itemsThisPage + 1 <= 3 && productsPerPage == 4) {
                cell.add(linea);
            }

            if (itemsThisPage + 1 <= 1 && productsPerPage == 2) {
                cell.add(linea);
            }

            return cell;
        } else {
            // Distribución vertical tradicional
            card.add(codigo);
            card.add(image);
            card.add(linea);
            card.add(nombre);
            card.add(linea);
            card.add(precio);
            card.add(linea);
            card.add(uxb);

            return new Cell()
                    .add(card)
                    .setBorder(Border.NO_BORDER)
                    .setPadding(5);
        }
    }

}
