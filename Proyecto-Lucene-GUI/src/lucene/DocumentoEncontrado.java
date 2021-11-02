package lucene;

import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.EventHandler;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.stream.Stream;

public class DocumentoEncontrado {

    public Button btnPagina;
    public String tituloMostrar;
    public String tituloBuscar;
    public String encabezados;
    public String referencias;
    public String texto;
    public String resumen;
    public String archivo;
    public Integer posicion;
    public Float puntaje;
    public int posicionInicialDocumento;
    public int largoDocumento;

    DocumentoEncontrado() {
        tituloBuscar = tituloMostrar = encabezados = referencias = texto = resumen = archivo = null;
        posicion = null;
        puntaje = null;
        btnPagina = new Button("Mostrar");
        btnPagina.addEventHandler(
                MouseEvent.MOUSE_PRESSED,
                (e) -> abrirPagina()
        );
        posicionInicialDocumento = largoDocumento = 0;
    }


    public String limpiarAcentos(String cadena) {
        String limpio = null;
        if (cadena !=null) {
            // Normalizar texto para eliminar acentos, dieresis, cedillas y tildes
            limpio = Normalizer.normalize(cadena, Normalizer.Form.NFD);
            // Quitar caracteres no ASCII excepto la enie, interrogacion que abre, exclamacion que abre, grados, U con dieresis.
            limpio = limpio.replaceAll("[^A-Za-z\\u0303\\-+&|!(){}\\[\\]^\"~*?: ]", "");
            // Regresar a la forma compuesta, para poder comparar la enie con la tabla de valores
            limpio = Normalizer.normalize(limpio, Normalizer.Form.NFC);
        }
        return limpio;
    }

    public void abrirPagina() {
        StringBuilder paginaWeb = new StringBuilder();
        String restante;

        // Lee el documento HTML de la coleccion a la que pertenece
        try (BufferedReader buffer = new BufferedReader(new FileReader(archivo))) {
            String linea;
            for (int i = 0; i < posicionInicialDocumento; i++) {
                buffer.readLine();
            }
            for (int i = 0; i < largoDocumento; i++){
                linea = buffer.readLine();
                paginaWeb.append(linea);
            }

            // Crea el archivo del nuevo HTML
            String tituloMostrarLimpio;
            tituloMostrarLimpio = tituloMostrar.replace("/","");
            tituloMostrarLimpio = tituloMostrar.replace(":","");
            tituloMostrarLimpio = limpiarAcentos(tituloMostrarLimpio);
            Path ficheroHTML = Paths.get(".","/htmls/"+tituloMostrarLimpio+".html");
            FileWriter fileWriter = new FileWriter(ficheroHTML.toString());
            BufferedWriter escritor = new BufferedWriter(fileWriter);
            escritor.write(paginaWeb.toString());
            escritor.close();

            // Abre el html en el navegador
            String ubicacion = ficheroHTML.toAbsolutePath().toString();
            ubicacion = ubicacion.replace(".\\", "\\");
            File htmlFile = new File(ubicacion);

            try {
                Desktop.getDesktop().browse(htmlFile.toURI());
            }
            catch (IOException e){
                System.out.println("Hubo un problema al cargar la pagina web");
            }
        }
        catch (IOException  e) {
            System.out.println("Hubo un error al extraer el html de la coleccion");
        }
    }

    public void llenarDatos(int _posicion, float _puntaje, Document doc){

        setPosicion(_posicion);
        setPuntaje(_puntaje);
        IndexableField campo = doc.getField("tituloBuscar");
        if (campo != null) {
            setTituloBuscar(campo.stringValue());
        }
        campo = doc.getField("archivo");
        if (campo != null) {
            setArchivo(campo.stringValue());
        }
        campo = doc.getField("posicionInicial");
        if (campo != null) {
            setPosicionInicialDocumento(Integer.parseInt(campo.stringValue()));
        }
        campo = doc.getField("largoDocumento");
        if (campo != null) {
            setLargoDocumento(Integer.parseInt(campo.stringValue()));
        }
        campo = doc.getField("tituloMostrar");
        if (campo != null) {
            setTituloMostrar(campo.stringValue());
        }
        campo = doc.getField("ref");
        if (campo != null) {
            setReferencias(campo.stringValue());
        }
        campo = doc.getField("encab");
        if (campo != null) {
            setEncabezados(campo.stringValue());
        }
        campo = doc.getField("texto");
        if (campo != null) {
            setTexto(campo.stringValue());
        }
        campo = doc.getField("resumen");
        if (campo != null) {
            setResumen(campo.stringValue());
        }
    }
    public String getTituloMostrar() {
        return tituloMostrar;
    }

    public void setTituloMostrar(String tituloMostrar) {
        this.tituloMostrar = tituloMostrar;
    }

    public String getTituloBuscar() {
        return tituloBuscar;
    }

    public void setTituloBuscar(String tituloBuscar) {
        this.tituloBuscar = tituloBuscar;
    }

    public String getEncabezados() {
        return encabezados;
    }

    public void setEncabezados(String encabezados) {
        this.encabezados = encabezados;
    }

    public String getReferencias() {
        return referencias;
    }

    public void setReferencias(String referencias) {
        this.referencias = referencias;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public Button getBtnPagina() {
        return btnPagina;
    }

    public void setBtnPagina(Button btnPagina) {
        this.btnPagina = btnPagina;
    }

    public Integer getPosicion() {
        return posicion;
    }

    public void setPosicion(Integer posicion) {
        this.posicion = posicion;
    }

    public Float getPuntaje() {
        return puntaje;
    }

    public void setPuntaje(Float puntaje) {
        this.puntaje = puntaje;
    }

    public int getPosicionInicialDocumento() {
        return posicionInicialDocumento;
    }

    public void setPosicionInicialDocumento(int posicionInicialDocumento) {
        this.posicionInicialDocumento = posicionInicialDocumento;
    }

    public int getLargoDocumento() {
        return largoDocumento;
    }

    public void setLargoDocumento(int largoDocumento) {
        this.largoDocumento = largoDocumento;
    }

    public String getArchivo() {
        return archivo;
    }

    public void setArchivo(String archivo) {
        this.archivo = archivo;
    }

    public void setResumen(String resumen) {
        this.resumen = resumen;
    }

    public String getResumen() {
        return resumen;
    }

}
