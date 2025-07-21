package service;

import java.io.File;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;

public class GeneratePDFService extends Service<Integer> {
    private final File archivoExcel;
    private final File carpetaImagenes;
    private final File archivoPdf;
    private final File archivoDestino;

    // CONFIGURACION DE IMAGEN Y TAMAÑO DE PAGINA
    private final float imageSize;
    private final float pageWidth;
    private final float pageHeight;

    // VARIABLES BOOLEAN DE CADA COLUMNA PARA SABER SI HAY QUE MOSTRARLA O NO
    private final boolean codigoColumn;
    private final boolean productoColumn;
    private final boolean precioColumn;
    private final boolean unidadPorBultoColumn;
    // VARIABLES BOOLEAN PARA SABER SI TIENE IMAGEN Y PARA TRAER EL TEXTAREA PARA EL
    // LOG
    private final boolean imagenes;
    private final TextArea logTextArea;
    private final int productoQuantity;
    private final String titleTextInput;
    private final String subtitleTextInput;

    private final String selectedTheme;
    private final boolean presupuestoActivo;

    // CONSTRUCTOR
    public GeneratePDFService(
            // ENVÍO COMO PARAMETRO TODAS LAS VARIABLES AL CONSTRUCTOR GeneratePDFService

            File archivoExcel,
            File carpetaImagenes,
            File archivoPdf,
            File archivoDestino,

            float imageSize,
            float pageWidth,
            float pageHeight,

            boolean codigoColumn,
            boolean productoColumn,
            boolean precioColumn,
            boolean unidadPorBultoColumn,

            boolean imagenes,
            TextArea logTextArea,
            int productoQuantity,
            String titleTextInput,
            String subtitleTextInput,
            String selectedTheme,
            boolean presupuestoActivo
            ) {

        // ASIGNO EL VALOR A CADA VARIABLE CON EL VALOR DE LAS VARIABLES QUE ENTRAN COMO
        // PARAMETROS EN LA FUNCION
        this.archivoExcel = archivoExcel;
        this.carpetaImagenes = carpetaImagenes;
        this.archivoPdf = archivoPdf;
        this.archivoDestino = archivoDestino;

        this.imageSize = imageSize;
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;

        this.codigoColumn = codigoColumn;
        this.productoColumn = productoColumn;
        this.precioColumn = precioColumn;
        this.unidadPorBultoColumn = unidadPorBultoColumn;

        this.imagenes = imagenes;
        this.logTextArea = logTextArea;
        this.productoQuantity = productoQuantity;
        this.titleTextInput = titleTextInput;
        this.subtitleTextInput = subtitleTextInput;

        this.selectedTheme = selectedTheme;
        this.presupuestoActivo = presupuestoActivo;
    }

    @Override // Task es asincronismo. Permite correr en segundo plano
    //FUNCIÓN ASINCRONICA QUE DEVUELVE UN INT <INTEGER> QUE CONTIENE LA CANTIDAD DE PRODUCTOS GENERADOS
    protected Task<Integer> createTask() {
        return new Task<>() {
            @Override
            protected Integer call() throws Exception {
                return PDFGenerator.generarPDF(archivoExcel, carpetaImagenes, archivoPdf, archivoDestino,
                        imageSize, pageWidth, pageHeight,
                        codigoColumn, productoColumn, precioColumn, unidadPorBultoColumn,
                        imagenes, logTextArea, productoQuantity, titleTextInput, subtitleTextInput, selectedTheme, presupuestoActivo);
            }
        };
    }

}
