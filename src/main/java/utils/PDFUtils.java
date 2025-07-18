package utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.font.FontProvider;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.styledxmlparser.resolver.font.BasicFontProvider;

import themes.Theme;

public class PDFUtils {

    // Función que detecta si una fila está vacía
    public static boolean isEmptyRow(Row row) {
        for (int i = row.getFirstCellNum(); i <= row.getLastCellNum(); i++) {
            final org.apache.poi.ss.usermodel.Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    // Función que devuelve el valor de una celda
    public static String getCellValue(org.apache.poi.ss.usermodel.Cell cell) throws Exception {
        if (cell == null) {
            return "";
        }

        final CellType cellType = cell.getCellType();
        switch (cellType) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) { // date
                    return cell.getDateCellValue().toString();
                } else { // numeric
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                switch (cell.getCachedFormulaResultType()) {
                    case STRING:
                        return cell.getStringCellValue();
                    case NUMERIC:
                        if (DateUtil.isCellDateFormatted(cell)) {
                            return cell.getDateCellValue().toString();
                        } else {
                            return String.valueOf(cell.getNumericCellValue());
                        }
                    case BOOLEAN:
                        return String.valueOf(cell.getBooleanCellValue());
                    case ERROR:
                        return "0";
                    default:
                        return "";
                }
            case BLANK:
                return "";
            case ERROR:
                throw new Exception("Error en la celda fila: " + cell.getAddress().getRow() + 1 + " columna: "
                        + cell.getAddress().getColumn() + 1);
            default:
                return "";
        }
    }

    // Setea configuracion estética del documento
    public static void setDocument(Document doc) {
        final FontProvider fontProvider = new BasicFontProvider(true, true);
        doc.setFontProvider(fontProvider);
        doc.setMargins(0, 0, 0, 0);
    }

    // Funcion que agrega primer pagina del catalogo
    public static void addFirstPage(Document doc, float pageHeight, float pageWidth, String titleTextInput, String subtitleTextInput, Theme theme)
            throws Exception {

        if (titleTextInput == null || titleTextInput.trim().isEmpty()) {
            titleTextInput = "CATÁLOGO";
        }

        Paragraph titulo = new Paragraph(titleTextInput)
                .setFontSize(60)
                .setBold()
                .setFontColor(theme.titleTextColor)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(0)
                .setMultipliedLeading(0.8f);

        if (subtitleTextInput == null || subtitleTextInput.trim().isEmpty()) {
            String mes = LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, new Locale("es"));
            int anio = LocalDate.now().getYear();
            subtitleTextInput = mes + " " + anio;
        }

        Image logo = new Image(theme.logoImage);
        logo.setWidth(300);
        logo.setHorizontalAlignment(HorizontalAlignment.CENTER);
        logo.setMarginBottom(20);

        Paragraph subtitulo = new Paragraph(subtitleTextInput)
                .setFontSize(30)
                .setBold()
                .setFontColor(theme.titleTextColor)
                .setTextAlignment(TextAlignment.CENTER);

        // 🔧 Calcular altura útil, sin márgenes
        float usableHeight = pageHeight - doc.getTopMargin() - doc.getBottomMargin();

        Div wrapper = new Div()
                .setHeight(usableHeight)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.CENTER);

        wrapper.add(logo);
        wrapper.add(titulo);
        wrapper.add(subtitulo);

        doc.add(wrapper);

        // 🔁 Salto de página explícito para evitar desbordes en la página 2
        doc.add(new AreaBreak());
    }

    public static void addPageNumber(PdfDocument pdfDoc, int pageNumber, float pageWidth, Theme theme) {
        PdfPage page = pdfDoc.getPage(pageNumber);
        PdfCanvas canvas = new PdfCanvas(page);

        try {
            var font = PdfFontFactory.createFont(); // Fuente por defecto

            float fontSize = 10f;
            float y = 20f; // margen inferior

            float textX = pageWidth / 2 + 15; // Dejo un poco de espacio para el logo a la izquierda
            float logoX = pageWidth / 2 - 40; // Logo a la izquierda del texto

            // Dibujar texto centrado, con pequeño desplazamiento hacia la derecha
            canvas.beginText()
                    .setFontAndSize(font, fontSize)
                    .setColor(ColorConstants.DARK_GRAY, true)
                    .moveText(textX, y)
                    .showText("Página " + pageNumber)
                    .endText();

            // Cargar logo desde recursos (classpath)
            ImageData logoData = theme.logoImage;

            Rectangle rect = new Rectangle(logoX, y - 5, 30, 25); // ajustá el tamaño a gusto
            canvas.addImageFittedIntoRectangle(logoData, rect, false);

        } catch (Exception e) {
            System.err.println("Error al dibujar número de página y logo: " + e.getMessage());
        }
    }

    // Función que agrega un producto
    public static void addProduct(Row row, Map<String, String> productos) throws Exception {
        final double codigoValue = Double.parseDouble(getCellValue(row.getCell(0)));
        final String codigo = (codigoValue % 1 == 0) ? String.format("%.0f", codigoValue) : String.valueOf(codigoValue);
        final String producto = getCellValue(row.getCell(1));
        productos.put(codigo, producto);
    }

    // Verifica si el Excel contiene las 4 columnas CODIGO PRODUCTO PRECIO UXB
    public static boolean isValidExcel(Row firstRow) throws Exception {
        if (firstRow == null || firstRow.getLastCellNum() < 4) {
            throw new Exception(
                    "Verifique que la hoja tenga los 4 encabezados en orden. Código, Nombre, Precio y Unidad por Bulto.");
        }
        return true;
    }

    // Cuenta cantidad de productos en el Excel
    public static int countRowsInFile(org.apache.poi.ss.usermodel.Sheet sheet, StringBuilder log) {
        int rowCount = 0;
        for (Row row : sheet) {
            if (row != null && !PDFUtils.isEmptyRow(row)) {
                rowCount++;
            }
        }

        log.append(rowCount);

        return rowCount;
    }

    // Define el fondo del PDF
    public static void setPDFBackground(PdfDocument pdfDoc, ImageData portada, ImageData fondoGeneral) {
        pdfDoc.addEventHandler(PdfDocumentEvent.START_PAGE, new IEventHandler() {
            @Override
            public void handleEvent(Event event) {
                PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
                PdfPage page = docEvent.getPage();
                int pageNumber = pdfDoc.getPageNumber(page);
                Rectangle pageSize = page.getPageSize();

                PdfCanvas canvas = new PdfCanvas(
                        page.newContentStreamBefore(),
                        page.getResources(),
                        pdfDoc);

                // Elegimos la imagen según el número de página
                ImageData fondo = (pageNumber == 1) ? portada : fondoGeneral;

                canvas.addImageFittedIntoRectangle(fondo, pageSize, false);
            }
        });
    }

    // Agrega la tabla al documento
    public static void addTableToDocument(Table table, Document doc) {
        if (table.getNumberOfRows() > 0) {
            table.setExtendBottomRow(false);
            table.setMargin(0);
            table.setPadding(0);
            table.setFixedLayout();
            doc.add(table);
        }
    }

    public static String formatPrice(String rawPrice) {
        try {
            double price = Double.parseDouble(rawPrice.replace(",", "."));
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator(',');
            symbols.setGroupingSeparator('.');

            DecimalFormat formatter = new DecimalFormat("#,##0.00", symbols);
            return formatter.format(price);
        } catch (NumberFormatException e) {
            return "--";
        }
    }

    public static String safeText(String text, String fallback) {
        if (text == null || text.trim().isEmpty())
            return fallback;
        return text;
    }
}
