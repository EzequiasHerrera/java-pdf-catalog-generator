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
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.function.Consumer;
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

    public static PDFGenerationStats generarPDF(
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

            TextFlow logTextFlow,
            int productsPerPage, // CANTIDAD DE PRODUCTOS QUE QUIERO POR PAGINA
            String titleTextInput,
            String subtitleTextInput,
            String selectedTheme,
            boolean presupuestoActivo,
            float codigoFontSize,
            float productoFontSize,
            float precioFontSize,
            float uxbFontSize,
            com.itextpdf.kernel.colors.Color codigoColor,
            com.itextpdf.kernel.colors.Color productoColor,
            com.itextpdf.kernel.colors.Color precioColor,
            com.itextpdf.kernel.colors.Color uxbColor) throws Exception {

        final Consumer<String> log;
        if (logTextFlow == null) {
            log = System.out::println;
        } else {
            log = message -> Platform.runLater(() -> {
                Text text = new Text(message);
                text.setFill(Color.web("#d3d700"));
                logTextFlow.getChildren().add(text);
            });
        }

        final PDFGenerationStats stats = new PDFGenerationStats();

        try (final OPCPackage pkg = OPCPackage.open(archivoExcel, PackageAccess.READ);
             final XSSFWorkbook workbook = new XSSFWorkbook(pkg)) {

            final Sheet sheet = workbook.getSheetAt(0);
            final Row firstRow = sheet.getRow(0);

            if (ExcelUtils.isValidExcel(firstRow)) {
                int totalDataRows = ExcelUtils.countRowsInFile(sheet);
                if (totalDataRows < 2) {
                    throw new Exception("El archivo Excel no contiene productos. Debe tener al menos 1 producto además de los encabezados.");
                }
                final int lastRowIndex = sheet.getLastRowNum();

                try (final PdfWriter writer = new PdfWriter(archivoDestino.getAbsolutePath());
                     final PdfDocument pdfDoc = new PdfDocument(writer);
                     final Document doc = new Document(pdfDoc, pageType.toPageSize())) {

                    final Theme theme = selectedTheme.equalsIgnoreCase(KitchenToolsTheme.THEME_NAME)
                            ? KitchenToolsTheme.getTheme()
                            : LineageTheme.getTheme();
                    final ImageData backgroundFirstPageImg = theme.backgroundFirstPageImage;
                    final ImageData backgroundImg = theme.backgroundImage;
                    final PdfFont font = PdfFontFactory.createFont();
                    final ImageData logoData = theme.logoImage;
                    final float pageWidth = pageType.getWidth();
                    final float pageHeight = pageType.getHeight();

                    pdfDoc.addEventHandler(PdfDocumentEvent.START_PAGE,
                            new BackgroundHandler(pdfDoc, backgroundFirstPageImg, backgroundImg, caratula));
                    pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE,
                            new FooterHandler(font, logoData, caratula));

                    if (productsPerPage <= 12) {
                        doc.setMargins(10, 10, 10, 10);
                    } else {
                        doc.setMargins(10, 0, 0, 0);
                    }

                    if (caratula) {
                        PDFUtils.addFirstPage(doc, pageHeight, pageWidth, titleTextInput, subtitleTextInput, theme, presupuestoActivo);
                    }

                    int actualProductIndex = 1;

                    while (actualProductIndex <= lastRowIndex) {
                        final Table table = TableBuilder.createConfiguredTable(pageHeight, productsPerPage);
                        int itemsThisPage = 0;

                        while (itemsThisPage < productsPerPage && actualProductIndex <= lastRowIndex) {
                            final Row row = sheet.getRow(actualProductIndex);
                            if (ExcelUtils.isEmptyRow(row)) {
                                if (ExcelUtils.hasAnyData(row) && ExcelUtils.hasNoCode(row)) {
                                    stats.filasIgnoradas++;
                                    log.accept("Fila " + (actualProductIndex + 1) + " ignorada: no tiene código.\n");
                                }
                                actualProductIndex++;
                                continue;
                            }

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
                                    log, productsPerPage, esPar, theme,
                                    codigoFontSize, productoFontSize, precioFontSize, uxbFontSize,
                                    codigoColor, productoColor, precioColor, uxbColor,
                                    stats);

                            table.addCell(container);

                            actualProductIndex++;
                            itemsThisPage++;
                            stats.productosGenerados++;
                        }

                        if (itemsThisPage > 0) {
                            // Completar última fila con celdas vacías para evitar warning
                            int columnas = (productsPerPage <= 4) ? 1 : (productsPerPage <= 12) ? 3 : 5;
                            int celdasFaltantes = (columnas - (itemsThisPage % columnas)) % columnas;
                            for (int i = 0; i < celdasFaltantes; i++) {
                                table.addCell(new Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
                            }

                            doc.add(table);
                            if (itemsThisPage == productsPerPage && actualProductIndex <= lastRowIndex) {
                                doc.add(new AreaBreak());
                            }
                        }
                    }
                }
            }
        }

        log.accept(stats.toSummary());
        return stats;
    }

}
