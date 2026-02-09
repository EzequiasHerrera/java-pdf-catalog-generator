package cmd;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import enums.PageType;
import enums.ProductQuantity;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import pdf.PDFStyleDefaults;
import service.PDFGenerationStats;
import service.PDFGenerator;
import utils.ExcelUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// Clase principal cuando se ejecuta desde línea de comandos
public class CommandLine {

    private static final Path JACOB_DLL = Paths.get(System.getenv("PROGRAMDATA"), "SuperMaster", "libs", "jacob-1.21-x64.dll");
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss");

    private File carpetaImagenes; // Directorio donde encontrar las imagenes
    private File archivoMasterExcel; // Archivo SUPER MASTER Excel

    private PageType pageType; // TAMAÑO DE PÁGINA
    private boolean codigoCheckBox; // CODIGO
    private boolean productoCheckBox; // NOMBRE
    private boolean precioCheckBox; // PRECIO
    private boolean unidadPorBultoCheckBox; // UXB
    private boolean imagenCheckBox; // IMAGEN

    public static void main(String[] args) {
        try {
            Configurator.setLevel("com.itextpdf", org.apache.logging.log4j.Level.ERROR); // Suprime logs de iText
            CommandLine app = new CommandLine();
            System.out.println(app.dtf.format(LocalDateTime.now()) + ": Catálogo PDF Generator iniciado(consola).");
            app.loadDefaultValues();
            final List<HashMap<String, Object>> parametros = app.obtenerParametros();
            if (!parametros.isEmpty()) {
                app.filtrarOrdenarExportar(parametros);
                app.generarCatalogo(parametros);
            } else {
                throw new Exception("La hoja 'Parametros' está vacía.");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1); // Salir con código de error para n8n
        }
    }

    private String getJarFolder() throws URISyntaxException {
        URI uri = CommandLine.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI();

        Path path = Paths.get(uri); // esto soporta UNC
        return path.getParent().toString();
    }

    private void loadDefaultValues() {
        pageType = PageType.A4;
        codigoCheckBox = true;
        productoCheckBox = true;
        precioCheckBox = true;
        unidadPorBultoCheckBox = true;
        imagenCheckBox = true;
    }

    public List<HashMap<String, Object>> obtenerParametros() throws Exception {

        final File excel = new File(this.getJarFolder() + File.separator + "Parametros.xlsx");

        if (excel.isFile()) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy");
            final List<HashMap<String, Object>> parametros = new ArrayList<>();

            try (final OPCPackage pkg = OPCPackage.open(excel, PackageAccess.READ);
                 final XSSFWorkbook workbook = new XSSFWorkbook(pkg)) {

                // HOJA UBICACIONES
                final Sheet ubicacionesSheet = workbook.getSheetAt(1); // 2° hoja

                Row row = ubicacionesSheet.getRow(1); // 2° fila
                if (ExcelUtils.isEmptyRow(row)) {
                    throw new Exception("La hoja 'Ubicaciones' está vacía.");
                }

                archivoMasterExcel = Paths.get(ExcelUtils.getCellValue(row.getCell(0))).toFile(); // A
                carpetaImagenes = Paths.get(ExcelUtils.getCellValue(row.getCell(1))).toFile(); // B

                System.out.println("------------------------------------------------------------------------------------------------------------------");
                System.out.println(dtf.format(LocalDateTime.now()) + ": Ubicaciones cargadas:"
                        + "\n-Archivo Super Master: " + archivoMasterExcel.getAbsolutePath()
                        + "\n-Imágenes: " + carpetaImagenes.getAbsolutePath());

                if (validarUbicaciones()) {
                    // HOJA PARAMETROS
                    final Sheet parametrosSheet = workbook.getSheetAt(0); // 1° hoja
                    final int lastRowIndex = parametrosSheet.getLastRowNum();

                    for (int r = 1; r <= lastRowIndex; r++) { // Empiezo en la fila 2
                        row = parametrosSheet.getRow(r);
                        if (ExcelUtils.isEmptyRow(row)) {
                            continue;
                        }
                        // LEER CELDAS
                        String listaPrecios = ExcelUtils.getCellValue(row.getCell(0)); //
                        String mixProductos = ExcelUtils.getCellValue(row.getCell(1)); //
                        String clasificacion = ExcelUtils.getCellValue(row.getCell(2)); //
                        String caratulaValue = ExcelUtils.getCellValue(row.getCell(3));
                        boolean caratula = caratulaValue.equalsIgnoreCase("SI"); //
                        if (!caratula && !caratulaValue.equalsIgnoreCase("NO") && !caratulaValue.isBlank()) {
                            System.err.println("WARNING Fila " + (r + 1) + ": Valor de carátula '" + caratulaValue + "' no reconocido (se esperaba 'SI' o 'NO'). Se usó 'NO' por defecto.");
                        }
                        String title = ExcelUtils.getCellValue(row.getCell(4)); //
                        String selectedTheme = ExcelUtils.getCellValue(row.getCell(5)); //
                        if (!selectedTheme.equalsIgnoreCase("KT") && !selectedTheme.equalsIgnoreCase("LINEA GE") && !selectedTheme.isBlank()) {
                            System.err.println("WARNING Fila " + (r + 1) + ": Tema '" + selectedTheme + "' no reconocido (se esperaba 'KT' o 'LINEA GE'). Se usará 'LINEA GE' por defecto.");
                        }
                        String presupuestoValue = ExcelUtils.getCellValue(row.getCell(6));
                        boolean presupuesto = presupuestoValue.equalsIgnoreCase("PRESUPUESTO"); //
                        if (!presupuesto && !presupuestoValue.equalsIgnoreCase("CATALOGO") && !presupuestoValue.isBlank()) {
                            System.err.println("WARNING Fila " + (r + 1) + ": Valor de presupuesto '" + presupuestoValue + "' no reconocido (se esperaba 'PRESUPUESTO' o 'CATALOGO'). Se usó 'CATALOGO' por defecto.");
                        }
                        int productoQuantity = (int) Double.parseDouble(ExcelUtils.getCellValue(row.getCell(7))); //
                        String subtitle = formatter.format(LocalDate.now()); // Fecha actual
                        String carpetaDestino = ExcelUtils.getCellValue(row.getCell(8)); //

                        // SETEAR TAMAÑO DE IMAGEN DEPENDIENDO LA CANTIDAD DE PRODUCTOS
                        final float imageSize = ProductQuantity.fromQuantity(productoQuantity).getImageSize();
                        // GUARDAR FILA EN LISTA DE PARAMETROS
                        final HashMap<String, Object> fila = new HashMap<>();
                        fila.put("listaPrecios", listaPrecios);
                        fila.put("mixProductos", mixProductos);
                        fila.put("clasificacion", clasificacion);
                        fila.put("caratula", caratula);
                        fila.put("selectedTheme", selectedTheme);
                        fila.put("productoQuantity", productoQuantity);
                        fila.put("imageSize", imageSize);
                        fila.put("presupuesto", presupuesto);
                        fila.put("title", title);
                        fila.put("subtitle", subtitle);
                        fila.put("carpetaDestino", carpetaDestino);
                        parametros.add(fila);

                        System.out.println("------------------------------------------------------------------------------------------------------------------");
                        System.out.println(dtf.format(LocalDateTime.now()) + ": Parámetros cargados:"
                                + "\n-Lista de precios: " + listaPrecios
                                + "\n-Mix productos: " + mixProductos
                                + "\n-Clasificación: " + clasificacion
                                + "\n-Tema: " + selectedTheme
                                + "\n-Cantidad de productos: " + productoQuantity
                                + "\n-Presupuesto: " + (presupuesto ? "Sí" : "No")
                                + "\n-Carátula: " + (caratula ? "Sí" : "No")
                                + "\n-Título: " + title
                                + "\n-Subtítulo: " + subtitle
                                + "\n-Carpeta destino: " + carpetaDestino);
                    }
                }
                return parametros;
            } catch (Exception e) {
                throw e;
            }
        } else {
            throw new Exception("No se encontró el archivo 'Parametros.xlsx' en: " + excel.getAbsolutePath());
        }
    }

    // Filtrar, ordenar y exportar usando las macros de Excel (JACOB)
    public void filtrarOrdenarExportar(List<HashMap<String, Object>> parametros) throws URISyntaxException {
        // DLL de JACOB
        if (!Files.exists(JACOB_DLL)) {
            throw new RuntimeException("No se encontró el archivo DLL en: " + JACOB_DLL);
        }
        System.setProperty("jacob.dll.path", JACOB_DLL.toAbsolutePath().toString());

        ActiveXComponent xl = null;
        try {
            xl = new ActiveXComponent("Excel.Application");
            xl.setProperty("Visible", false);
            Dispatch.put(xl, "DisplayAlerts", false); // Desactivar alertas
            Dispatch.put(xl, "EnableEvents", false); // Desactivar eventos

            Dispatch workbooks = xl.getProperty("Workbooks").toDispatch();
            Dispatch original = Dispatch.call(workbooks, "Open", archivoMasterExcel.getAbsolutePath()).toDispatch();

            // Setea variable para que macros no muestren mensajes
            Dispatch.call(xl, "Run", "SetMostrarMensajes", new Variant(false));

            System.out.println("------------------------------------------------------------------------------------------------------------------");
            System.out.println(dtf.format(LocalDateTime.now()) + ": Actualizando datos " + archivoMasterExcel.getName() + "...");
            Dispatch.call(xl, "Run", "ActualizarTodo");

            for (HashMap<String, Object> map : parametros) {
                final String listaPrecios = (String) map.get("listaPrecios");
                final String mixProductos = (String) map.get("mixProductos");
                final String clasificacion = (String) map.get("clasificacion");
                if (listaPrecios == null || mixProductos == null) continue;

                System.out.println(dtf.format(LocalDateTime.now()) + ": Procesando..." +
                        " (Lista de precios: " + listaPrecios + " - Mix productos: " + mixProductos + " - Clasificación: " + clasificacion + ")");

                // Filtrar y ordenar
                System.out.println(dtf.format(LocalDateTime.now()) + ": Filtrando y ordenando datos...");
                if (clasificacion == null || clasificacion.isBlank()) {
                    Dispatch.call(xl, "Run", "FiltrarYOrdenar", new Variant(mixProductos));
                } else {
                    Dispatch.call(xl, "Run", "FiltrarYOrdenar", new Variant(mixProductos), new Variant(clasificacion));
                }

                // Exportar
                System.out.println(dtf.format(LocalDateTime.now()) + ": Exportando datos filtrados...");
                Dispatch.call(xl, "Run", "ExportarFiltradosDesdeSUPERMASTER", new Variant(listaPrecios));

                // Último workbook abierto
                int count = Dispatch.get(workbooks, "Count").getInt();
                Dispatch nuevo = Dispatch.call(workbooks, "Item", count).toDispatch();

                // Guardar nuevo workbook de forma segura
                Path rutaExcelOrigen = Paths.get(getJarFolder(), "Resultados", listaPrecios + "-" + mixProductos + (clasificacion.isBlank() ? "" : " - " + clasificacion) + ".xlsx");
                try {
                    Files.deleteIfExists(rutaExcelOrigen);
                } catch (Exception ex) {
                    System.err.println("No se pudo borrar el archivo anterior: " + rutaExcelOrigen + " - " + ex.getMessage());
                }

                System.out.println(dtf.format(LocalDateTime.now()) + ": Guardando Excel en " + rutaExcelOrigen + " ...");
                Dispatch.call(nuevo, "SaveAs", rutaExcelOrigen.toString(), new Variant(51)); // 51 = xlsx
                Dispatch.call(nuevo, "Close", false); // cerrar nuevo workbook
                map.put("rutaExcelOrigen", rutaExcelOrigen.toString());
            }

            Dispatch.call(xl, "Run", "SetMostrarMensajes", new Variant(true));

            System.out.println(dtf.format(LocalDateTime.now()) + ": Cerrando Excel...");
            Dispatch.call(original, "Close", false); // cerrar original sin guardar
        } finally {
            // Restaurar alertas y eventos
            if (xl != null) {
                Dispatch.put(xl, "DisplayAlerts", true);
                Dispatch.put(xl, "EnableEvents", true);
                xl.invoke("Quit");
            }
            // Forzar limpieza de threads COM
            ComThread.Release();
        }
    }

    public void generarCatalogo(List<HashMap<String, Object>> parametros) throws Exception {
        for (HashMap<String, Object> fila : parametros) {
            String listaPrecios = (String) fila.get("listaPrecios");
            String mixProductos = (String) fila.get("mixProductos");
            String clasificacion = (String) fila.get("clasificacion");
            boolean caratula = (boolean) fila.get("caratula");
            String selectedTheme = (String) fila.get("selectedTheme");
            int productoQuantity = (int) fila.get("productoQuantity");
            float imageSize = (float) fila.get("imageSize");
            boolean presupuesto = (boolean) fila.get("presupuesto");
            String title = (String) fila.get("title");
            String subtitle = (String) fila.get("subtitle");
            String rutaExcelOrigen = (String) fila.get("rutaExcelOrigen");
            String carpetaDestino = (String) fila.get("carpetaDestino");
            final File archivoOrigenExcel = new File(rutaExcelOrigen);
            final File carpetaDestinoFile = new File(carpetaDestino);

            if (validarCarpetaDestino(carpetaDestinoFile)) {
                final File archivoDestinoPdf = new File(carpetaDestinoFile.getAbsolutePath() + File.separator + (clasificacion.isBlank() ? "" : clasificacion + " - ") + mixProductos + ".pdf");
                if (validarArchivoOrigenExcel(archivoOrigenExcel)) {
                    final PDFGenerationStats stats = PDFGenerator.generarPDF(archivoOrigenExcel, carpetaImagenes, caratula, archivoDestinoPdf,
                            imageSize,
                            pageType,
                            codigoCheckBox, productoCheckBox, precioCheckBox,
                            unidadPorBultoCheckBox, imagenCheckBox, null,
                            productoQuantity, title, subtitle,
                            selectedTheme, presupuesto,
                            PDFStyleDefaults.FONT_SIZE_CODIGO,
                            PDFStyleDefaults.FONT_SIZE_PRODUCTO,
                            PDFStyleDefaults.FONT_SIZE_PRECIO,
                            PDFStyleDefaults.FONT_SIZE_UXB,
                            PDFStyleDefaults.WHITE_COLOR,
                            PDFStyleDefaults.BLACK_COLOR,
                            PDFStyleDefaults.BLACK_COLOR,
                            PDFStyleDefaults.BLACK_COLOR);
                    System.out.println("------------------------------------------------------------------------------------------------------------------");
                    System.out.println(dtf.format(LocalDateTime.now()) + ": Generando catálogo: " + archivoDestinoPdf.getName() + "...");
                    System.out.println(dtf.format(LocalDateTime.now()) + ": " + stats.productosGenerados + " productos han sido generados.");
                    System.out.println(dtf.format(LocalDateTime.now()) + ": \"" + archivoDestinoPdf.getAbsolutePath() + "\" generado.");
                } else {
                    System.err.println(dtf.format(LocalDateTime.now()) + ": El archivo Excel de origen no existe o está vacío para: " + listaPrecios + " - " + mixProductos + (clasificacion.isBlank() ? "" : " - " + clasificacion));
                }
            } else {
                System.err.println(dtf.format(LocalDateTime.now()) + ": La ubicacion: " + carpetaDestinoFile.getAbsolutePath() + " no existe.");
            }
        }
        System.out.println("------------------------------------------------------------------------------------------------------------------");
        System.out.println(dtf.format(LocalDateTime.now()) + ": Proceso finalizado.");
    }

    private boolean validarArchivoOrigenExcel(File archivoOrigenExcel) throws InvalidFormatException, IOException {
        if (archivoOrigenExcel != null && archivoOrigenExcel.isFile()) {
            try (final OPCPackage pkg = OPCPackage.open(archivoOrigenExcel, PackageAccess.READ);
                 final XSSFWorkbook workbook = new XSSFWorkbook(pkg)) {
                final Sheet sheet = workbook.getSheetAt(0); // 1° hoja
                final int rows = ExcelUtils.countRowsInFile(sheet);
                if (rows < 2) {
                    return false;
                }
            } catch (Exception e) {
                throw e;
            }
        } else {
            return false;
        }
        return true;
    }

    private boolean validarCarpetaDestino(File carpetaDestino) {
        return carpetaDestino != null && carpetaDestino.isDirectory();
    }

    private boolean validarUbicaciones() throws Exception {
        if (carpetaImagenes != null && carpetaImagenes.isDirectory() && archivoMasterExcel != null && archivoMasterExcel.isFile()) {
            return true;
        } else {
            throw new Exception("Las ubicaciones del excel 'Parametros' en la hoja 'Ubicaciones' son incorrectas.");
        }
    }

}