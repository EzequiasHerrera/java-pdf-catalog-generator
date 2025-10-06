package cmd;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import service.PDFGenerator;
import themes.KitchenToolsTheme;
import themes.LineageTheme;
import utils.PDFUtils;

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

// Clase principal para ejecutar desde línea de comandos: java -Dfile.encoding=UTF-8 --enable-native-access=ALL-UNNAMED -jar catalog-generator-automated-2.1-shaded.jar
public class CommandLine {

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss");

    private File carpetaImagenes; // Directorio donde encontrar las imagenes
    private File archivoMasterExcel; // Archivo SUPER MASTER Excel
    private File archivoCaratulaPdf; // Archivo carátula PDF
    private File carpetaDestinoKT; // Directorio donde guardar NUEVO PDF
    private File carpetaDestinoLGE; // Directorio donde guardar NUEVO PDF

    private boolean codigoCheckBox; // CODIGO
    private boolean productoCheckBox; // NOMBRE
    private boolean precioCheckBox; // PRECIO
    private boolean unidadPorBultoCheckBox; // UXB
    private boolean imagenCheckBox; // IMAGEN

    private String pageWidthTextInput;
    private String pageHeightTextInput;

    public static void main(String[] args) {
        try {
            Configurator.setLevel("com.itextpdf", org.apache.logging.log4j.Level.ERROR); // Suprime logs de iText
            CommandLine app = new CommandLine();
            System.out.println(app.dtf.format(LocalDateTime.now()) + ": Catálogo PDF Generator iniciado.");
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
//        logTextArea = new StringBuilder();
        pageWidthTextInput = "595";
        pageHeightTextInput = "842";
        codigoCheckBox = true;
        productoCheckBox = true;
        precioCheckBox = true;
        unidadPorBultoCheckBox = true;
        imagenCheckBox = true;
    }

    public List<HashMap<String, Object>> obtenerParametros() throws Exception {
        final File excel = new File(this.getJarFolder() + File.separator + "Parametros.xlsx");
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy");
        final List<HashMap<String, Object>> parametros = new ArrayList<>();

        try (final OPCPackage pkg = OPCPackage.open(excel, PackageAccess.READ);
             final XSSFWorkbook workbook = new XSSFWorkbook(pkg)) {

            // UBICACIONES
            final Sheet ubicacionesSheet = workbook.getSheetAt(1); // 2° hoja

            Row row = ubicacionesSheet.getRow(1); // 2° fila
            if (PDFUtils.isEmptyRow(row)) {
                throw new Exception("La hoja 'Ubicaciones' está vacía.");
            }

            archivoMasterExcel = Paths.get(PDFUtils.getCellValue(row.getCell(0))).toFile(); // A
            carpetaImagenes = Paths.get(PDFUtils.getCellValue(row.getCell(1))).toFile(); // B
            carpetaDestinoKT = Paths.get(PDFUtils.getCellValue(row.getCell(2))).toFile(); // C
            carpetaDestinoLGE = Paths.get(PDFUtils.getCellValue(row.getCell(3))).toFile(); // D

            System.out.println("------------------------------------------------------------------------------------------------------------------");
            System.out.println(dtf.format(LocalDateTime.now()) + ": Ubicaciones cargadas:"
                    + "\n-Imágenes: " + carpetaImagenes.getAbsolutePath()
                    + "\n-Destino KT: " + carpetaDestinoKT.getAbsolutePath()
                    + "\n-Destino LGE: " + carpetaDestinoLGE.getAbsolutePath()
                    + "\n-Archivo Super Master: " + archivoMasterExcel.getAbsolutePath());

            if (validarUbicaciones()) {
                // PARAMETROS
                final Sheet parametrosSheet = workbook.getSheetAt(0); // 1° hoja
                final int parametrosRows = PDFUtils.countRowsInFile(parametrosSheet, new StringBuilder());

                for (int r = 1; r <= parametrosRows; r++) { // Empiezo en la fila 2
                    row = parametrosSheet.getRow(r);
                    if (PDFUtils.isEmptyRow(row)) {
                        continue;
                    }
                    // LEER CELDAS
                    String listaPrecios = PDFUtils.getCellValue(row.getCell(0)); //
                    String mixProductos = PDFUtils.getCellValue(row.getCell(1)); //
                    String clasificacion = PDFUtils.getCellValue(row.getCell(2)); //
                    String titleTextInput = PDFUtils.getCellValue(row.getCell(3)); //
                    String selectedTheme = PDFUtils.getCellValue(row.getCell(4)); //
                    boolean presupuestoCheckBox = PDFUtils.getCellValue(row.getCell(5)).equalsIgnoreCase("PRESUPUESTO"); //
                    int productoQuantityComboBox = (int) Double.parseDouble(PDFUtils.getCellValue(row.getCell(6))); //
                    String subtitleTextInput = formatter.format(LocalDate.now()); // Fecha actual

                    // SETEAR TAMAÑO DE IMAGEN DEPENDIENDO LA CANTIDAD DE PRODUCTOS
                    final String imageSizeTextInput = switch (productoQuantityComboBox) {
                        case 2 -> "380";
                        case 4 -> "190";
                        case 12 -> "90";
                        case 20 -> "60";
                        default -> throw new Exception("La cantidad de productos debe ser 2/4/12/20.");
                    };

                    System.out.println("------------------------------------------------------------------------------------------------------------------");
                    System.out.println(dtf.format(LocalDateTime.now()) + ": Parámetros cargados:"
                            + "\n-Lista de precios: " + listaPrecios
                            + "\n-Mix productos: " + mixProductos
                            + "\n-Clasificación: " + clasificacion
                            + "\n-Tema: " + selectedTheme
                            + "\n-Cantidad de productos: " + productoQuantityComboBox
                            + "\n-Presupuesto: " + (presupuestoCheckBox ? "Sí" : "No")
                            + "\n-Título: " + titleTextInput
                            + "\n-Subtítulo: " + subtitleTextInput);

                    final HashMap<String, Object> fila = new HashMap<>();
                    fila.put("listaPrecios", listaPrecios);
                    fila.put("mixProductos", mixProductos);
                    fila.put("clasificacion", clasificacion);
                    fila.put("selectedTheme", selectedTheme);
                    fila.put("productoQuantityComboBox", productoQuantityComboBox);
                    fila.put("imageSizeTextInput", imageSizeTextInput);
                    fila.put("presupuestoCheckBox", presupuestoCheckBox);
                    fila.put("titleTextInput", titleTextInput);
                    fila.put("subtitleTextInput", subtitleTextInput);

                    parametros.add(fila);
                }
            }
            return parametros;
        } catch (Exception e) {
            throw e;
        }
    }

    // Filtrar, ordenar y exportar usando las macros de Excel (JACOB)
    public void filtrarOrdenarExportar(List<HashMap<String, Object>> parametros) throws URISyntaxException {
        // DLL de JACOB
        final Path dllPath = Paths.get(getJarFolder(), "jacob-1.21-x64.dll");
        if (!Files.exists(dllPath)) {
            throw new RuntimeException("No se encontró el archivo DLL en: " + dllPath);
        }
        System.setProperty("jacob.dll.path", dllPath.toAbsolutePath().toString());

        ActiveXComponent xl = null;
        try {
            xl = new ActiveXComponent("Excel.Application");
            xl.setProperty("Visible", false);
            Dispatch.put(xl, "DisplayAlerts", false);
            Dispatch.put(xl, "EnableEvents", false);

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
                if (listaPrecios == null || mixProductos == null || clasificacion == null) continue;

                System.out.println(dtf.format(LocalDateTime.now()) + ": Procesando..." +
                        " (Lista de precios: " + listaPrecios + " - Mix productos: " + mixProductos + " - Clasificación: " + clasificacion + ")");

                // Filtrar y ordenar
                System.out.println(dtf.format(LocalDateTime.now()) + ": Filtrando y ordenando datos...");
                Dispatch.call(xl, "Run", "FiltrarYOrdenar", new Variant(mixProductos), new Variant(clasificacion));

                // Exportar
                System.out.println(dtf.format(LocalDateTime.now()) + ": Exportando datos filtrados...");
                Dispatch.call(xl, "Run", "ExportarFiltradosDesdeSUPERMASTER", new Variant(listaPrecios));

                // Último workbook abierto
                int count = Dispatch.get(workbooks, "Count").getInt();
                Dispatch nuevo = Dispatch.call(workbooks, "Item", count).toDispatch();

                // Guardar nuevo workbook de forma segura
                Path rutaExcelOrigen = Paths.get(getJarFolder(), "Resultados", listaPrecios + "-" + mixProductos + "-" + clasificacion + ".xlsx");
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
        if (validarInputs()) {
            for (HashMap<String, Object> fila : parametros) {
                String listaPrecios = (String) fila.get("listaPrecios");
                String mixProductos = (String) fila.get("mixProductos");
                String clasificacion = (String) fila.get("clasificacion");
                String selectedTheme = (String) fila.get("selectedTheme");
                int productoQuantityComboBox = (int) fila.get("productoQuantityComboBox");
                String imageSizeTextInput = (String) fila.get("imageSizeTextInput");
                boolean presupuestoCheckBox = (boolean) fila.get("presupuestoCheckBox");
                String titleTextInput = (String) fila.get("titleTextInput");
                String subtitleTextInput = (String) fila.get("subtitleTextInput");
                String rutaExcelOrigen = (String) fila.get("rutaExcelOrigen");
                final File archivoOrigenExcel = new File(rutaExcelOrigen);
                final File carpetaDestino;
                if (selectedTheme.equals(KitchenToolsTheme.THEME_NAME)) {
                    carpetaDestino = carpetaDestinoKT;
                } else if (selectedTheme.equals(LineageTheme.THEME_NAME)) {
                    carpetaDestino = carpetaDestinoLGE;
                } else {
                    throw new Exception("El tema seleccionado no es válido: " + selectedTheme);
                }
                final File archivoDestinoPdf = new File(carpetaDestino.getAbsolutePath() + File.separator + clasificacion + " - " + mixProductos + ".pdf");

                if (validarArchivoOrigenExcel(archivoOrigenExcel)) {
                    final int productos = PDFGenerator.generarPDF(archivoOrigenExcel, carpetaImagenes, archivoCaratulaPdf, archivoDestinoPdf,
                            Float.parseFloat(imageSizeTextInput),
                            Float.parseFloat(pageWidthTextInput),
                            Float.parseFloat(pageHeightTextInput),
                            codigoCheckBox, productoCheckBox, precioCheckBox,
                            unidadPorBultoCheckBox, imagenCheckBox, null,
                            productoQuantityComboBox, titleTextInput, subtitleTextInput,
                            selectedTheme, presupuestoCheckBox);

                    System.out.println("------------------------------------------------------------------------------------------------------------------");
                    System.out.println(dtf.format(LocalDateTime.now()) + ": Generando catálogo: " + archivoDestinoPdf.getName() + "...");
                    System.out.println(dtf.format(LocalDateTime.now()) + ": " + productos + " productos han sido generados.");
                    System.out.println(dtf.format(LocalDateTime.now()) + ": \"" + archivoDestinoPdf.getAbsolutePath() + "\" generado.");
                } else {
                    System.out.println(dtf.format(LocalDateTime.now()) + ": El archivo Excel de origen no existe o está vacío para: " + listaPrecios + "-" + mixProductos + "-" + clasificacion);
                }
            }
            System.out.println("------------------------------------------------------------------------------------------------------------------");
            System.out.println(dtf.format(LocalDateTime.now()) + ": Proceso finalizado.");
        }
    }

//    public void deletePdfs(Path folder) throws IOException {
//        System.out.println("Eliminando archivos PDF de: " + folder.toString());
//        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, "*.pdf")) {
//            for (Path entry : stream) {
//                Files.deleteIfExists(entry);
//                System.out.println("Eliminado: " + entry.getFileName());
//            }
//        }
//    }

    private boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            Float.parseFloat(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private boolean validarArchivoOrigenExcel(File archivoOrigenExcel) throws InvalidFormatException, IOException {
        if (archivoOrigenExcel != null && archivoOrigenExcel.isFile()) {
            try (final OPCPackage pkg = OPCPackage.open(archivoOrigenExcel, PackageAccess.READ);
                 final XSSFWorkbook workbook = new XSSFWorkbook(pkg)) {
                final Sheet sheet = workbook.getSheetAt(0); // 1° hoja
                final int rows = PDFUtils.countRowsInFile(sheet, new StringBuilder());
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

    private boolean validarInputs() throws Exception {
        if (isNumeric(pageWidthTextInput) && isNumeric(pageHeightTextInput)) {
            return true;
        } else {
            throw new Exception("Los inputs pageWidth o pageHeight contienen valores inválidos.");
        }
    }

    private boolean validarUbicaciones() throws Exception {
        if (carpetaImagenes != null && carpetaImagenes.isDirectory() &&
                carpetaDestinoKT != null && carpetaDestinoKT.isDirectory() &&
                carpetaDestinoLGE != null && carpetaDestinoLGE.isDirectory() &&
                archivoMasterExcel != null && archivoMasterExcel.isFile()) {
            return true;
        } else {
            throw new Exception("Las ubicaciones del excel 'Parametros' en la hoja 'Ubicaciones' son incorrectas.");
        }
    }

}