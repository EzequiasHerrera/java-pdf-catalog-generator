package pdf.components;

import static pdf.PDFStyleDefaults.GRAY_COLOR;
import static pdf.PDFStyleDefaults.WHITE_COLOR;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.BorderRadius;
import com.itextpdf.layout.properties.HorizontalAlignment;

public class CardPortadaComponent {

    public final static float CARD_WIDTH = 350f;
    public final static float BORDER_WIDTH = 2f;

    public static Div build(Paragraph titulo, Paragraph subtitulo, boolean presupuestoActivo) {

        final Div separator1 = new Div()
                .setWidth(CARD_WIDTH)
                .setHeight(1)
                .setBackgroundColor(ColorConstants.LIGHT_GRAY);

        final Div separator2 = new Div()
                .setWidth(CARD_WIDTH)
                .setHeight(1)
                .setBackgroundColor(ColorConstants.LIGHT_GRAY);

        String textoSuperior;
        if (presupuestoActivo) {
            textoSuperior = "PRESUPUESTO";
        } else {
            textoSuperior = "CATALOGO";
        }

        return new Div()
                // .setHeight(100)
                .setWidth(CARD_WIDTH)
                .setBackgroundColor(WHITE_COLOR)
                .setBorderRadius(new BorderRadius(15f))
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, BORDER_WIDTH))
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .add(new Paragraph(textoSuperior)
                        .simulateBold()
                        .setPadding(10)
                        .setFontSize(23)
                        .setCharacterSpacing(1)
                        .setFontColor(GRAY_COLOR))
                .add(separator1)
                .add(titulo)
                .add(separator2)
                .add(subtitulo);
    }

}