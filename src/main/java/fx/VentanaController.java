package fx;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.log4j.BasicConfigurator;
import service.GeneratePDFService;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

//Implementar Initializable le permite ejecutar código cuando se abre la ventana
public class VentanaController implements Initializable {

    // Crea un InputTextBox
    @FXML
    private TextField ubicacionExcel;
    @FXML
    private TextField ubicacionImagenes;

    @FXML
    private TextField codigoFontSize; // CODIGO
    @FXML
    private TextField productoFontSize; // NOMBRE
    @FXML
    private TextField precioFontSize; // PRECIO
    @FXML
    private TextField unidadPorBultoFontSize; // UXB

    // Muestra un dropbox con un selector de colores
    @FXML
    private ColorPicker codigoColorPicker; // CODIGO
    @FXML
    private ColorPicker productoColorPicker; // NOMBRE
    @FXML
    private ColorPicker precioColorPicker; // PRECIO
    @FXML
    private ColorPicker unidadPorBultoColorPicker; // UXB

    @FXML
    private ComboBox<String> sheetSizeComboBox;

    // Crea un InputBox donde ingresar tamaño de imagenes (SE PODRÍA SIMPLIFICAR CON
    // A4 etc)
    @FXML
    private TextField imageSizeTextInput;
    @FXML
    private TextField pageWidthTextInput;
    @FXML
    private TextField pageHeightTextInput;
    // @FXML
    // private TextField productoQuantityTextInput;
    @FXML
    private ComboBox<Integer> productoQuantityComboBox;

    @FXML
    private TextField titleTextInput;
    @FXML
    private TextField subtitleTextInput;

    // Coloca un checkbox para cada uno de los items
    @FXML
    private CheckBox codigoCheckBox; // CODIGO
    @FXML
    private CheckBox productoCheckBox; // NOMBRE
    @FXML
    private CheckBox precioCheckBox; // PRECIO
    @FXML
    private CheckBox unidadPorBultoCheckBox; // UXB
    @FXML
    private CheckBox presupuestoCheckBox;

    // THEMES
    @FXML
    private Button lineageButton;
    @FXML
    private Button kitchenButton;

    private String selectedTheme = "lineage";

    @FXML
    private CheckBox imagenCheckBox;

    // Crea un area donde escribir texto
    @FXML
    private TextArea logTextArea;

    // Crea un botón
    @FXML
    private Button generarButton;

    // Barra que muestra el progreso
    @FXML
    private ProgressIndicator progressIndicator;

    // Archivo Excel
    private File archivoExcel;

    // presupuesto o cliente
    private boolean presupuestoActivo = false;

    // Directorio donde encontrar las imagenes
    private File carpetaImagenes;

    // Directorio donde encontrar carátula PDF
    private File archivoPdf;

    // Directorio donde guardar NUEVO PDF
    private File archivoDestino;

    // Audios para error o success
    private AudioClip errorSound;
    private AudioClip successSound;

    // -------------------------------------------------------------------------------------------------//

    // Se ejecuta automáticamente cuando se carga la ventana
    // url es FXMLLoader.load(getClass().getResource("/fxml/Ventana.fxml"))
    // rb sirve para traducir la UI si tenés archivos .properties con textos en
    // distintos idiomas
    public void initialize(URL url, ResourceBundle rb) {
        BasicConfigurator.configure(); // configure Log4j
        inicializarComponentes();
        Main.stage.setOnCloseRequest(event -> savePreferences());

        productoQuantityComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null)
                return;

            String imageSize = switch (newVal) {
                case 2 -> "380";
                case 4 -> "190";
                case 12 -> "90";
                case 20 -> "60";
                default -> "";
            };

            imageSizeTextInput.setText(imageSize);
        });

    }

    private void inicializarComponentes() {
        ubicacionExcel.setTooltip(new Tooltip("La 1º hoja debe contener los 7 encabezados en orden."));
        ubicacionImagenes.setTooltip(new Tooltip("Formatos de las imágenes: .jpg, .jpeg, .png y .bmp"));

        // Populate the ChoiceBox with font families
        sheetSizeComboBox.getItems().addAll("A4", "A3", "Carta");
        productoQuantityComboBox.getItems().addAll(2, 4, 12, 20);

        errorSound = new AudioClip(getClass().getResource("/audios/error.mp3").toExternalForm());
        successSound = new AudioClip(getClass().getResource("/audios/success.mp3").toExternalForm());

        errorSound.setVolume(0.1);
        successSound.setVolume(0.1);

        carpetaImagenes = new File("Z:\\Doc. Compartidos\\DUX ERP Linea GE\\IMAGENES (subidas a la Web)");

        if (carpetaImagenes != null && carpetaImagenes.isDirectory()) {
            ubicacionImagenes.setText(carpetaImagenes.getAbsolutePath());
        }

        loadPreferences(); // Load previous state from preferences
    }

    private void loadPreferences() {
        // CREO UN SISTEMA DE PREFERENCIAS CON NOMBRE catalogo
        final Preferences prefs = Preferences.userRoot().node("catalogo");

        codigoFontSize.setText(prefs.get("codigoFontSize", "6"));
        productoFontSize.setText(prefs.get("productoFontSize", "6"));
        precioFontSize.setText(prefs.get("precioFontSize", "7"));
        unidadPorBultoFontSize.setText(prefs.get("unidadPorBultoFontSize", "6"));
        productoQuantityComboBox.setValue(prefs.getInt("productoQuantityTextInput", 12));
        // productoQuantityTextInput.setText(prefs.get("productoQuantityTextInput",
        // "8"));
        presupuestoCheckBox.setSelected(prefs.getBoolean("presupuestoCheckBox", false));
        presupuestoActivo = presupuestoCheckBox.isSelected(); // Actualiza el valor

        titleTextInput.setText(prefs.get("titleTextInput", "TITULO"));
        subtitleTextInput.setText(prefs.get("subtitleTextInput", ""));

        // Cargo ubicacion de archivos de preferencias-----------------------

        // EXCEL
        String excelPath = prefs.get("ubicacionExcel", "");
        ubicacionExcel.setText(excelPath);

        archivoExcel = new File(excelPath);
        if (!archivoExcel.exists() || archivoExcel.isDirectory()) {
            archivoExcel = null;
        }

        // IMAGENES
        String imgPath = prefs.get("ubicacionImagenes", "");
        ubicacionImagenes.setText(imgPath);

        carpetaImagenes = new File(imgPath);
        if (!carpetaImagenes.exists() || !carpetaImagenes.isDirectory()) {
            carpetaImagenes = null;
        }
        // Termino de cargar archivos de preferencias------------------------

        // --------------------------------------------------------//
        final String[] codigoColor = prefs.get("codigoColorPicker", "0,0,0").split(",");
        codigoColorPicker.setValue(new Color(Double.parseDouble(codigoColor[0]), Double.parseDouble(codigoColor[1]),
                Double.parseDouble(codigoColor[2]), 1));

        final String[] productoColor = prefs.get("productoColorPicker", "0,0,0.54").split(",");
        productoColorPicker.setValue(new Color(Double.parseDouble(productoColor[0]),
                Double.parseDouble(productoColor[1]), Double.parseDouble(productoColor[2]), 1));

        final String[] precioColor = prefs.get("precioColorPicker", "0.54,0,0").split(",");
        precioColorPicker.setValue(new Color(Double.parseDouble(precioColor[0]), Double.parseDouble(precioColor[1]),
                Double.parseDouble(precioColor[2]), 1));

        final String[] unidadPorBultoColor = prefs.get("unidadPorBultoColorPicker", "0,0,0").split(",");
        unidadPorBultoColorPicker.setValue(new Color(Double.parseDouble(unidadPorBultoColor[0]),
                Double.parseDouble(unidadPorBultoColor[1]), Double.parseDouble(unidadPorBultoColor[2]), 1));

        // -----------------VALORES POR DEFECTO DE TAMAÑO Y
        // FUENTE-------------------------------//
        sheetSizeComboBox.setValue(prefs.get("sheetSize", "A4"));

        imageSizeTextInput.setText(prefs.get("imageSizeTextInput", "83"));
        pageWidthTextInput.setText(prefs.get("pageWidthTextInput", "595"));
        pageHeightTextInput.setText(prefs.get("pageHeightTextInput", "842"));

        // -------------------INHABILITAR OPCIONES AL
        // DESTILDAR-------------------------------------//
        codigoCheckBox.setSelected(prefs.getBoolean("codigoCheckBox", true));
        if (!codigoCheckBox.isSelected()) {
            codigoFontSize.setDisable(true);
            codigoColorPicker.setDisable(true);
        }

        productoCheckBox.setSelected(prefs.getBoolean("productoCheckBox", true));
        if (!productoCheckBox.isSelected()) {
            productoFontSize.setDisable(true);
            productoColorPicker.setDisable(true);
        }

        precioCheckBox.setSelected(prefs.getBoolean("precioCheckBox", true));
        if (!precioCheckBox.isSelected()) {
            precioFontSize.setDisable(true);
            precioColorPicker.setDisable(true);
        }

        unidadPorBultoCheckBox.setSelected(prefs.getBoolean("unidadPorBultoCheckBox", true));
        if (!unidadPorBultoCheckBox.isSelected()) {
            unidadPorBultoFontSize.setDisable(true);
            unidadPorBultoColorPicker.setDisable(true);
        }

        imagenCheckBox.setSelected(prefs.getBoolean("imagenCheckBox", true));
        if (!imagenCheckBox.isSelected()) {
            imageSizeTextInput.setDisable(true);
        }

        // -----------------------------------------------------------//
        codigoCheckBox.setTextFill(Paint.valueOf((codigoColorPicker.getValue().toString())));
        productoCheckBox.setTextFill(Paint.valueOf((productoColorPicker.getValue().toString())));
        precioCheckBox.setTextFill(Paint.valueOf((precioColorPicker.getValue().toString())));
        unidadPorBultoCheckBox.setTextFill(Paint.valueOf((unidadPorBultoColorPicker.getValue().toString())));
        // -----------------------------------------------------------//
        System.out.println("Excel: " + (archivoExcel != null ? archivoExcel.getAbsolutePath() : "null"));
        System.out.println("Imágenes: " + (carpetaImagenes != null ? carpetaImagenes.getAbsolutePath() : "null"));

    }

    private void savePreferences() {
        final Preferences prefs = Preferences.userRoot().node("catalogo");

        prefs.put("codigoFontSize", codigoFontSize.getText());
        prefs.put("productoFontSize", productoFontSize.getText());
        prefs.put("precioFontSize", precioFontSize.getText());
        prefs.put("unidadPorBultoFontSize", unidadPorBultoFontSize.getText());
        // prefs.put("productoQuantityTextInput", productoQuantityTextInput.getText());
        prefs.putInt("productoQuantityTextInput", productoQuantityComboBox.getValue());
        prefs.put("titleTextInput", titleTextInput.getText());
        prefs.put("subtitleTextInput", subtitleTextInput.getText());

        prefs.put("ubicacionExcel", ubicacionExcel.getText());
        prefs.put("ubicacionImagenes", ubicacionImagenes.getText());

        prefs.put("codigoColorPicker", codigoColorPicker.getValue().getRed() + ","
                + codigoColorPicker.getValue().getGreen() + "," + codigoColorPicker.getValue().getBlue());
        prefs.put("productoColorPicker", productoColorPicker.getValue().getRed() + ","
                + productoColorPicker.getValue().getGreen() + "," + productoColorPicker.getValue().getBlue());
        prefs.put("precioColorPicker", precioColorPicker.getValue().getRed() + ","
                + precioColorPicker.getValue().getGreen() + "," + precioColorPicker.getValue().getBlue());
        prefs.put("unidadPorBultoColorPicker",
                unidadPorBultoColorPicker.getValue().getRed() + "," + unidadPorBultoColorPicker.getValue().getGreen()
                        + "," + unidadPorBultoColorPicker.getValue().getBlue());

        prefs.put("sheetSize", sheetSizeComboBox.getValue());

        prefs.put("imageSizeTextInput", imageSizeTextInput.getText());
        prefs.put("pageWidthTextInput", pageWidthTextInput.getText());
        prefs.put("pageHeightTextInput", pageHeightTextInput.getText());

        prefs.putBoolean("codigoCheckBox", codigoCheckBox.isSelected());
        prefs.putBoolean("productoCheckBox", productoCheckBox.isSelected());
        prefs.putBoolean("precioCheckBox", precioCheckBox.isSelected());
        prefs.putBoolean("unidadPorBultoCheckBox", unidadPorBultoCheckBox.isSelected());

        prefs.putBoolean("imagenCheckBox", imagenCheckBox.isSelected());
    }

    @FXML
    public void onClickTheme(ActionEvent event) {
        Object source = event.getSource();

        if (source == lineageButton) {
            selectedTheme = "lineage";
            lineageButton.setStyle("-fx-background-color: #fff;");
            kitchenButton.setStyle("-fx-background-color: #444;");
        } else if (source == kitchenButton) {
            selectedTheme = "kitchen";
            kitchenButton.setStyle("-fx-background-color: #fff;");
            lineageButton.setStyle("-fx-background-color: #444;");
        }

        System.out.println("Tema seleccionado: " + selectedTheme);
    }

    @FXML
    public void buscarExcel(ActionEvent event) {
        logTextArea.clear();

        Preferences prefs = Preferences.userRoot().node("catalogo");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Elige archivo .XLSX");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivo XLSX", "*.xlsx"));

        // ——— Leo la última ruta guardada ——— //

        // Busco la ultima ruta guardada en el sistema
        final File lastPath = new File(prefs.get("ubicacionExcel", ""));

        if (!lastPath.exists() || !lastPath.isDirectory()) {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        } else {
            fileChooser.setInitialDirectory(lastPath);
        }

        archivoExcel = fileChooser.showOpenDialog(Main.stage);

        if (archivoExcel != null) {
            ubicacionExcel.setText(archivoExcel.getAbsolutePath());

            // ✅ Guardo la ruta seleccionada
            prefs.put("ubicacionExcel", archivoExcel.getAbsolutePath());
        } else {
            ubicacionExcel.clear();
        }
    }

    @FXML
    public void buscarImagenes(ActionEvent event) {
        logTextArea.clear();

        Preferences prefs = Preferences.userRoot().node("catalogo");

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Selecciona la carpeta donde están las imágenes");
        final File lastPath = new File(prefs.get("ubicacionImagenes", ""));

        if (!lastPath.exists() || !lastPath.isDirectory()) {
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        } else {
            directoryChooser.setInitialDirectory(lastPath);
        }

        carpetaImagenes = directoryChooser.showDialog(Main.stage);

        if (carpetaImagenes != null) {
            ubicacionImagenes.setText(carpetaImagenes.getAbsolutePath());
            // ✅ Guardo la ruta seleccionada
            prefs.put("ubicacionImagenes", carpetaImagenes.getAbsolutePath());
        } else {
            ubicacionImagenes.clear();
        }
    }

    @FXML
    public void generarCatalogo(ActionEvent event) {
        logTextArea.clear();
        if (archivoExcel != null && archivoExcel.isFile() && carpetaImagenes != null && carpetaImagenes.exists()) {
            if (validarTextInputs()) {
                if (elegirDestino()) {
                    // ACA LLAMA AL GENERADOR DEL PDF
                    GeneratePDFService service = new GeneratePDFService(
                            archivoExcel, carpetaImagenes, archivoPdf, archivoDestino,
                            Float.parseFloat(imageSizeTextInput.getText()),
                            Float.parseFloat(pageWidthTextInput.getText()),
                            Float.parseFloat(pageHeightTextInput.getText()),
                            codigoCheckBox.isSelected(), productoCheckBox.isSelected(), precioCheckBox.isSelected(),
                            unidadPorBultoCheckBox.isSelected(), imagenCheckBox.isSelected(), logTextArea,
                            productoQuantityComboBox.getValue(), titleTextInput.getText(), subtitleTextInput.getText(),
                            selectedTheme, presupuestoActivo);

                    service.setOnRunning(e -> {
                        generarButton.setDisable(true);
                        progressIndicator.setVisible(true);
                        logTextArea.setStyle("-fx-text-fill: darkblue;");
                        logTextArea
                                .appendText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss"))
                                        + ": Generando PDF...\n");
                    });
                    service.setOnSucceeded(e -> {
                        successSound.play();
                        logTextArea.setStyle("-fx-text-fill: darkgreen;");
                        logTextArea.appendText(" productos han sido generados.\n");
                        logTextArea
                                .appendText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss"))
                                        + ": \"" + archivoDestino.getAbsolutePath() + "\" generado.\n");
                        generarButton.setDisable(false);
                        progressIndicator.setVisible(false);
                    });
                    service.setOnFailed(e -> {
                        service.getException().printStackTrace();
                        errorSound.play();
                        logTextArea.setStyle("-fx-text-fill: firebrick;");
                        logTextArea
                                .appendText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss"))
                                        + ": Error: " + service.getException().getLocalizedMessage() + "\n");
                        generarButton.setDisable(false);
                        progressIndicator.setVisible(false);
                    });
                    service.start();
                }
            } else {
                logTextArea.setStyle("-fx-text-fill: firebrick;");
                logTextArea.appendText("Completa los parámetros correctamente.\n");
            }
        } else {
            logTextArea.setStyle("-fx-text-fill: firebrick;");
            logTextArea.appendText("Error: las ubicaciones son incorrectas.\n");
        }
    }

    @FXML
    public void onClickImagenes(Event event) {
        imageSizeTextInput.setDisable(!imagenCheckBox.isSelected());
    }

    // -----------------------------------------------------//
    @FXML
    public void onClickPresupuestoCheckBox(Event event) {
        presupuestoActivo = presupuestoCheckBox.isSelected();
        System.out.println(presupuestoActivo);
    }

    @FXML
    public void onClickCodigoColumn(Event event) {
        deshabilitarColumna(codigoCheckBox, codigoFontSize, codigoColorPicker);
    }

    @FXML
    public void onClickProductoColumn(Event event) {
        deshabilitarColumna(productoCheckBox, productoFontSize, productoColorPicker);
    }

    @FXML
    public void onClickPrecioColumn(Event event) {
        deshabilitarColumna(precioCheckBox, precioFontSize, precioColorPicker);
    }

    @FXML
    public void onClickUnidadPorBultoColumn(Event event) {
        deshabilitarColumna(unidadPorBultoCheckBox, unidadPorBultoFontSize, unidadPorBultoColorPicker);
    }

    // -----------------------------------------------------//

    @FXML
    public void onCodigoColorChange(Event event) {
        codigoCheckBox.setTextFill(Paint.valueOf((codigoColorPicker.getValue().toString())));
    }

    @FXML
    public void onProductoColorChange(Event event) {
        productoCheckBox.setTextFill(Paint.valueOf((productoColorPicker.getValue().toString())));
    }

    @FXML
    public void onPrecioColorChange(Event event) {
        precioCheckBox.setTextFill(Paint.valueOf((precioColorPicker.getValue().toString())));
    }

    @FXML
    public void onUnidadPorBultoColorChange(Event event) {
        unidadPorBultoCheckBox.setTextFill(Paint.valueOf((unidadPorBultoColorPicker.getValue().toString())));
    }

    // -----------------------------------------------------//
    @FXML
    public void onSheetSizeChange(Event event) {
        String selectedSheetSize = sheetSizeComboBox.getValue();

        switch (selectedSheetSize) {
            case "A4":
                pageWidthTextInput.setText("595");
                pageHeightTextInput.setText("842");
                break;
            case "A3":
                pageWidthTextInput.setText("842");
                pageHeightTextInput.setText("1190");
                break;
            case "Carta":
                pageWidthTextInput.setText("612");
                pageHeightTextInput.setText("792");
                break;
        }

    }
    // ---------------------VALIDACIONES ETC-------------------------------//

    private void deshabilitarColumna(CheckBox checkBox, TextField fontSize, ColorPicker colorPicker) {
        if (checkBox.isSelected()) {
            fontSize.setDisable(false);
            colorPicker.setDisable(false);
            if (isNumeric(imageSizeTextInput.getText()) && isNumeric(fontSize.getText())) {
                imageSizeTextInput.setText(
                        "" + (Float.parseFloat(imageSizeTextInput.getText()) - Float.parseFloat(fontSize.getText())));
            }
        } else {
            fontSize.setDisable(true);
            colorPicker.setDisable(true);
            if (isNumeric(imageSizeTextInput.getText()) && isNumeric(fontSize.getText())) {
                imageSizeTextInput.setText(
                        "" + (Float.parseFloat(imageSizeTextInput.getText()) + Float.parseFloat(fontSize.getText())));
            }
        }
    }

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

    private boolean validarTextInputs() {
        return isNumeric(codigoFontSize.getText())
                && isNumeric(productoFontSize.getText())
                && isNumeric(precioFontSize.getText())
                && isNumeric(unidadPorBultoFontSize.getText())
                // && isNumeric(productoQuantityTextInput.getText())

                && isNumeric(imageSizeTextInput.getText())
                && isNumeric(pageWidthTextInput.getText())
                && isNumeric(pageHeightTextInput.getText());
    }

    private boolean elegirDestino() {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yy");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccione destino y nombre a guardar");
        final File defaultPath = new File("Z:\\Doc. Compartidos\\CATALOGOS\\CATALOGOS VENDEDORES");
        if (defaultPath.exists() && defaultPath.isDirectory()) {
            fileChooser.setInitialDirectory(defaultPath);
        } else {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        }
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo PDF", "*.pdf"));
        fileChooser.setInitialFileName(
                (archivoPdf != null ? archivoPdf.getName().replaceFirst("[.][^.]+$", "").toUpperCase() : "") + " - "
                        + formatter.format(LocalDate.now()));
        archivoDestino = fileChooser.showSaveDialog(Main.stage);
        if (archivoDestino != null) {
            return true;
        } else {
            return false;
        }
    }

}