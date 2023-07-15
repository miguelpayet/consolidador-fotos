package pe.net.sdp.foto;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Actualizador {

    private static final Logger LOGGER = LogManager.getLogger(Foto.class.getName());
    private final Foto foto;

    public Actualizador(Foto unaFoto) {
        foto = unaFoto;
    }

    public void actualizar() throws IOException {
        if (foto.getTipo() == Foto.ORIGEN) {
            copiarArchivo();
        } else {
            moverArchivo();
        }
    }

    private void copiarArchivo() throws IOException {
        crearDirectorio();
        String archivoDestino = obtenerNombreArchivoUnico(foto.getArchivoDestino());
        try {
            Files.copy(Path.of(foto.getArchivoOrigen()), Path.of(archivoDestino), StandardCopyOption.COPY_ATTRIBUTES);
        } catch (FileAlreadyExistsException e) {
            LOGGER.error("archivo ya existe {} -> {}", foto.getArchivoOrigen(), archivoDestino, e);
        }
    }

    private void crearDirectorio() throws IOException {
        String directorioDestino = FilenameUtils.getFullPath(foto.getArchivoDestino());
        String directorio = obtenerNombreDirectorioUnico(directorioDestino);
        if (!directorio.equals(directorioDestino)) {
            int lastSlashIndex = directorio.lastIndexOf("/");
            String destinoFoto;
            if (lastSlashIndex > 0) {
                destinoFoto = directorioDestino + FilenameUtils.getName(foto.getArchivoDestino());
            } else {
                destinoFoto = directorioDestino + "/" + FilenameUtils.getName(foto.getArchivoDestino());
            }
            foto.setArchivoDestino(destinoFoto);
        }
        File dir = new File(directorio);
        if (!dir.exists()) {
            FileUtils.forceMkdir(dir);
        }
    }

    private void moverArchivo() throws IOException {
        crearDirectorio();
        String archivoDestino = obtenerNombreArchivoUnico(foto.getArchivoDestino());
        Files.move(Path.of(foto.getArchivoOrigen()), Path.of(archivoDestino));
    }

    private String obtenerNombreArchivoUnico(String archivoDestino) {
        int posicion = 0;
        File fileDestino = new File(archivoDestino);
        String baseName = FilenameUtils.getBaseName(archivoDestino);
        String directorioDestino = FilenameUtils.getFullPath(archivoDestino);
        String extension = FilenameUtils.getExtension(archivoDestino);
        while (fileDestino.exists()) {
            archivoDestino = String.format("%s%s-%02d.%s", directorioDestino, baseName, ++posicion, extension);
            fileDestino = new File(archivoDestino);
        }
        return archivoDestino;
    }

    private String obtenerNombreDirectorioUnico(String unDirectorioDestino) {
        File dirDestino = new File(unDirectorioDestino);
        while (dirDestino.exists()) {
            unDirectorioDestino = obtenerSiguienteNombreDirectorio(unDirectorioDestino);
            dirDestino = new File(unDirectorioDestino);
        }
        return unDirectorioDestino;
    }

    private String obtenerSiguienteNombreDirectorio(String unDirectorioDestino) {
        int posicion = 0;
        String numeroFormateado = String.format("%02d", ++posicion);
        int ultimoSlash = unDirectorioDestino.lastIndexOf("/");
        if (ultimoSlash > 0) {
            unDirectorioDestino = unDirectorioDestino.substring(0, ultimoSlash) + "-" + numeroFormateado + unDirectorioDestino.substring(ultimoSlash);
        } else {
            unDirectorioDestino = unDirectorioDestino + "-" + numeroFormateado;
        }
        return unDirectorioDestino;
    }

}
