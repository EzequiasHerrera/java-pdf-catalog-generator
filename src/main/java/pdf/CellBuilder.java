package pdf;

import com.itextpdf.kernel.colors.Color;
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
import java.util.function.Consumer;
import service.PDFGenerationStats;

public class CellBuilder {

    public static Cell createCell(Sheet sheet, int rowNumber, boolean codigoColumn, boolean productoColumn,
                                  boolean precioColumn, boolean unidadPorBultoColumn,
                                  boolean imagenes, File carpetaImagenes, float imageSize, float pageWidth, float pageHeight,
                                  float itemsThisPage,
                                  Consumer<String> log, int productsPerPage, boolean esPar, Theme theme,
                                  float codigoFontSize, float productoFontSize, float precioFontSize, float uxbFontSize,
                                  Color codigoColor, Color productoColor, Color precioColor, Color uxbColor,
                                  PDFGenerationStats stats) throws Exception {

        Row row = sheet.getRow(rowNumber);

        float availableWidthSpace = imagenes ? pageWidth - imageSize : pageWidth;

        Div card = DivComponent.build(productsPerPage, theme, availableWidthSpace);

        Paragraph codigo = codigoColumn ? CodigoComponent.build(row.getCell(0), theme, availableWidthSpace, codigoFontSize, codigoColor, log) : null;
        Paragraph nombre = productoColumn ? NombreComponent.build(row.getCell(1), theme, productoFontSize, productoColor, log) : null;
        Paragraph precio = precioColumn ? PrecioComponent.build(row.getCell(2), theme, precioFontSize, precioColor, log) : null;
        Paragraph uxb = unidadPorBultoColumn ? UxBComponent.build(row.getCell(3), theme, uxbFontSize, uxbColor, log) : null;
        Image image = imagenes ? ImagenComponent.build(row.getCell(0), carpetaImagenes, imageSize, log, stats) : null;

        if (productsPerPage <= 4) {
            // Armar card con componentes visibles y lineas entre ellos
            boolean hasContent = false;
            if (codigo != null) { card.add(codigo); hasContent = true; }
            if (nombre != null) {
                if (hasContent) card.add(LineComponent.build());
                card.add(nombre); hasContent = true;
            }
            if (precio != null) {
                if (hasContent) card.add(LineComponent.build());
                card.add(precio); hasContent = true;
            }
            if (uxb != null) {
                if (hasContent) card.add(LineComponent.build());
                card.add(uxb);
            }

            Table horizontalLayout = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .useAllAvailableWidth()
                    .setBorder(Border.NO_BORDER);

            horizontalLayout.setHeight((pageHeight - 60) / (productsPerPage == 2 ? 2 : 4));

            card.setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER);

            if (image != null) {
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
            } else {
                horizontalLayout.addCell(new Cell(1, 2)
                        .add(card)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setBorder(Border.NO_BORDER));
            }

            Cell cell = new Cell()
                    .add(horizontalLayout)
                    .setBorder(Border.NO_BORDER)
                    .setPaddingTop(2);

            if (itemsThisPage + 1 <= 3 && productsPerPage == 4) {
                cell.add(LineComponent.build());
            }

            if (itemsThisPage + 1 <= 1 && productsPerPage == 2) {
                cell.add(LineComponent.build());
            }

            return cell;
        } else {
            // Distribucion vertical tradicional
            boolean hasContent = false;
            if (codigo != null) { card.add(codigo); hasContent = true; }
            if (image != null) card.add(image);
            if (nombre != null) {
                if (hasContent) card.add(LineComponent.build());
                card.add(nombre); hasContent = true;
            }
            if (precio != null) {
                if (hasContent) card.add(LineComponent.build());
                card.add(precio); hasContent = true;
            }
            if (uxb != null) {
                if (hasContent) card.add(LineComponent.build());
                card.add(uxb);
            }

            return new Cell()
                    .add(card)
                    .setBorder(Border.NO_BORDER)
                    .setPadding(5);
        }
    }

}
