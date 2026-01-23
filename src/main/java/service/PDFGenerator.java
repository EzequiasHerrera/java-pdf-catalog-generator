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
        int productosGenerados = 0;

        try (final OPCPackage pkg = OPCPackage.open(archivoExcel, PackageAccess.READ);
             final XSSFWorkbook workbook = new XSSFWorkbook(pkg)) {

            final Sheet sheet = workbook.getSheetAt(0);
            final Row firstRow = sheet.getRow(0);

            if (ExcelUtils.isValidExcel(firstRow)) {
                int totalRows = ExcelUtils.countRowsInFile(sheet);
                if (totalRows < 2) {
                    throw new Exception("El archivo Excel no contiene productos. Debe tener al menos 1 producto además de los encabezados.");
                }

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

                    while (actualProductIndex <= totalRows) {
                        final Table table = TableBuilder.createConfiguredTable(pageHeight, productsPerPage);
                        int itemsThisPage = 0;

                        while (itemsThisPage < productsPerPage && actualProductIndex <= totalRows) {
                            final Row row = sheet.getRow(actualProductIndex);
                            if (ExcelUtils.isEmptyRow(row)) {
                                if (ExcelUtils.hasAnyData(row) && ExcelUtils.hasNoCode(row)) {
                                    log.append("Fila ").append(actualProductIndex + 1).append(" ignorada: no tiene código.\n");
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
                                    log, productsPerPage, esPar, theme);

                            table.addCell(container);

                            actualProductIndex++;
                            itemsThisPage++;
                            productosGenerados++;
                        }

                        if (itemsThisPage > 0) {
                            // Completar última fila con celdas vacías para evitar warning
                            int columnas = (productsPerPage <= 4) ? 1 : (productsPerPage <= 12) ? 3 : 5;
                            int celdasFaltantes = (columnas - (itemsThisPage % columnas)) % columnas;
                            for (int i = 0; i < celdasFaltantes; i++) {
                                table.addCell(new Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
                            }

                            doc.add(table);
                            if (itemsThisPage == productsPerPage && actualProductIndex <= totalRows) {
                                doc.add(new AreaBreak());
                            }
                        }
                    }
                }
            }
        }

        if (!log.isEmpty()) {
            if (logTextArea == null) {
                System.out.println(log);
            } else {
                Platform.runLater(() -> {
                    logTextArea.setStyle("-fx-text-fill: #d3d700;");
                    logTextArea.appendText(log.toString());
                });
            }
        }

        return productosGenerados;
    }

}
