package pe.net.sdp.foto;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeneradorRutasUnicas {

    public GeneradorRutasUnicas() {
    }

    public String obtenerNombreArchivoUnico(String unArchivoDestino) {
        while ((new File(unArchivoDestino)).exists()) {
            unArchivoDestino = obtenerSiguienteNombreArchivo(unArchivoDestino);
        }
        return unArchivoDestino;
    }

    public String obtenerNombreDirectorioUnico(String unDirectorioDestino) {
        unDirectorioDestino = quitarUltimoSlash(unDirectorioDestino);
        while ((new File(unDirectorioDestino)).exists()) {
            unDirectorioDestino = obtenerSiguienteNombreDirectorio(unDirectorioDestino);
        }
        return unDirectorioDestino;
    }

    private String obtenerSiguienteNombreArchivo(String unArchivoDestino) {
        String baseName = FilenameUtils.getBaseName(unArchivoDestino);
        String directorioDestino = quitarUltimoSlash(FilenameUtils.getFullPath(unArchivoDestino));
        String extension = FilenameUtils.getExtension(unArchivoDestino);
        String regex = "^(.*\\-)([0-9]*)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(baseName);
        if (matcher.matches()) {
            int numero = Integer.parseInt(matcher.group(2)) + 1;
            unArchivoDestino = String.format("%s/%s%02d.%s", directorioDestino, matcher.group(1), numero, extension);
        } else {
            unArchivoDestino = String.format("%s/%s-%02d.%s", directorioDestino, baseName, 1, extension);
        }
        return unArchivoDestino;
    }

    private String obtenerSiguienteNombreDirectorio(String unDirectorioDestino) {
        unDirectorioDestino = quitarUltimoSlash(unDirectorioDestino);
        String regex = "^(.*\\-)([0-9]*)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(unDirectorioDestino);
        if (matcher.matches() && matcher.groupCount() == 2) {
            int numero = Integer.parseInt(matcher.group(2)) + 1;
            unDirectorioDestino = String.format("%s%02d", matcher.group(1), numero);
        } else {
            unDirectorioDestino = String.format("%s-%02d", unDirectorioDestino, 1);
        }
        return unDirectorioDestino;
    }

    private String quitarUltimoSlash(String unDirectorioDestino) {
        if (unDirectorioDestino.endsWith("/")) {
            unDirectorioDestino = unDirectorioDestino.substring(0, unDirectorioDestino.length() - 1);
        }
        return unDirectorioDestino;
    }

}
