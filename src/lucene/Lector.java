package lucene;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Lector {

    public ArrayList<ArrayList<String>> htmlsDeCadaArchivo;

    public ArrayList<ArrayList<String>> obtenerDocumentos() throws IOException {
        htmlsDeCadaArchivo = new ArrayList<ArrayList<String>>();
        ArrayList<String> documentosHtml = new ArrayList<String>();
        String separador = File.separator;
        Path ruta = Paths.get(".", "/input/");
        File carpeta = new File(ruta.toString());

        if (carpeta.exists()) {
            File[] files = carpeta.listFiles();
            for (File file : files) {
                if (file.isFile()){
                    htmlsDeCadaArchivo.add(extraerHTML(file));
                }
            }
        }
        return htmlsDeCadaArchivo;
    }

    public ArrayList<String> extraerHTML (File archivo) throws IOException {
        ArrayList<String> documentosHtml = new ArrayList<String>();
        StringBuilder nuevoDocumento = new StringBuilder();
        int contador = 0;
        boolean documentoIniciado = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.equals("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">")) {
                    documentoIniciado = true;
                    nuevoDocumento.setLength(0);    // Lectura nueva
                    contador++;
                } else if (linea.equals("</html>")) {
                    documentoIniciado = false;
                    documentosHtml.add(nuevoDocumento.toString());
                } else if (documentoIniciado) {
                    nuevoDocumento.append(linea);
                }
            }
        }
        return documentosHtml;
    }

}
