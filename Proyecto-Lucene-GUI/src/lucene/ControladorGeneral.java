package lucene;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.cell.PropertyValueFactory;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ControladorGeneral implements Controlador {


    // Variables JAVAFX
    @FXML public TableView<DocumentoEncontrado> tblEscalafon;
    @FXML public TableColumn<String,DocumentoEncontrado> clmPosicion;
    @FXML public TableColumn<String,DocumentoEncontrado> clmTitulo;
    @FXML public TableColumn<String,DocumentoEncontrado> clmPagina;
    @FXML public TableColumn<String,DocumentoEncontrado> clmPuntaje;
    @FXML private TextField tfdDirectorioIndexacion, tfdArchivoIndexar, tfdDirectorioIndice,
                      tfdConsulta;
    @FXML private RadioButton rdbActualizar, rdbConsultaPersonalizada;
    @FXML private Label lblEstadoIndexacion, lblDocumentosEncontrados, lblDocumentosColeccion, lblTiempoConsulta, lblResumenVistaPrevia;
    @FXML private ComboBox<String> cbxCampos;

    // Variables LUCENE
    Lector lector;
    Indexador indexador;
    Buscador buscador;

    // Otras variables
    Alert alerta = new Alert(Alert.AlertType.WARNING,"");
    ArrayList<Html_Indexado> archivos;
    ArrayList<ArrayList<DocumentoEncontrado>> resultados;
    int paginaActual = 0;

    @Override
    public void prepararVentana() {
        llenarComboBox();
        configurarTabla();
        Analizador analizadores = new Analizador();
        try {
            analizadores.leerStopWords("stop_words");
        }
        catch (IOException e){
            alerta.setTitle("ERROR");
            alerta.setHeaderText("Hubo un problema al cargar el archivo de stop words");
            alerta.setContentText("Revise que el nombre de archivo especificado es correcto.");
            alerta.show();
        }
        lector = new Lector();
        indexador = new Indexador(analizadores);
        buscador = new Buscador(analizadores, lblDocumentosEncontrados, lblTiempoConsulta, lblDocumentosColeccion);
    }

    public void llenarComboBox(){
        if (cbxCampos.getItems().size() == 0) {
            cbxCampos.getItems().addAll("", "titulo", "texto", "encab", "ref");
            cbxCampos.getSelectionModel().selectFirst();
        }
    }

    public void configurarTabla(){
        clmPosicion.setCellValueFactory(new PropertyValueFactory<>("posicion"));
        clmTitulo.setCellValueFactory(new PropertyValueFactory<>("tituloMostrar"));
        clmPagina.setCellValueFactory(new PropertyValueFactory<>("btnPagina"));
        clmPuntaje.setCellValueFactory(new PropertyValueFactory<>("puntaje"));
        tblEscalafon.setPlaceholder(new Label("No hay resultados"));
        tblEscalafon.getSelectionModel().selectedItemProperty().addListener(
            (observableValue, docAnterior, docSiguiente) -> {
                if (observableValue != null) {
                    if (docSiguiente != null && !docSiguiente.equals(docAnterior))
                        lblResumenVistaPrevia.setText(docSiguiente.getResumen());
                }
            }
        );
    }

    public void limpiarTabla(){
        tblEscalafon.getItems().clear();
    }

    public boolean validarCamposIndexacion(){
        return !tfdArchivoIndexar.getText().equals("")
                && !tfdDirectorioIndexacion.getText().equals("");
    }

    public boolean validarCamposBusqueda(){
        return !tfdDirectorioIndice.getText().equals("") &&
                !tfdConsulta.getText().equals("");
    }

    public void indexar(ActionEvent actionEvent) {
        if (validarCamposIndexacion()){
            try {
                archivos = lector.obtenerDocumentos(tfdArchivoIndexar.getText());
            }
            catch (IOException e){
                alerta.setTitle("ERROR");
                alerta.setHeaderText("Hubo un error al leer la colección de documentos");
                alerta.show();
            }
            try {
                lblEstadoIndexacion.setText("Indexando ...");
                long inicio = System.currentTimeMillis();
                indexador.configurarIndexador(tfdDirectorioIndexacion.getText(),rdbActualizar.isSelected());
                int contador = 0;
                boolean actualizar = rdbActualizar.isSelected();
                for (Html_Indexado html : archivos) {
                    indexador.indexarContenidos(html,actualizar,contador);
                    contador++;
                }
                // Se cierra la escritura
                indexador.writer.close();
                long fin = System.currentTimeMillis();
                double tiempo = (double) ((fin - inicio)/1000);

                lblEstadoIndexacion.setText(contador + " documentos indexados de la coleccion "
                                            + tfdArchivoIndexar.getText() + " en " + tiempo
                                            + " segundos");

            }
            catch (IOException e){
                alerta.setTitle("ERROR");
                alerta.setHeaderText("Hubo un error durante la indexación");
                alerta.show();
            }
        }
        else {
            alerta.setTitle("ALERTA");
            alerta.setHeaderText("Alguno de los campos esta vacio");
            alerta.show();
        }
    }

    public void buscar(ActionEvent actionEvent){
        limpiarTabla();
        lblResumenVistaPrevia.setText("");
        if (validarCamposBusqueda()){
            resultados = buscador.buscarDocumento(tfdDirectorioIndice.getText(),
                    cbxCampos.getSelectionModel().getSelectedItem(), tfdConsulta.getText(),
                    20,rdbConsultaPersonalizada.isSelected());
            paginaActual = 0;
            if (resultados.size() > 0){
                for (DocumentoEncontrado doc : resultados.get(paginaActual)){
                    tblEscalafon.getItems().add(doc);
                }
            }
        }
        else {
            alerta.setTitle("ALERTA");
            alerta.setHeaderText("Por favor, asegúrese de llenar todos los campos");
            alerta.show();
        }
    }

    public void verSiguiente(){
        if (!tblEscalafon.getItems().isEmpty() && paginaActual+1 < resultados.size()) {
            limpiarTabla();
            for (DocumentoEncontrado doc : resultados.get(++paginaActual)){
                tblEscalafon.getItems().add(doc);
            }
        }
        else {
            alerta.setTitle("Alerta");
            alerta.setContentText("No hay mas resultados");
            alerta.show();
        }

    }

    public void verAnterior(){
        if (paginaActual-1 >= 0) {
            limpiarTabla();
            for (DocumentoEncontrado doc : resultados.get(--paginaActual)){
                tblEscalafon.getItems().add(doc);
            }
        }
        else {
            alerta.setTitle("Alerta");
            alerta.setContentText("Esta en la primera página");
            alerta.show();
        }
    }

    public void testHtml(ActionEvent actionEvent){
        File htmlFile = new File("test");
        try {
            Desktop.getDesktop().browse(htmlFile.toURI());
        }
        catch (IOException e){
            System.out.println("Hubo un problema al cargar la pagina web");
        }
    }

}
