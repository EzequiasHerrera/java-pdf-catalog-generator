package utils;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import pdf.components.CardPortadaComponent;
import pdf.themes.Theme;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PDFUtils {

    // Funcion que agrega primer pagina del catalogo
    public static void addFirstPage(Document doc, float pageHeight, float pageWidth, String titleTextInput,
                                    String subtitleTextInput, Theme theme, boolean presupuestoActivo) {

        if (titleTextInput == null || titleTextInput.isBlank()) {
            if (presupuestoActivo) {
                titleTextInput = "PRESUPUESTO";
            } else {
                titleTextInput = "CATALOGO";
            }
        }

        Paragraph titulo = new Paragraph(titleTextInput)
                .setFontSize(50)
                .simulateBold()
                .setFontColor(theme.titleTextColor)
                .setTextAlignment(TextAlignment.CENTER)
                .setPaddingBottom(30)
                .setPaddingTop(30)
                .setMultipliedLeading(0.8f);

        if (subtitleTextInput == null || subtitleTextInput.isBlank()) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy");
            subtitleTextInput = formatter.format(LocalDate.now());
        }

        Paragraph subtitulo = new Paragraph(subtitleTextInput)
                .setFontSize(25)
                .simulateBold()
                .setFontColor(theme.subtitleTextColor)
                .setPadding(10)
                .setTextAlignment(TextAlignment.CENTER);

        Image logo = new Image(theme.logoImage);
        logo.setWidth(300);
        logo.setHorizontalAlignment(HorizontalAlignment.CENTER);
        logo.setMarginBottom(20);

        // 🔧 Calcular altura útil, sin márgenes
        float usableHeight = pageHeight - doc.getTopMargin() - doc.getBottomMargin();

        Div portada = new Div()
                .setHeight(usableHeight)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.CENTER);


        //DEBO AGREGAR LA PALABRA CLIENTE O CATALOGO SEGUN CORRESPONDA
        portada.add(logo);
        portada.add(CardPortadaComponent.build(titulo, subtitulo, presupuestoActivo));

        doc.add(portada);

        // 🔁 Salto de página explícito para evitar desbordes en la página 2
        doc.add(new AreaBreak());
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
