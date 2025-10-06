package utils;

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
    private final float fontSize;
    private final float y;

    public FooterHandler(PdfFont font, ImageData logoData, float fontSize, float y) {
        this.font = font;
        this.logoData = logoData;
        this.fontSize = fontSize;
        this.y = y;
    }

    @Override
    public void onAcceptedEvent(AbstractPdfDocumentEvent event) {
        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
        PdfPage page = docEvent.getPage();
        PdfDocument pdfDoc = docEvent.getDocument();
        int pageNumber = pdfDoc.getPageNumber(page);
        if (pageNumber > 1) { // Skip footer on the first page
            float pageWidth = page.getPageSize().getWidth();
            PdfCanvas canvas = new PdfCanvas(page);
            float textX = pageWidth / 2 + 15f;
            float logoX = pageWidth / 2 - 40f;

            canvas.beginText()
                    .setFontAndSize(font, fontSize)
                    .setColor(ColorConstants.DARK_GRAY, true)
                    .moveText(textX, y)
                    .showText("Página " + pageNumber)
                    .endText();

            Rectangle rect = new Rectangle(logoX, y - 5, 30, 25);
            canvas.addImageFittedIntoRectangle(logoData, rect, false);
        }
    }

}
