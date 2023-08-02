package pe.net.sdp.foto;

import java.util.regex.Pattern;

public class Filtro {

    private Pattern[] patterns;

    public Filtro(String[] unosRegex) {
        patterns = new Pattern[unosRegex.length];
        int i = 0;
        for (String regex : unosRegex) {
            patterns[i++] = Pattern.compile(regex);
        }
    }

    public Filtro(String unRegex) {
        patterns = new Pattern[1];
        patterns[0] = Pattern.compile(unRegex);
    }

    public boolean estaFiltrado(String unaCadena) {
        for (Pattern p : patterns) {
            if (p.matcher(unaCadena).matches()) {
                return true;
            }
        }
        return false;
    }

}
