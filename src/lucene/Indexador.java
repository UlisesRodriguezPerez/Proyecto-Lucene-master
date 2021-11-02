package lucene;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;


public class Indexador
{
	public IndexWriter writer;
	CharArraySet stopWords;
	ArrayList<String> bodies, referencias, titulos, encabezados;

	public Indexador() {
        bodies = new ArrayList<String>();
        referencias = new ArrayList<String>();
        titulos = new ArrayList<String>();
        encabezados = new ArrayList<String>();
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

	private static void mostrarTokens(Analyzer analyzer,String text) throws IOException
	{
		TokenStream stream = analyzer.tokenStream(null,new StringReader(text));
		CharTermAttribute caracter = stream.addAttribute(CharTermAttribute.class);
		stream.reset();
		while (stream.incrementToken()) {
			System.out.print(" [" + caracter.toString()+ "]" + "\n");
		}
		stream.end();
		stream.close();
	}

	public void configurarIndexador() throws IOException {
		Analizador analizador = new Analizador();
		analizador.leerStopWords();
		Path rutaDirectorioIndice = Paths.get(".","indices");
		Directory directorioIndice = FSDirectory.open(rutaDirectorioIndice);
		IndexWriterConfig configuracionIndice = new IndexWriterConfig(analizador);
		try {
			writer = new IndexWriter(directorioIndice, configuracionIndice);
		}
		catch (IOException e){
			System.out.println("Hubo un problema al configurar el indexador jaja");
		}
	}

	public void indexarContenidos(String html) {
		Document DocumentoLucene = new Document();

		org.jsoup.nodes.Document Html = Jsoup.parse(html);

		// Se indexa el body del html puro
		String HTML = Html.html();
//		IndexableField htmlPuro = new TextField("html",HTML,Field.Store.YES);
		// Se indexa el body del html
		HTML = Html.body().text();
		IndexableField body = new TextField("texto",HTML, Field.Store.YES);
		// Se indexa el <title>
		HTML = Html.getElementsByTag("title").text();
		IndexableField title = new TextField("titulo",HTML,Field.Store.YES);

		// Se indexa los <h?>
		StringBuilder encabezados = new StringBuilder();
		encabezados.append(Html.getElementsByTag("h1").text());
		encabezados.append(Html.getElementsByTag("h2").text());
		encabezados.append(Html.getElementsByTag("h3").text());
		encabezados.append(Html.getElementsByTag("h4").text());
		encabezados.append(Html.getElementsByTag("h5").text());
		encabezados.append(Html.getElementsByTag("h6").text());

		IndexableField headers = new TextField("encab",encabezados.toString(),Field.Store.YES);

		// Se indexa las <a>
		HTML = Html.getElementsByTag("a").text();
		IndexableField links = new TextField("ref",HTML,Field.Store.YES);

		// Con stemming
		DocumentoLucene.add(headers);
		DocumentoLucene.add(body);
		// Sin stemming
//		DocumentoLucene.add(htmlPuro);
		DocumentoLucene.add(links);
		DocumentoLucene.add(title);

		try {
			writer.addDocument(DocumentoLucene);
		}
		catch (IOException e) {
			System.out.println("Hubo un error en la indexacion");
		}
	}
}