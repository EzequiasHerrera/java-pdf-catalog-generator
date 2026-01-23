package fx;

import enums.PageType;
import enums.ProductQuantity;
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
import pdf.PDFStyleDefaults;
import pdf.themes.KitchenToolsTheme;
import pdf.themes.LineageTheme;
import utils.ValidationUtils;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

//Implementar Initializable le permite ejecutar código cuando se abre la ventana
public class VentanaController implements Initializable {

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

    @FXML
    private TextField pageWidthTextInput;
    @FXML
    private TextField pageHeightTextInput;
    @FXML
    private TextField imageSizeTextInput;

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
    private ComboBox<String> pageTypeComboBox;
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
    @FXML
    private CheckBox imagenCheckBox;
    @FXML
    private CheckBox caratulaCheckBox;

    // THEMES
    @FXML
    private Button lineageButton;
    @FXML
    private Button kitchenButton;

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
    // Directorio donde encontrar las imagenes
    private File carpetaImagenes;
    // Directorio donde guardar NUEVO PDF
    private File archivoDestino;

    // presupuesto o cliente
    private String selectedTheme;

    // Audios para error o success
    private AudioClip errorSound;
    private AudioClip successSound;

    // Preferencias del usuario (instancia única)
    private final Preferences prefs = Preferences.userRoot().node("catalogo");
    // -------------------------------------------------------------------------------------------------//

    // Se ejecuta automáticamente cuando se carga la ventana
    // url es FXMLLoader.load(getClass().getResource("/fxml/Ventana.fxml"))
    // rb sirve para traducir la UI si tenés archivos .properties con textos en
    // distintos idiomas
    public void initialize(URL url, ResourceBundle rb) {
        BasicConfigurator.configure(); // configure Log4j
        inicializarComponentes();
        Main.stage.setOnCloseRequest(event -> savePreferences());
    }

    private void inicializarComponentes() {

        ubicacionImagenes.setTooltip(new Tooltip("Formatos de las imágenes: .jpg, .jpeg, .png y .bmp"));

        pageTypeComboBox.getItems().addAll(Arrays.stream(PageType.values()).map(pageType -> pageType.name()).toArray(value -> new String[value]));
        productoQuantityComboBox.getItems().addAll(Arrays.stream(ProductQuantity.values()).map(productQuantity -> productQuantity.getQuantity()).toArray(value -> new Integer[value]));

        errorSound = new AudioClip(getClass().getResource("/audios/error.mp3").toExternalForm());
        successSound = new AudioClip(getClass().getResource("/audios/success.mp3").toExternalForm());

        errorSound.setVolume(0.1);
        successSound.setVolume(0.1);

        // Listeners para actualizar valores automáticamente
        pageTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            PageType pageType = PageType.valueOf(pageTypeComboBox.getValue());
            pageWidthTextInput.setText(String.valueOf(pageType.getWidth()));
            pageHeightTextInput.setText(String.valueOf(pageType.getHeight()));
        });

        productoQuantityComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null)
                return;
            try {
                final String imageSize = String.valueOf(ProductQuantity.fromQuantity(newVal).getImageSize());
                imageSizeTextInput.setText(imageSize);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        loadPreferences(); // Load previous state from preferences
    }

    private void loadPreferences() {
        codigoFontSize.setText(prefs.get("codigoFontSize", String.valueOf(PDFStyleDefaults.FONT_SIZE_CODIGO)));
        productoFontSize.setText(prefs.get("productoFontSize", String.valueOf(PDFStyleDefaults.FONT_SIZE_PRODUCTO)));
        precioFontSize.setText(prefs.get("precioFontSize", String.valueOf(PDFStyleDefaults.FONT_SIZE_PRECIO)));
        unidadPorBultoFontSize.setText(prefs.get("unidadPorBultoFontSize", String.valueOf(PDFStyleDefaults.FONT_SIZE_UXB)));

        productoQuantityComboBox.setValue(prefs.getInt("productoQuantityComboBox", ProductQuantity.TWELVE.getQuantity()));
        presupuestoCheckBox.setSelected(prefs.getBoolean("presupuestoCheckBox", false));

        titleTextInput.setText(prefs.get("titleTextInput", ""));
        subtitleTextInput.setText(prefs.get("subtitleTextInput", ""));

        // Cargo ubicacion de archivos de preferencias
        String excelPath = prefs.get("ubicacionExcel", "");
        archivoExcel = new File(excelPath);
        if (archivoExcel.isFile()) {
            ubicacionExcel.setText(archivoExcel.getAbsolutePath());
        } else {
            archivoExcel = null;
        }

        String imgPath = prefs.get("ubicacionImagenes", "");
        carpetaImagenes = new File(imgPath);
        if (carpetaImagenes.isDirectory()) {
            ubicacionImagenes.setText(carpetaImagenes.getAbsolutePath());
        } else {
            carpetaImagenes = null;
        }

        // Cargo colores de preferencias
        loadColorPreference("codigoColorPicker", "0,0,0", codigoColorPicker);
        loadColorPreference("productoColorPicker", "0,0,0.54", productoColorPicker);
        loadColorPreference("precioColorPicker", "0.54,0,0", precioColorPicker);
        loadColorPreference("unidadPorBultoColorPicker", "0,0,0", unidadPorBultoColorPicker);

        pageTypeComboBox.setValue(prefs.get("pageTypeComboBox", "A4"));

        selectedTheme = prefs.get("selectedTheme", LineageTheme.THEME_NAME);
        changeButtonStyles(selectedTheme);

        // Inhabilitar opciones según checkboxes
        presupuestoCheckBox.setSelected(prefs.getBoolean("presupuestoCheckBox", false));

        caratulaCheckBox.setSelected(prefs.getBoolean("caratulaCheckBox", true));
        if (!caratulaCheckBox.isSelected()) {
            titleTextInput.setDisable(true);
            subtitleTextInput.setDisable(true);
            presupuestoCheckBox.setDisable(true);
        }

        loadCheckBoxState("codigoCheckBox", codigoCheckBox, codigoFontSize, codigoColorPicker);
        loadCheckBoxState("productoCheckBox", productoCheckBox, productoFontSize, productoColorPicker);
        loadCheckBoxState("precioCheckBox", precioCheckBox, precioFontSize, precioColorPicker);
        loadCheckBoxState("unidadPorBultoCheckBox", unidadPorBultoCheckBox, unidadPorBultoFontSize, unidadPorBultoColorPicker);

        imagenCheckBox.setSelected(prefs.getBoolean("imagenCheckBox", true));
        if (!imagenCheckBox.isSelected()) {
            imageSizeTextInput.setDisable(true);
        }

        // Aplicar colores a los checkboxes
        codigoCheckBox.setTextFill(Paint.valueOf(codigoColorPicker.getValue().toString()));
        productoCheckBox.setTextFill(Paint.valueOf(productoColorPicker.getValue().toString()));
        precioCheckBox.setTextFill(Paint.valueOf(precioColorPicker.getValue().toString()));
        unidadPorBultoCheckBox.setTextFill(Paint.valueOf(unidadPorBultoColorPicker.getValue().toString()));

        System.out.println("Excel: " + (archivoExcel != null ? archivoExcel.getAbsolutePath() : "null"));
        System.out.println("Imágenes: " + (carpetaImagenes != null ? carpetaImagenes.getAbsolutePath() : "null"));
    }

    private void loadColorPreference(String key, String defaultValue, ColorPicker colorPicker) {
        final String[] rgb = prefs.get(key, defaultValue).split(",");
        colorPicker.setValue(new Color(
                Double.parseDouble(rgb[0]),
                Double.parseDouble(rgb[1]),
                Double.parseDouble(rgb[2]), 1));
    }

    private void loadCheckBoxState(String key, CheckBox checkBox, TextField fontSize, ColorPicker colorPicker) {
        checkBox.setSelected(prefs.getBoolean(key, true));
        if (!checkBox.isSelected()) {
            fontSize.setDisable(true);
            colorPicker.setDisable(true);
        }
    }

    private void savePreferences() {
        prefs.put("codigoFontSize", codigoFontSize.getText());
        prefs.put("productoFontSize", productoFontSize.getText());
        prefs.put("precioFontSize", precioFontSize.getText());
        prefs.put("unidadPorBultoFontSize", unidadPorBultoFontSize.getText());
        prefs.putInt("productoQuantityComboBox", productoQuantityComboBox.getValue());
        prefs.put("titleTextInput", titleTextInput.getText());
        prefs.put("subtitleTextInput", subtitleTextInput.getText());

        prefs.put("ubicacionExcel", ubicacionExcel.getText());
        prefs.put("ubicacionImagenes", ubicacionImagenes.getText());

        saveColorPreference("codigoColorPicker", codigoColorPicker);
        saveColorPreference("productoColorPicker", productoColorPicker);
        saveColorPreference("precioColorPicker", precioColorPicker);
        saveColorPreference("unidadPorBultoColorPicker", unidadPorBultoColorPicker);

        prefs.put("pageTypeComboBox", pageTypeComboBox.getValue());
        prefs.put("selectedTheme", selectedTheme);

        prefs.putBoolean("presupuestoCheckBox", presupuestoCheckBox.isSelected());
        prefs.putBoolean("caratulaCheckBox", caratulaCheckBox.isSelected());
        prefs.putBoolean("codigoCheckBox", codigoCheckBox.isSelected());
        prefs.putBoolean("productoCheckBox", productoCheckBox.isSelected());
        prefs.putBoolean("precioCheckBox", precioCheckBox.isSelected());
        prefs.putBoolean("unidadPorBultoCheckBox", unidadPorBultoCheckBox.isSelected());
        prefs.putBoolean("imagenCheckBox", imagenCheckBox.isSelected());
    }

    private void saveColorPreference(String key, ColorPicker colorPicker) {
        Color color = colorPicker.getValue();
        prefs.put(key, color.getRed() + "," + color.getGreen() + "," + color.getBlue());
    }

    @FXML
    public void onClickTheme(ActionEvent event) {
        Object source = event.getSource();
        selectedTheme = (source == lineageButton) ? LineageTheme.THEME_NAME : KitchenToolsTheme.THEME_NAME;
        changeButtonStyles(selectedTheme);
    }

    private void changeButtonStyles(String selectedTheme) {
        if (selectedTheme.equals(LineageTheme.THEME_NAME)) {
            lineageButton.setStyle("-fx-background-color: #fff;-fx-border-color: #1E88E5; -fx-border-width: 3;");
            kitchenButton.setStyle("-fx-background-color: #444;-fx-border-color: transparent; -fx-border-width: 0;");
        } else {
            kitchenButton.setStyle("-fx-background-color: #fff;-fx-border-color: #1E88E5; -fx-border-width: 3;");
            lineageButton.setStyle("-fx-background-color: #444;-fx-border-color: transparent; -fx-border-width: 0;");
        }
    }

    @FXML
    public void buscarExcel(ActionEvent event) {
        logTextArea.clear();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Elige archivo .xlsx");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivo XLSX", "*.xlsx"));

        // Busco la ultima ruta guardada en el sistema
        final File lastPath = new File(prefs.get("ubicacionExcel", ""));

        if (!lastPath.isDirectory()) {
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

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Selecciona la carpeta donde están las imágenes");
        final File lastPath = new File(prefs.get("ubicacionImagenes", ""));

        if (!lastPath.isDirectory()) {
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
        logTextArea.setStyle("-fx-text-fill: firebrick;");

        // Validar ubicaciones
        if (archivoExcel == null || !archivoExcel.isFile()) {
            logTextArea.appendText("Error: No se seleccionó un archivo Excel válido.\n");
            return;
        }
        if (carpetaImagenes == null || !carpetaImagenes.isDirectory()) {
            logTextArea.appendText("Error: No se seleccionó una carpeta de imágenes válida.\n");
            return;
        }

        // Validar que haya al menos 1 columna activa
        if (!codigoCheckBox.isSelected() && !productoCheckBox.isSelected()
                && !precioCheckBox.isSelected() && !unidadPorBultoCheckBox.isSelected()) {
            logTextArea.appendText("Error: Debe seleccionar al menos una columna (Código, Producto, Precio o UxB).\n");
            return;
        }

        // Validar inputs numéricos
        String errorValidacion = validarTextInputs();
        if (errorValidacion != null) {
            logTextArea.appendText("Error: " + errorValidacion + "\n");
            return;
        }

        if (!elegirDestino()) {
            return;
        }

        // ACA LLAMA AL GENERADOR DEL PDF
        GeneratePDFService service = new GeneratePDFService(
                archivoExcel, carpetaImagenes, caratulaCheckBox.isSelected(), archivoDestino,
                Float.parseFloat(imageSizeTextInput.getText()),
                PageType.valueOf(pageTypeComboBox.getValue()),
                codigoCheckBox.isSelected(), productoCheckBox.isSelected(), precioCheckBox.isSelected(),
                unidadPorBultoCheckBox.isSelected(), imagenCheckBox.isSelected(), logTextArea,
                productoQuantityComboBox.getValue(), titleTextInput.getText(), subtitleTextInput.getText(),
                selectedTheme, presupuestoCheckBox.isSelected());

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
            logTextArea.appendText(service.getValue() + " productos han sido generados.\n");
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

    @FXML
    public void onClickImagenes(Event event) {
        imageSizeTextInput.setDisable(!imagenCheckBox.isSelected());
    }

    // -----------------------------------------------------//
    @FXML
    public void onClickCaratulaCheckBox(Event event) {
        if (caratulaCheckBox.isSelected()) {
            presupuestoCheckBox.setDisable(false);
            titleTextInput.setDisable(false);
            subtitleTextInput.setDisable(false);
        } else {
            presupuestoCheckBox.setDisable(true);
            titleTextInput.setDisable(true);
            subtitleTextInput.setDisable(true);
        }
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

    // ---------------------VALIDACIONES ETC-------------------------------//

    private void deshabilitarColumna(CheckBox checkBox, TextField fontSize, ColorPicker colorPicker) {
        if (checkBox.isSelected()) {
            fontSize.setDisable(false);
            colorPicker.setDisable(false);
            if (ValidationUtils.isNumeric(imageSizeTextInput.getText()) && ValidationUtils.isNumeric(fontSize.getText())) {
                imageSizeTextInput.setText(
                        "" + (Float.parseFloat(imageSizeTextInput.getText()) - Float.parseFloat(fontSize.getText())));
            }
        } else {
            fontSize.setDisable(true);
            colorPicker.setDisable(true);
            if (ValidationUtils.isNumeric(imageSizeTextInput.getText()) && ValidationUtils.isNumeric(fontSize.getText())) {
                imageSizeTextInput.setText(
                        "" + (Float.parseFloat(imageSizeTextInput.getText()) + Float.parseFloat(fontSize.getText())));
            }
        }
    }

    /**
     * Valida los campos de texto numéricos.
     * @return null si todo es válido, o un mensaje de error específico
     */
    private String validarTextInputs() {
        if (!ValidationUtils.isNumeric(codigoFontSize.getText())) {
            return "El tamaño de fuente del código no es válido.";
        }
        if (!ValidationUtils.isNumeric(productoFontSize.getText())) {
            return "El tamaño de fuente del producto no es válido.";
        }
        if (!ValidationUtils.isNumeric(precioFontSize.getText())) {
            return "El tamaño de fuente del precio no es válido.";
        }
        if (!ValidationUtils.isNumeric(unidadPorBultoFontSize.getText())) {
            return "El tamaño de fuente de UxB no es válido.";
        }
        if (!ValidationUtils.isNumeric(imageSizeTextInput.getText())) {
            return "El tamaño de imagen no es válido.";
        }
        if (!ValidationUtils.isNumeric(pageWidthTextInput.getText())) {
            return "El ancho de página no es válido.";
        }
        if (!ValidationUtils.isNumeric(pageHeightTextInput.getText())) {
            return "El alto de página no es válido.";
        }
        return null;
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
        fileChooser.setInitialFileName(" - " + formatter.format(LocalDate.now()));
        archivoDestino = fileChooser.showSaveDialog(Main.stage);
        if (archivoDestino == null) {
            return false;
        }
        return true;
    }

}