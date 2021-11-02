package lucene;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui.fxml"));
        StackPane root = loader.load();
        Controlador controlador = loader.getController();
        primaryStage.setTitle("Progra II RIT");
        primaryStage.setScene(new Scene(root));
        controlador.prepararVentana();
        primaryStage.show();

        // Para borrar los html al cerrar la aplicacion
        primaryStage.setOnCloseRequest( (e) -> {
              Path dirHTMLs = Paths.get(".","htmls");
              File carpetaHTMLs = new File(String.valueOf(dirHTMLs));
              File[] contents = carpetaHTMLs.listFiles();
              if (contents != null) {
                  for (File f : contents) {
                      f.delete();
                  }
              }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
