package fx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    public static Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;

        //Cuando se ejecute load, cargará la ventana que se encuentre en esa carpeta
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Ventana.fxml"));

        //Definimos el titulo de la ventana
        primaryStage.setTitle("Catalogo");

        //Definimos ICONO de logo de aplicación y lo seteamos
        final Image icon = new Image(getClass().getResource("/images/logoapp.png").toExternalForm());
        primaryStage.getIcons().add(icon);

        //Creamos escena principal Parent root
        primaryStage.setScene(new Scene(root));

        //Mostramos la escena principal
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
