package lucene;

public class Html_Indexado {
    String archivo;
    String html;
    int lineaInicial;
    int largo;

    Html_Indexado(String pArchivo, String pHtml, int pLinea, int pLargo){
        archivo = pArchivo;
        html = pHtml;
        lineaInicial = pLinea;
        largo = pLargo;
    }

    public String getHTML(){
        return html;
    }

    public int getLineaInicial() {
        return lineaInicial;
    }

    public int getLargo() {
        return largo;
    }

    public String getArchivo(){
        return archivo;
    }

}
