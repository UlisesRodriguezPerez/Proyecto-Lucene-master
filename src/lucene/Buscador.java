package lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Buscador {
    public void buscarDocumento (String searchVal, int cantidadPorPagina)
    {
        List<DocumentoEncontrado> retVal = new ArrayList<DocumentoEncontrado>();

        try
        {
            Path directorioIndices = Paths.get(".","/indices");

            String queries = "";
            IndexReader lector = DirectoryReader.open(FSDirectory.open(directorioIndices));
            IndexSearcher buscador = new IndexSearcher(lector);
            Analizador analizador = new Analizador();
            analizador.leerStopWords();

            BufferedReader in = null;
            in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

            QueryParser parser = new QueryParser("titulo",analizador);
            Query q = parser.parse(searchVal);
            System.out.println("Buscando : " + q.toString("titulo"));

            // Empieza la busqueda
            long inicio = System.currentTimeMillis();

            buscador.search(q,100, Sort.RELEVANCE);

            TopDocs resultados = buscador.search(q, 5 * cantidadPorPagina);
            ScoreDoc[] hits = resultados.scoreDocs;

            int numTotalHits = Math.toIntExact(resultados.totalHits.value);
            System.out.println(numTotalHits + " documentos relacionados en total");

            int start = 0;
            int end = Math.min(numTotalHits, cantidadPorPagina);

//            hits = buscador.search(q, numTotalHits).scoreDocs;
            for (ScoreDoc hit : hits) {
                System.out.println("doc="+hit.doc+" score="+hit.score);
                Document doc = buscador.doc(hit.doc);
                String titulo = doc.get("ref");
                System.out.println("Referencias: " + titulo);
            }

            long fin = System.currentTimeMillis();
            double tiempo = (double) ((fin - inicio)/100);
            System.out.println(tiempo +" segundos");

//            if (allFound.scoreDocs != null)
//            {
//                for (ScoreDoc doc : allFound.scoreDocs)
//                {
//                    System.out.println("Score: " + doc.score);
//
//                    int docidx = doc.doc;
//                    Document docRetrieved = searcher.doc(docidx);
//                    if (docRetrieved != null)
//                    {
//                        FoundDocument docToAdd = new FoundDocument();
//
//                        IndexableField field = docRetrieved.getField("TITLE");
//                        if (field != null)
//                        {
//                            docToAdd.setTitle(field.stringValue());
//                        }
//
//                        field = docRetrieved.getField("DOCID");
//                        if (field != null)
//                        {
//                            docToAdd.setDocumentId(field.stringValue());
//                        }
//
//                        field = docRetrieved.getField("KEYWORDS");
//                        if (field != null)
//                        {
//                            docToAdd.setKeywords(field.stringValue());
//                        }
//
//                        field = docRetrieved.getField("CATEGORY");
//                        if (field != null)
//                        {
//                            docToAdd.setCategory(field.stringValue());
//                        }
//
//                        if (docToAdd.validate())
//                        {
//                            retVal.add(docToAdd);
//                        }
//                    }
//                }
//            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
