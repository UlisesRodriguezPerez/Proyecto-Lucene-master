package lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.tartarus.snowball.ext.SpanishStemmer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Analizador extends Analyzer {

    // Atributos
    CharArraySet stopWords;

    // Metodos
    @Override
    protected TokenStreamComponents createComponents(String s) {
        TokenizerEspanol tokenizerEspanol = new TokenizerEspanol();
        // Se asignan los stopwords al analizador
        TokenStream filtro = new LowerCaseFilter(tokenizerEspanol);
        filtro = new StopFilter(filtro, stopWords);
        // Se hace el stemming de acuerdo a la implementación Snowball del stemmer en español
        SpanishStemmer stemmerEspanol = new SpanishStemmer();
        filtro = new SnowballFilter(filtro, stemmerEspanol);
        return new TokenStreamComponents(tokenizerEspanol, filtro);
    }

    public void leerStopWords() throws IOException {
        ArrayList<String> listaStopWords = new ArrayList<String>();
        Path ubicacionStopWords = Paths.get(".","stop_words");
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
