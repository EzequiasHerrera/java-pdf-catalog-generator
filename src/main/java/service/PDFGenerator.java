package service;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.event.PdfDocumentEvent;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import themes.KitchenToolsTheme;
import themes.LineageTheme;
import themes.Theme;
import utils.FooterHandler;
import utils.PDFUtils;

import java.io.File;

public class PDFGenerator {

    public static int generarPDF(
            File archivoExcel,
            File carpetaImagenes,
            File archivoCaratulaPdf,
            File archivoDestino,

            float imageSize,
            float pageWidth,
            float pageHeight,

            boolean codigoColumn,
            boolean productoColumn,
            boolean precioColumn,
            boolean unidadPorBultoColumn,

            boolean imagenes,

            TextArea logTextArea,
            int productoQuantity,
            String titleTextInput,
            String subtitleTextInput,
            String selectedTheme,
            boolean presupuestoActivo) throws Exception {

        int totalProducts = 0;
        final int productsPerPage = productoQuantity; // CANTIDAD DE PRODUCTOS QUE QUIERO POR PAGINA
        final StringBuilder log = new StringBuilder();
        final Theme theme = (selectedTheme.equalsIgnoreCase(KitchenToolsTheme.THEME_NAME)) ? KitchenToolsTheme.getTheme() : LineageTheme.getTheme();

        // IMAGENES CARGADAS SEGUN EL THEME
        final ImageData backgroundFirstPageImg = theme.backgroundFirstPageImage;
        final ImageData backgroundImg = theme.backgroundImage;

        try ( // EXCEL ----------------------------------------------
              final OPCPackage pkg = OPCPackage.open(archivoExcel, PackageAccess.READ);
              final XSSFWorkbook workbook = new XSSFWorkbook(pkg)) {

            final Sheet sheet = workbook.getSheetAt(0);
            final Row excelColumns = sheet.getRow(0);
            totalProducts = PDFUtils.countRowsInFile(sheet, log);
            // EXCEL ----------------------------------------------

            try (final PdfWriter writer = new PdfWriter(archivoDestino.getAbsolutePath());
                 final PdfDocument pdfDoc = new PdfDocument(writer);
                 final Document doc = new Document(pdfDoc, PageSize.A4)) {

                final PdfFont font = PdfFontFactory.createFont(); // Fuente por defecto
                final ImageData logoData = theme.logoImage; // Cargar logo desde recursos (classpath)
                final float fontSize = 10f; // tamaño de fuente para el pie de página
                final float y = 20f; // margen inferior

                pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, new FooterHandler(font, logoData, fontSize, y)); // Agregar pie de página a cada página

                if (PDFUtils.isValidExcel(excelColumns)) {

                    PDFUtils.setPDFBackground(pdfDoc, backgroundFirstPageImg, backgroundImg);

                    if (productsPerPage <= 12) {
                        doc.setMargins(10, 10, 10, 10);
                    } else {
                        doc.setMargins(10, 0, 0, 0);
                    }

                    PDFUtils.addFirstPage(doc, pageHeight, pageWidth, titleTextInput, subtitleTextInput, theme, presupuestoActivo);

                    int actualProductIndex = 1;

                    while (actualProductIndex <= totalProducts) {
                        // 🧱 Nueva tabla por página
                        // Diferencio entre 2, 3 y 5 columnas
                        final Table table = TableBuilder.createConfiguredTable(pageHeight, productsPerPage);

                        int itemsThisPage = 0;

                        // 🧠 Cargo hasta X productos POR PAGINA o hasta que no queden más
                        while (itemsThisPage < productsPerPage && actualProductIndex <= totalProducts) {

                            final Row row = sheet.getRow(actualProductIndex);
                            if (PDFUtils.isEmptyRow(row)) {
                                actualProductIndex++;
                                continue;
                            }

                            // VUELTA PAR O IMPAR (para saber si va a la izquierda o derecha)
                            boolean esPar = itemsThisPage % 2 == 0;

                            final Cell container = CellBuilder.createTest(
                                    sheet,
                                    actualProductIndex,
                                    codigoColumn,
                                    productoColumn,
                                    precioColumn,
                                    unidadPorBultoColumn,
                                    imagenes,
                                    carpetaImagenes,
                                    imageSize,
                                    pageWidth,
                                    pageHeight,
                                    itemsThisPage,
                                    log, productsPerPage, esPar, theme);

                            table.addCell(container);

                            actualProductIndex++;
                            itemsThisPage++;
                        }

                        // 📄 Agrego tabla con hasta 12 productos
                        doc.add(table);
//                    PDFUtils.addPageNumber(pdfDoc, font, fontSize, logoData, y); // Cambiado por FooterHandler

                        // ↪️ Si quedan productos, salto de página
                        if (actualProductIndex <= totalProducts) {
                            doc.add(new AreaBreak());
                        }
                    }

                }
            } catch (Exception e) {
                throw e;
            }
        } catch (Exception e) {
            throw e;
        }

        if (log.length() > 0) {
            Platform.runLater(() -> {
                if (logTextArea != null) {
                    logTextArea.setStyle("-fx-text-fill: #d3d700;");
                    logTextArea.appendText(log.toString());
                }
            });
        }
        return totalProducts - 1; // Resto 1 para no contar la fila de encabezados
    }
}
