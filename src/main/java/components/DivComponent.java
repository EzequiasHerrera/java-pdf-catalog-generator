package components;

import static utils.PDFStyleDefaults.*;

import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;

import themes.Theme;

import com.itextpdf.layout.properties.BorderRadius;
import com.itextpdf.layout.borders.SolidBorder;

public class DivComponent {

    public static Div build(int productsPerPage, Theme theme, float availableWidthSpace) {
        if (productsPerPage == 2) {
            // height 362
            return new Div()
                    .setPadding(10)
                    .setMargin(0)
                    .setHeight(100)
                    .setWidth(availableWidthSpace - 60) // ESPACIO DISPONIBLE - MARGENES
                    .setBackgroundColor(LIGHT_GRAY)
                    .setBorderRadius(new BorderRadius(6))
                    .setTextAlignment(TextAlignment.CENTER) // ESTO ALINEA EL TEXTO DENTRO DE CADA CONTENEDOR DE TEXTO Y
                                                            // NO DENTRO DE LA CARD
                    .setVerticalAlignment(VerticalAlignment.MIDDLE); // ESTO ALINEA EL CONTENIDO DENTRO DE LA CARD
        } else if (productsPerPage == 4) {
            return new Div()
                    .setPadding(5)
                    .setMargin(0)
                    .setHeight(100)
                    .setWidth(availableWidthSpace / 2)
                    .setBackgroundColor(LIGHT_GRAY)
                    .setBorderRadius(new BorderRadius(6))
                    .setTextAlignment(TextAlignment.CENTER);
        } else if (productsPerPage == 12) {
            return new Div()
                    .setPadding(5)
                    .setPaddingTop(0)
                    .setMargin(0)
                    .setHeight(180)
                    .setWidth(availableWidthSpace / 3)
                    .setBorder(new SolidBorder(theme.cardBorderColor, 2.0f))
                    .setBorderRadius(new BorderRadius(6))
                    .setTextAlignment(TextAlignment.CENTER);
        } else {
            return new Div()
                    .setPadding(2)
                    .setPaddingTop(0)
                    .setMargin(0)
                    .setWidth(100)
                    .setHeight(180)
                    .setBorder(new SolidBorder(theme.cardBorderColor, 2.0f))
                    .setBorderRadius(new BorderRadius(6))
                    .setTextAlignment(TextAlignment.CENTER);
        }
    }
}
