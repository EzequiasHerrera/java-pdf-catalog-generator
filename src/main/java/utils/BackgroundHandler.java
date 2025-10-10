package utils;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.event.AbstractPdfDocumentEvent;
import com.itextpdf.kernel.pdf.event.AbstractPdfDocumentEventHandler;
import com.itextpdf.kernel.pdf.event.PdfDocumentEvent;

public class BackgroundHandler extends AbstractPdfDocumentEventHandler {

    private final PdfDocument pdfDoc;
    private final ImageData backgroundFirstPageImg;
    private final ImageData backgroundImg;
    private final boolean caratula;

    public BackgroundHandler(PdfDocument pdfDoc, ImageData backgroundFirstPageImg, ImageData backgroundImg, boolean caratula) {
        this.pdfDoc = pdfDoc;
        this.backgroundFirstPageImg = backgroundFirstPageImg;
        this.backgroundImg = backgroundImg;
        this.caratula = caratula;
    }

    @Override
    protected void onAcceptedEvent(AbstractPdfDocumentEvent event) { // MODIFICADO PARA LA NUEVA VERSION
        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
        PdfPage page = docEvent.getPage();
        int pageNumber = pdfDoc.getPageNumber(page);
        Rectangle pageSize = page.getPageSize();

        PdfCanvas canvas = new PdfCanvas(
                page.newContentStreamBefore(),
                page.getResources(),
                pdfDoc);

        // Elegimos la imagen según el número de página
        ImageData fondo = (caratula && pageNumber == 1) ? backgroundFirstPageImg : backgroundImg;
        canvas.addImageFittedIntoRectangle(fondo, pageSize, false);
    }

}
