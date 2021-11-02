package lucene;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.sun.javafx.css.parser.Token;
import javafx.scene.control.Alert;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;

import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;
import org.tartarus.snowball.ext.SpanishStemmer;


public class Indexador
{
	Analizador analizadores;
	IndexWriter writer;
	ArrayList<String> bodies, referencias, titulos, encabezados;
	Alert msgError = new Alert(Alert.AlertType.ERROR);
	Alert msgAlerta = new Alert(Alert.AlertType.WARNING);


	public Indexador(Analizador _analizadores) {
        bodies = new ArrayList<String>();
        referencias = new ArrayList<String>();
        titulos = new ArrayList<String>();
        encabezados = new ArrayList<String>();
        analizadores = _analizadores;
	}

	private static String obtenerTokens(TokenStream stream) throws IOException
	{
		StringBuilder tokens = new StringBuilder();
		CharTermAttribute caracter = stream.addAttribute(CharTermAttribute.class);
		stream.reset();
		while (stream.incrementToken()) {
			tokens.append(" ").append(caracter.toString()).append(" ");
		}
		stream.end();
		stream.close();
		return tokens.toString();
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

	public void configurarIndexador(String directorioIndices, boolean actualizar) throws IOException {
		analizadores = new Analizador();
		analizadores.leerStopWords("stop_words");
		Path rutaDirectorioIndice = Paths.get(".",directorioIndices);
		Directory directorioIndice = FSDirectory.open(rutaDirectorioIndice);
		IndexWriterConfig configuracionIndice = new IndexWriterConfig(analizadores.analizadorSimple);
		if (actualizar)
			configuracionIndice.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		else
			configuracionIndice.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		try {
			writer = new IndexWriter(directorioIndice, configuracionIndice);
		}
		catch (IOException e){
			msgError.setTitle("ERROR");
			msgError.setHeaderText("Hubo un problema al configurar el indexador");
			msgError.setContentText("Revise que el archivo de stop words es correxto y que el directorio del índice es correcto");
			msgError.show();
		}
	}

	public String sacarRaices(String campo, String texto) {
		TokenStream streamTextoConRaices = analizadores.analizadorConStemming.tokenStream(campo,texto);
		try {
			return obtenerTokens(streamTextoConRaices);
		}
		catch (IOException e){
			msgError.setTitle("ERROR");
			msgError.setHeaderText("No se pudo hacer el stemming");
			msgError.show();
		}
		return null;
	}

	public String quitarStopWords(String campo, String texto){
		try {
			TokenStream streamTextoSinStopWords =  analizadores.analizadorRemoverStopWords.tokenStream(campo,texto);
			return obtenerTokens(streamTextoSinStopWords);
		}
		catch (IOException e){
			msgError.setTitle("ERROR");
			msgError.setHeaderText("Hubo una error al remover los stop words de " + campo);
			msgError.show();
		}
		return null;
	}

	public IndexableField crearCampoTexto(String nombre, String html, boolean guardarEnIndice){
		if (guardarEnIndice)
			 return new TextField(nombre, html, Field.Store.YES);
		else
			return new TextField(nombre, html, Field.Store.NO);
	}

	public void indexarContenidos(Html_Indexado html_indexado, boolean actualizar, int id) {

		Document DocumentoLucene = new Document();

		org.jsoup.nodes.Document Html = Jsoup.parse(html_indexado.getHTML());

		IndexableField nombreArchivo = new TextField("archivo",html_indexado.getArchivo(),Field.Store.YES);
		IndexableField posicionInicial = new TextField("posicionInicial",String.valueOf(html_indexado.getLineaInicial()), Field.Store.YES);
		IndexableField largoDocumento = new TextField("largoDocumento",String.valueOf(html_indexado.getLargo()), Field.Store.YES);

		String HTML;

		// Se indexan primero los valores SIN STEMMING

		// Se indexa el <title>
		HTML = Html.getElementsByTag("title").text();
		IndexableField tituloMostrar = new TextField("tituloMostrar",HTML,Field.Store.YES);

		HTML = quitarStopWords("titulo",HTML);
		HTML = analizadores.limpiarAcentos(HTML,false);

		IndexableField tituloBuscar = null;
		if (HTML != null)
			tituloBuscar = new TextField("titulo", HTML, Field.Store.YES);
		else {
			msgAlerta.setTitle("ALERTA");
			msgAlerta.setHeaderText("La pagina " + html_indexado.archivo + "tiene un titulo vacio");
			msgAlerta.show();
		}

		// Se indexa las <a>
		HTML = Html.getElementsByTag("a").text();
		HTML = quitarStopWords("ref",HTML);
		HTML = analizadores.limpiarAcentos(HTML,false);
		IndexableField links = null;
		if (HTML != null)
			links = new TextField("ref",HTML, Field.Store.YES);
		else {
			msgAlerta.setTitle("ALERTA");
			msgAlerta.setHeaderText("La pagina " + html_indexado.archivo + " no tiene referencias");
		}

		// Se indexan los campos CON STEMMING

		// Se indexa el <body>
		HTML = Html.body().text();

		// Se sacan los primeros 100 caracteres para mostrar una vista previa del texto.
		StringBuilder Palabra = new StringBuilder();
		StringBuilder Resumen100 = new StringBuilder();
		int numeroPalabras = 0;
		int numChar = 0;
		char[] charsHTML = HTML.toCharArray();
		while (numChar < charsHTML.length && numeroPalabras < 100){
			if (charsHTML[numChar] == ' '){
				numeroPalabras++;
				Resumen100.append(Palabra.toString());
				Palabra.setLength(0);
				Palabra.append(" ");
			}
			else {
				Palabra.append(charsHTML[numChar]);
			}
			numChar++;
		}

		IndexableField resumen = new TextField("resumen",Resumen100.toString(),Field.Store.YES);
		// Se quitan stopwords, aplica stemming y se limpian acentos para el texto completo.
		HTML = sacarRaices("texto",HTML);
		HTML = analizadores.limpiarAcentos(HTML,false);
//		System.out.println("VERSION COMPLETA: " + HTML + "  Tamaño: " + HTML.length());
		IndexableField body = new TextField("texto",HTML, Field.Store.YES);

		// Se indexa los <h?>
		StringBuilder encabezados = new StringBuilder();
		encabezados.append(Html.getElementsByTag("h1").text()+" ");
		encabezados.append(Html.getElementsByTag("h2").text()+" ");
		encabezados.append(Html.getElementsByTag("h3").text()+" ");
		encabezados.append(Html.getElementsByTag("h4").text()+" ");
		encabezados.append(Html.getElementsByTag("h5").text()+" ");
		encabezados.append(Html.getElementsByTag("h6").text()+" ");

		HTML = encabezados.toString();
		HTML = sacarRaices("encab",HTML);
		HTML = analizadores.limpiarAcentos(HTML,false);
		IndexableField headers = new TextField("encab",HTML, Field.Store.YES);

		DocumentoLucene.add(nombreArchivo);
		DocumentoLucene.add(posicionInicial);
		DocumentoLucene.add(largoDocumento);
		DocumentoLucene.add(tituloMostrar);
		DocumentoLucene.add(links);
		DocumentoLucene.add(tituloBuscar);
		DocumentoLucene.add(headers);
		DocumentoLucene.add(body);
		DocumentoLucene.add(resumen);

		try {
			writer.addDocument(DocumentoLucene);
		}
		catch (IOException e) {
			msgError.setTitle("ERROR");
			msgError.setHeaderText("Hubo un error en la indexacion");
			msgError.setContentText("Revise que las rutas a la carpeta de indexacion y stop words son correctas");

		}
	}
}