package pdf;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.event.AbstractPdfDocumentEvent;
import com.itextpdf.kernel.pdf.event.AbstractPdfDocumentEventHandler;
import com.itextpdf.kernel.pdf.event.PdfDocumentEvent;

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
        Rectangle rect = new Rectangle(logoX, y - ((float) 25 / 2), 30, 25);
        canvas.addImageFittedIntoRectangle(logoData, rect, false);
    }

}

