package lucene;

import org.apache.lucene.analysis.util.CharTokenizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TokenizerEspanol extends CharTokenizer {
    Pattern patron = Pattern.compile("[A-Za-zÁÉÍÓÚÜáéíóúüÑñ]");

    @Override
    protected boolean isTokenChar(int i) {
        Matcher matcher = patron.matcher(String.valueOf((char) i));
        return matcher.matches();
    }
}