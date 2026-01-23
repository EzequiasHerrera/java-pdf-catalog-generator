package service;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.event.PdfDocumentEvent;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;
import enums.PageType;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import pdf.BackgroundHandler;
import pdf.CellBuilder;
import pdf.FooterHandler;
import pdf.TableBuilder;
import pdf.themes.KitchenToolsTheme;
import pdf.themes.LineageTheme;
import pdf.themes.Theme;
import utils.ExcelUtils;
import utils.PDFUtils;

import java.io.File;

public class PDFGenerator {

    public static int generarPDF(
            File archivoExcel,
            File carpetaImagenes,
            boolean caratula,
            File archivoDestino,

            float imageSize,
            PageType pageType,

            boolean codigoColumn,
            boolean productoColumn,
            boolean precioColumn,
            boolean unidadPorBultoColumn,

            boolean imagenes,

            TextArea logTextArea,
            int productsPerPage, // CANTIDAD DE PRODUCTOS QUE QUIERO POR PAGINA
            String titleTextInput,
            String subtitleTextInput,
            String selectedTheme,
            boolean presupuestoActivo) throws Exception {

        final StringBuilder log = new StringBuilder();
        int totalProducts = 0;

        try ( // EXCEL ----------------------------------------------
              final OPCPackage pkg = OPCPackage.open(archivoExcel, PackageAccess.READ);
              final XSSFWorkbook workbook = new XSSFWorkbook(pkg)) {

            final Sheet sheet = workbook.getSheetAt(0);
            final Row firstRow = sheet.getRow(0);

            if (ExcelUtils.isValidExcel(firstRow)) {
                totalProducts = ExcelUtils.countRowsInFile(sheet);
                if (totalProducts < 2) {
                    throw new Exception("El archivo Excel no contiene productos. Debe tener al menos 1 producto además de los encabezados.");
                }
                // EXCEL ----------------------------------------------

                try (final PdfWriter writer = new PdfWriter(archivoDestino.getAbsolutePath());
                     final PdfDocument pdfDoc = new PdfDocument(writer);
                     final Document doc = new Document(pdfDoc, pageType.toPageSize())) {

                    // IMAGENES CARGADAS SEGUN EL THEME
                    final Theme theme = (selectedTheme.equalsIgnoreCase(KitchenToolsTheme.THEME_NAME)) ? KitchenToolsTheme.getTheme() : LineageTheme.getTheme();
                    final ImageData backgroundFirstPageImg = theme.backgroundFirstPageImage;
                    final ImageData backgroundImg = theme.backgroundImage;
                    final PdfFont font = PdfFontFactory.createFont(); // Fuente por defecto
                    final ImageData logoData = theme.logoImage; // Cargar logo desde recursos (classpath)
                    final float pageWidth = pageType.getWidth();
                    final float pageHeight = pageType.getHeight();

                    pdfDoc.addEventHandler(PdfDocumentEvent.START_PAGE, new BackgroundHandler(pdfDoc, backgroundFirstPageImg, backgroundImg, caratula)); // Agregar fondo a cada página
                    pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, new FooterHandler(font, logoData, caratula)); // Agregar pie de página a cada página

                    if (productsPerPage <= 12) {
                        doc.setMargins(10, 10, 10, 10);
                    } else {
                        doc.setMargins(10, 0, 0, 0);
                    }

                    if (caratula) {
                        PDFUtils.addFirstPage(doc, pageHeight, pageWidth, titleTextInput, subtitleTextInput, theme, presupuestoActivo);
                    }

                    int actualProductIndex = 1;

                    while (actualProductIndex <= totalProducts) {
                        // 🧱 Nueva tabla por página
                        // Diferencio entre 2, 3 y 5 columnas
                        final Table table = TableBuilder.createConfiguredTable(pageHeight, productsPerPage);

                        int itemsThisPage = 0;

                        // 🧠 Cargo hasta X productos POR PAGINA o hasta que no queden más
                        while (itemsThisPage < productsPerPage && actualProductIndex <= totalProducts) {

                            final Row row = sheet.getRow(actualProductIndex);
                            if (ExcelUtils.isEmptyRow(row)) {
                                // Informar si la fila tiene datos pero no tiene código
                                if (ExcelUtils.hasAnyData(row) && ExcelUtils.hasNoCode(row)) {
                                    log.append("Fila ").append(actualProductIndex + 1).append(" ignorada: no tiene código.\n");
                                }
                                actualProductIndex++;
                                continue;
                            }

                            // VUELTA PAR O IMPAR (para saber si va a la izquierda o derecha)
                            boolean esPar = itemsThisPage % 2 == 0;

                            final Cell container = CellBuilder.createCell(
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
                        // ↪️ Si quedan productos, salto de página
                        if (actualProductIndex <= totalProducts) {
                            doc.add(new AreaBreak());
                        }
                    }
                } catch (Exception e) {
                    throw e;
                }
            }
        } catch (Exception e) {
            throw e;
        }

        if (!log.isEmpty() && logTextArea == null) { // Imprime por consola
            System.out.println(log);
        } else if (!log.isEmpty()) { // Imprime por JavaFx
            Platform.runLater(() -> {
                logTextArea.setStyle("-fx-text-fill: #d3d700;");
                logTextArea.appendText(log.toString());
            });
        }
        if (totalProducts > 1) {
            return totalProducts - 1; // Resto 1 para no contar la fila de encabezados
        } else {
            return 0;
        }
    }

}
