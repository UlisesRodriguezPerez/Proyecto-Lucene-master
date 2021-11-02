import lucene.Buscador;
import lucene.Indexador;
import lucene.Lector;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;


public class Main {

    public static void main(String[] args) throws IOException {
//        long inicio = System.currentTimeMillis();
//
//        Lector lector = new Lector();
//        ArrayList<ArrayList<String>> archivos = lector.obtenerDocumentos();
//
//        Indexador indexador= new Indexador();
//        indexador.leerStopWords();
//        indexador.configurarIndexador();
//
//        int cont = 0;
//        for (ArrayList<String> htmls : archivos) {
//            for (String html : htmls) {
//                indexador.indexarContenidos(html);
//                cont++;
//            }
//        }
//        indexador.writer.close();
//        System.out.println(cont + " indexados");
//
//        long fin = System.currentTimeMillis();
//
//        double tiempo = (double) ((fin - inicio)/1000);
//
//        System.out.println(tiempo +" segundos");

        Buscador buscador = new Buscador();
        String opcion = "";
        Scanner scanner = new Scanner(System.in);
        while (!opcion.equals("s")){
            System.out.println("Ingrese su consulta: ");
            opcion = scanner.nextLine();
            if (!opcion.equals("s"))
                buscador.buscarDocumento(opcion,20);
            else
                break;
        }

    }
}