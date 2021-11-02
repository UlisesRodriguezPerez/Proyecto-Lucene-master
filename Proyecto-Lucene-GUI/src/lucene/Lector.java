package lucene;

import javafx.scene.control.Alert;

import javax.swing.*;
import javax.swing.text.html.HTML;
import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Lector {

    public ArrayList<Html_Indexado> htmlsDeCadaArchivo;

    public ArrayList<Html_Indexado> obtenerDocumentos(String nombreDocumento) throws IOException {
        htmlsDeCadaArchivo = new ArrayList<Html_Indexado>();
        String separador = File.separator;
        Path ruta = Paths.get(".", "/input/"+nombreDocumento);
        File archivo = new File(ruta.toString());
        if (archivo.exists())
            htmlsDeCadaArchivo = extraerHTML(archivo);
        else {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("ERROR");
            alerta.setHeaderText("No se encontro el documento");
            alerta.setContentText("El archivo indicado no existe en la el directorio /input");
            alerta.show();
        }
        return htmlsDeCadaArchivo;
    }

    public ArrayList<Html_Indexado> extraerHTML (File archivo) throws IOException {
        ArrayList<Html_Indexado> documentosHtml = new ArrayList<Html_Indexado>();
        StringBuilder nuevoDocumento = new StringBuilder();
        int contador = 0;
        int posicionInicial = 0;
        int largo = 0;
        boolean documentoIniciado = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                contador++;
                if (linea.equals("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">")) {
                    documentoIniciado = true;
                    nuevoDocumento.setLength(0); // Lectura nueva
                    posicionInicial = contador;
                } else if (linea.equals("</html>")) {
                    documentoIniciado = false;
                    largo = contador - posicionInicial;
                    Html_Indexado html_indexado = new Html_Indexado(
                                                        archivo.toString(),
                                                        nuevoDocumento.toString(),
                                                        posicionInicial,
                                                        largo);
                    documentosHtml.add(html_indexado);
                } else if (documentoIniciado) {
                    nuevoDocumento.append(linea);
                }
            }
        }
        return documentosHtml;
    }

}
