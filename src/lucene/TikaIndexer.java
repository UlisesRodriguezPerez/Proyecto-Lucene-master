package lucene;

import java.io.InputStream;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;

//public class HtmlParse {
//        BodyContentHandler handler = new BodyContentHandler();
//        HtmlParser htmlParser = new HtmlParser();
//        Metadata metadata          = new Metadata();
//        ParseContext pcontext      = new ParseContext();
//
//        public
//        try (InputStream stream = AutoDetectParseExample.class.getResourceAsStream("index.html")) {
//            htmlParser.parse(stream, handler, metadata,pcontext);
//        }
//
//        System.out.println("Document Content:" + handler.toString());
//        System.out.println("Document Metadata:");
//        String[] metadatas = metadata.names();
//        for(String meta : metadatas) {
//            System.out.println(meta + ":   " + metadata.get(meta));
//        }
//    }
//}
