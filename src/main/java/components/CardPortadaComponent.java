package components;

import static utils.PDFStyleDefaults.GRAY_COLOR;
import static utils.PDFStyleDefaults.WHITE_COLOR;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.BorderRadius;
import com.itextpdf.layout.properties.HorizontalAlignment;

public class CardPortadaComponent {
    public static Div build(Paragraph titulo, Paragraph subtitulo) {

        Div separator = new Div()
                .setWidth(350)
                .setHeight(1)
                .setBackgroundColor(ColorConstants.LIGHT_GRAY);

        return new Div()
                // .setHeight(100)
                .setWidth(350)
                .setBackgroundColor(WHITE_COLOR)
                .setBorderRadius(new BorderRadius(15f))
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 2))
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .add(new Paragraph("CLIENTE")
                        .setBold()
                        .setPadding(10)
                        .setFontSize(23)
                        .setCharacterSpacing(1)
                        .setFontColor(GRAY_COLOR))
                .add(separator)
                .add(titulo)
                .add(separator)
                .add(subtitulo);
    }
}