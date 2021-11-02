package lucene;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.tartarus.snowball.ext.KpStemmer;
import org.tartarus.snowball.ext.SpanishStemmer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;

public class Analizador {

    public static CharArraySet stopWords;
    public Analyzer analizadorSimple, analizadorRemoverStopWords, analizadorConStemming;
    public TokenFilter filtrarTildes;

    Analizador() {
        analizadorSimple = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String s) {
                TokenizerEspanol tokenizerEspanol = new TokenizerEspanol();
                TokenStream filtro = new LowerCaseFilter(tokenizerEspanol);
                return new TokenStreamComponents(tokenizerEspanol,filtro);
            }
        };

        analizadorRemoverStopWords = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String s) {
                TokenizerEspanol tokenizerEspanol = new TokenizerEspanol();
                TokenStream filtro = new LowerCaseFilter(tokenizerEspanol);
                filtro = new StopFilter(filtro, stopWords);
                return new TokenStreamComponents(tokenizerEspanol, filtro);
            }
        };

        analizadorConStemming = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String s) {
                TokenizerEspanol tokenizerEspanol = new TokenizerEspanol();
                TokenStream filtro = new LowerCaseFilter(tokenizerEspanol);
                // Se asignan los stopwords al analizador
                filtro = new StopFilter(filtro, stopWords);
                // Se hace el stemming de acuerdo a la implementación Snowball del stemmer en español
                SpanishStemmer stemmerEspanol = new SpanishStemmer();
                filtro = new SnowballFilter(filtro, stemmerEspanol);
                return new TokenStreamComponents(tokenizerEspanol, filtro);
            }
        };
    }

    public String limpiarAcentos(String cadena, boolean busqueda) {
        String limpio = null;
        if (cadena !=null) {
            String valor = cadena;
            // Normalizar texto para eliminar acentos, dieresis, cedillas y tildes
            limpio = Normalizer.normalize(valor, Normalizer.Form.NFD);
            // Quitar caracteres no ASCII excepto la enie, interrogacion que abre, exclamacion que abre, grados, U con dieresis.
            if (busqueda)
                limpio = limpio.replaceAll("[^A-Za-z\\u0303\\-+&|!(){}\\[\\]^\"~*?: \\d]", "");
            else
                limpio = limpio.replaceAll("[^A-Za-z\\u0303 ]", "");
            // Regresar a la forma compuesta, para poder comparar la enie con la tabla de valores
            limpio = Normalizer.normalize(limpio, Normalizer.Form.NFC);
        }
        return limpio;
    }


    /*
        Entrada: Nombre del archivo de texto que contiene la lista de stopwords separadas por lineas.
        Resultado: Carga los stop words que utiliza analizadorRemoverStopWords y analizadorConStemming
                   para ignorar las palabras comunes de un texto
        Excepciones: Si hay un problema en la lectura se manda una excepción
     */
    public void leerStopWords(String nombreArchivo) throws IOException {
        ArrayList<String> listaStopWords = new ArrayList<String>();
        Path ubicacionStopWords = Paths.get(".",nombreArchivo);
        File archivoStopWords = new File(ubicacionStopWords.toString());
        try (BufferedReader lector = new BufferedReader(new FileReader(archivoStopWords))) {
            String stopWord;
            while ((stopWord = lector.readLine()) != null){
                listaStopWords.add(stopWord);
            }
        }
        stopWords = new CharArraySet(listaStopWords,false);
    }
}
