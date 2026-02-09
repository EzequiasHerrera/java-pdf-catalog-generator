package fx;

import com.itextpdf.kernel.colors.Color;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.text.TextFlow;
import enums.PageType;
import service.PDFGenerationStats;
import service.PDFGenerator;

import java.io.File;

public class GeneratePDFService extends Service<PDFGenerationStats> {
    private final File archivoExcel;
    private final File carpetaImagenes;
    private final boolean caratula;
    private final File archivoDestino;

    // CONFIGURACION DE IMAGEN Y TAMAÑO DE PAGINA
    private final float imageSize;
    private final PageType pageType;

    // VARIABLES BOOLEAN DE CADA COLUMNA PARA SABER SI HAY QUE MOSTRARLA O NO
    private final boolean codigoColumn;
    private final boolean productoColumn;
    private final boolean precioColumn;
    private final boolean unidadPorBultoColumn;
    // VARIABLES BOOLEAN PARA SABER SI TIENE IMAGEN Y PARA TRAER EL TEXTAREA PARA EL
    // LOG
    private final boolean imagenes;
    private final TextFlow logTextFlow;
    private final int productoQuantity;
    private final String titleTextInput;
    private final String subtitleTextInput;

    private final String selectedTheme;
    private final boolean presupuestoActivo;

    // Font sizes
    private final float codigoFontSize;
    private final float productoFontSize;
    private final float precioFontSize;
    private final float uxbFontSize;

    // Font colors
    private final Color codigoColor;
    private final Color productoColor;
    private final Color precioColor;
    private final Color uxbColor;

    // CONSTRUCTOR
    public GeneratePDFService(
            // ENVÍO COMO PARAMETRO TODAS LAS VARIABLES AL CONSTRUCTOR GeneratePDFService
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
            TextFlow logTextFlow,
            int productoQuantity,
            String titleTextInput,
            String subtitleTextInput,
            String selectedTheme,
            boolean presupuestoActivo,
            float codigoFontSize,
            float productoFontSize,
            float precioFontSize,
            float uxbFontSize,
            Color codigoColor,
            Color productoColor,
            Color precioColor,
            Color uxbColor
    ) {

        // ASIGNO EL VALOR A CADA VARIABLE CON EL VALOR DE LAS VARIABLES QUE ENTRAN COMO
        // PARAMETROS EN LA FUNCION
        this.archivoExcel = archivoExcel;
        this.carpetaImagenes = carpetaImagenes;
        this.caratula = caratula;
        this.archivoDestino = archivoDestino;

        this.imageSize = imageSize;
        this.pageType = pageType;

        this.codigoColumn = codigoColumn;
        this.productoColumn = productoColumn;
        this.precioColumn = precioColumn;
        this.unidadPorBultoColumn = unidadPorBultoColumn;

        this.imagenes = imagenes;
        this.logTextFlow = logTextFlow;
        this.productoQuantity = productoQuantity;
        this.titleTextInput = titleTextInput;
        this.subtitleTextInput = subtitleTextInput;

        this.selectedTheme = selectedTheme;
        this.presupuestoActivo = presupuestoActivo;
        this.codigoFontSize = codigoFontSize;
        this.productoFontSize = productoFontSize;
        this.precioFontSize = precioFontSize;
        this.uxbFontSize = uxbFontSize;
        this.codigoColor = codigoColor;
        this.productoColor = productoColor;
        this.precioColor = precioColor;
        this.uxbColor = uxbColor;
    }

    @Override // Task es asincronismo. Permite correr en segundo plano
    //FUNCIÓN ASINCRONICA QUE DEVUELVE UN INT <INTEGER> QUE CONTIENE LA CANTIDAD DE PRODUCTOS GENERADOS
    protected Task<PDFGenerationStats> createTask() {
        return new Task<>() {
            @Override
            protected PDFGenerationStats call() throws Exception {
                return PDFGenerator.generarPDF(archivoExcel, carpetaImagenes, caratula, archivoDestino,
                        imageSize, pageType,
                        codigoColumn, productoColumn, precioColumn, unidadPorBultoColumn,
                        imagenes, logTextFlow, productoQuantity, titleTextInput, subtitleTextInput, selectedTheme, presupuestoActivo,
                        codigoFontSize, productoFontSize, precioFontSize, uxbFontSize,
                        codigoColor, productoColor, precioColor, uxbColor);
            }
        };
    }

}
