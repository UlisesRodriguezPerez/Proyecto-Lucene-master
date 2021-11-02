package lucene;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Buscador {
    // Variables
    IndexReader lector;
    IndexSearcher buscador;
    Analizador analizadores;
    Alert msgError, msgAlerta;
    ArrayList<ArrayList<DocumentoEncontrado>> documentosEncontrados;
    Label lblDocumentosEncontrados, lblTiempoConsulta, lblDocumentosColeccion;
    int cantidadPaginas;
    Pattern patronTitulo, patronTexto, patronReferencia, patronEncabezado,
            patronBooleano, patronConsulta, patronPalabra, patronFrase, patronEspeciales;

    Buscador (Analizador _analizadores, Label _lblDocumentosEncontrados, Label _lblTiempoConsulta, Label _lblDocumentosColeccion) {
        msgError = new Alert(Alert.AlertType.ERROR);
        msgError.setTitle("ERROR");
        msgAlerta = new Alert(Alert.AlertType.WARNING);
        msgAlerta.setTitle("ALERTA");
        analizadores = _analizadores;
        lblDocumentosEncontrados = _lblDocumentosEncontrados;
        lblTiempoConsulta = _lblTiempoConsulta;
        lblDocumentosColeccion = _lblDocumentosColeccion;
        patronTitulo = Pattern.compile("(?<titulo>titulo:)(?<contenidoTitulo>(\".*\")|([^ ]+))");
        patronTexto = Pattern.compile("(?<texto>texto:)(?<contenidoTexto>(\".*\")|([^ ]+))");
        patronReferencia = Pattern.compile("(?<ref>ref:)(?<contenidoReferencia>(\".*\")|([^ ]+))");
        patronEncabezado = Pattern.compile("(?<encab>encab:)(?<contenidoEncabezado>(\".*\")|([^ ]+))");
        patronBooleano = Pattern.compile("(?<bool>OR|AND|NOT)");
        patronPalabra = Pattern.compile("(?<palabra>[^ ]+)");
        patronFrase = Pattern.compile("(?<frase>\".*\")");
        patronEspeciales = Pattern.compile("[~*^.\\d]+");
        patronConsulta = Pattern.compile("(?<titulo>(?<campoTitulo>titulo:)(?<contenidoTitulo>(\"[^\"]+\")|([^ ]+)))|" +
                                         "(?<texto>(?<campoTexto>texto:)(?<contenidoTexto>(\"[^\"]+\")|([^ ]+)))|" +
                                         "(?<ref>(?<campoReferencia>ref:)(?<contenidoReferencia>(\"[^\"]+\")|([^ ]+)))|" +
                                         "(?<encab>(?<campoEncabezado>encab:)(?<contenidoEncabezado>(\"[^\"]+\")|([^ ]+)))|" +
                                         "(?<bool>OR|AND|NOT)|" +
                                         "(?<palabra>[^ ]+)");
    }

    // Metodos
    private static String tokensToString(TokenStream stream) throws IOException
    {
        StringBuilder tokens = new StringBuilder();
        CharTermAttribute caracter = stream.addAttribute(CharTermAttribute.class);
        stream.reset();
        while (stream.incrementToken()) {
            //TODO: SI ALGO ESTA FALLANDO, poner .append(" ") al principio
            tokens.append(" ").append(caracter.toString()).append(" ");
        }
        stream.end();
        stream.close();
        return tokens.toString();
    }

    public String quitarStopWords(String campo, String texto){
        try {
            TokenStream streamTextoSinStopWords =  analizadores.analizadorRemoverStopWords.tokenStream(campo,texto);
            return tokensToString(streamTextoSinStopWords);
        }
        catch (IOException e){
            msgError.setTitle("ERROR");
            msgError.setHeaderText("Hubo una error al remover los stop words de " + campo);
            msgError.show();
        }
        return null;
    }

    public String sacarRaices(String campo, String texto) {
        TokenStream streamTextoConRaices = analizadores.analizadorConStemming.tokenStream(campo,texto);
        try {
            return tokensToString(streamTextoConRaices);
        }
        catch (IOException e){
            msgError.setTitle("ERROR");
            msgError.setHeaderText("No se pudo hacer el stemming");
            msgError.show();
        }
        return null;
    }

    public String sacarComandosEspeciales(String texto){
        StringBuilder comandoEspecial = new StringBuilder();
        for (char caracter : texto.toCharArray()){
            String c = String.valueOf(caracter);

            if (c.matches(patronEspeciales.pattern())){
                System.out.println("CHARACTER: " + c);
                comandoEspecial.append(c);
            }

        }
        return comandoEspecial.toString();
    }
    public Query prepararConsulta(String campoSeleccionado, String textoConsulta, boolean personalidada) {
        Query consulta;
        Analyzer analizadorSeleccionado = null;

        if (personalidada){
            StringBuilder consultaPersonalizada = new StringBuilder();
            Matcher grupos = patronConsulta.matcher(textoConsulta);
            while (grupos.find()){
                System.out.println(grupos.group());
                if (grupos.group().matches(patronTitulo.pattern())){
                    String[] partes = grupos.group().split(":");

                    if (partes[1].matches(patronPalabra.pattern()))
                        consultaPersonalizada.append(partes[0]+":").append(quitarStopWords("titulo",partes[1])).append(" ");

                    else if (partes[1].matches(patronFrase.pattern())) {
                        StringBuilder fraseSinStopWords = new StringBuilder();
                        String[] palabrasFrase = partes[1].split(" ");
                        for (String palabra : palabrasFrase) {
                            fraseSinStopWords.append(quitarStopWords("titulo", palabra)).append(" ");
                        }
                        consultaPersonalizada.append(partes[0] + ":").append("\"" + quitarStopWords("titulo", fraseSinStopWords.toString()) + "\"").append(" ");
                    }
                }
                else if (grupos.group().matches(patronReferencia.pattern())){
                    String[] partes = grupos.group().split(":");
                    if (partes[1].matches(patronPalabra.pattern()))
                        consultaPersonalizada.append(partes[0]+":").append(quitarStopWords("ref",partes[1])).append(" ");
                    else if (partes[1].matches(patronFrase.pattern())) {
                        StringBuilder fraseSinStopWords = new StringBuilder();
                        String[] palabrasFrase = partes[1].split(" ");
                        for (String palabra : palabrasFrase){
                            fraseSinStopWords.append(quitarStopWords("ref", palabra)).append(" ");
                        }
                        consultaPersonalizada.append(partes[0] + ":").append("\"" + quitarStopWords("ref", fraseSinStopWords.toString()) + "\"").append(" ");
                    }
                }
                else if (grupos.group().matches(patronTexto.pattern())){
                    String[] partes = grupos.group().split(":");

                    if (partes[1].matches(patronPalabra.pattern()))
                        consultaPersonalizada.append(partes[0]+":").append(sacarRaices("texto",partes[1])).append(" ");

                    else if (partes[1].matches(patronFrase.pattern())) {
                        StringBuilder fraseConStemming = new StringBuilder();
                        String[] palabrasFrase = partes[1].split(" ");
                        for (String palabra : palabrasFrase){
                            fraseConStemming.append(sacarRaices("texto", palabra)).append(" ");
                        }
                        consultaPersonalizada.append(partes[0] + ":").append("\"" + sacarRaices("texto", fraseConStemming.toString()) + "\"").append(" ");
                    }
                }
                else if (grupos.group().matches(patronEncabezado.pattern())){
                    String[] partes = grupos.group().split(":");
                    if (partes[1].matches(patronPalabra.pattern()))
                        consultaPersonalizada.append(partes[0]+":").append(sacarRaices("encab",partes[1])).append(" ");
                    else if (partes[1].matches(patronFrase.pattern())) {
                        StringBuilder fraseConStemming = new StringBuilder();
                        String[] palabrasFrase = partes[1].split(" ");
                        for (String palabra : palabrasFrase){
                            System.out.println(palabra);
                            fraseConStemming.append(sacarRaices("encab", palabra)).append(" ");
                        }
                        consultaPersonalizada.append(partes[0] + ":").append("\"" + sacarRaices("encab", fraseConStemming.toString()) + "\"").append(" ");
                    }
                }
                else if (grupos.group().matches(patronBooleano.pattern())){
                    consultaPersonalizada.append(grupos.group()).append(" ");
                }
                else if (grupos.group().matches(patronPalabra.pattern())){
                    consultaPersonalizada.append(sacarRaices("texto",grupos.group())).append(" ");
                }
            }
            campoSeleccionado = "texto";
            textoConsulta = consultaPersonalizada.toString();
            analizadorSeleccionado = analizadores.analizadorSimple;
        }
        else {
            if (campoSeleccionado.equals("")) {
                campoSeleccionado = "texto";
                analizadorSeleccionado = analizadores.analizadorConStemming;
            }
            else if (campoSeleccionado.equals("titulo") || campoSeleccionado.equals("ref")){
                analizadorSeleccionado = analizadores.analizadorRemoverStopWords;
            }
            else {
                analizadorSeleccionado = analizadores.analizadorConStemming;
            }
        }
        QueryParser parser = new QueryParser(campoSeleccionado, analizadorSeleccionado);
        String consultaSinTildes = analizadores.limpiarAcentos(textoConsulta, true);
        try {
            System.out.println("consulta ANTES de parsing:" + consultaSinTildes);
            consulta = parser.parse(consultaSinTildes);
            System.out.println("consulta DESPUES de parsing:" + consulta);
            return consulta;
        }
        catch (ParseException e){
            msgAlerta.setTitle("ERROR");
            msgAlerta.setHeaderText("Lo sentimos, hubo un problema al procesar la consulta");
            msgAlerta.setContentText("Inténtelo de nuevo.");
            msgAlerta.show();
        }
        return null;
    }

    public void ejecutarConsulta(Query consulta, int cantidadPorPagina){

        long inicio = System.currentTimeMillis();
        int numeroResultados = 0;

        try {
            TopDocs resultados = buscador.search(consulta, cantidadPorPagina);
            ScoreDoc[] docResultados = resultados.scoreDocs;

            numeroResultados = Math.toIntExact(resultados.totalHits.value);
            cantidadPaginas = (int) Math.ceil((float) numeroResultados / (float) cantidadPorPagina);

            int posicion = 0;
            float puntaje = 0;

            for (int i = 0; i < cantidadPaginas; i++){
                ArrayList<DocumentoEncontrado> lista = new ArrayList<>();
                for (ScoreDoc scoreDoc : docResultados) {
                    puntaje = scoreDoc.score;
                    Document doc = buscador.doc(scoreDoc.doc);
                    DocumentoEncontrado documento = new DocumentoEncontrado();
                    if (doc != null) {
                        posicion++;
                        documento.llenarDatos(posicion, puntaje, doc);
                    }
                    lista.add(documento);
                }
                documentosEncontrados.add(lista);
                docResultados = buscador.searchAfter(docResultados[docResultados.length-1],consulta,cantidadPorPagina).scoreDocs;
            }
            long fin = System.currentTimeMillis();
            float tiempo = (float) ((fin - inicio))/1000;
            lblDocumentosEncontrados.setText(numeroResultados + " documentos encontrados.");
            lblTiempoConsulta.setText("Duración: " + tiempo + " segundos");
        }
        catch (IOException e){
            msgError.setTitle("ERROR");
            msgError.setHeaderText("Hubo un error al ejecutar la consulta");
            msgError.show();
        }
    }

    public ArrayList<ArrayList<DocumentoEncontrado>> buscarDocumento (String directorioIndice, String campoSeleccionado,
                                                           String textoConsulta, int cantidadPorPagina, boolean personalizada) {

        documentosEncontrados = new ArrayList<ArrayList<DocumentoEncontrado>>();

        try {
            Path directorioIndices = Paths.get(".", directorioIndice);
            lector = DirectoryReader.open(FSDirectory.open(directorioIndices));
            lblDocumentosColeccion.setText("Documentos en la colección: " + lector.numDocs());
        }
        catch (IOException e){
            msgError.setTitle("ERROR");
            msgError.setHeaderText("Huno un error al abrir el directorio de índices");
            msgError.setContentText("Por favor, verifique que la dirección suministrada es correcta");
        }

        buscador = new IndexSearcher(lector);

        Query consulta = prepararConsulta(campoSeleccionado,textoConsulta, personalizada);

        if (consulta != null){
            ejecutarConsulta(consulta,cantidadPorPagina);
        }

        return documentosEncontrados;
    }
}
