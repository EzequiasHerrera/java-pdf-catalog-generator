package utils;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.event.AbstractPdfDocumentEvent;
import com.itextpdf.kernel.pdf.event.AbstractPdfDocumentEventHandler;
import com.itextpdf.kernel.pdf.event.PdfDocumentEvent;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;

public class FooterHandler extends AbstractPdfDocumentEventHandler {

    private final PdfFont font;
    private final ImageData logoData;

    private final boolean caratula;

    public FooterHandler(PdfFont font, ImageData logoData, boolean caratula) {
        this.font = font;
        this.logoData = logoData;

        this.caratula = caratula;
    }

    @Override
    public void onAcceptedEvent(AbstractPdfDocumentEvent event) {
        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
        PdfDocument pdfDoc = docEvent.getDocument();
        PdfPage page = docEvent.getPage();
        int pageNumber = pdfDoc.getPageNumber(page);

        // Si hay carátula, omitimos solo la primera página
        if (caratula && pageNumber == 1) return;

        drawFooter(page, pageNumber);
    }

    private void drawFooter(PdfPage page, int pageNumber) {
        float pageWidth = page.getPageSize().getWidth();
        PdfCanvas canvas = new PdfCanvas(page);

        final float fontSize = 10f; // tamaño de fuente para el pie de página
        final float y = 20f; // margen inferior
        float textX = pageWidth / 2;
        float logoX = pageWidth / 2 - 40;

        // Texto
        canvas.beginText()
                .setFontAndSize(font, fontSize)
                .setColor(ColorConstants.DARK_GRAY, true)
                .moveText(textX, y)
                .showText("Página " + pageNumber)
                .endText();

        // Imagen
        Rectangle rect = new Rectangle(logoX, y - (25 / 2), 30, 25);
        canvas.addImageFittedIntoRectangle(logoData, rect, false);
    }

//    private void agregarNumeroPagina(PdfDocument pdfDoc, Document doc) {
//        final int numberOfPages = pdfDoc.getNumberOfPages();
//        final Style fontStyle = new Style().setFontSize(5).setFontColor(new DeviceRgb(128, 128, 128));
//        for (int i = 1; i <= numberOfPages; i++) {
//            // Write aligned text to the specified parameters point
//            doc.showTextAligned(new Paragraph(String.format("Página %s de %s", i, numberOfPages)).addStyle(fontStyle),
//                    pageWidth / 2, 2.5f, i, TextAlignment.CENTER, VerticalAlignment.MIDDLE, 0);
//        }
}

