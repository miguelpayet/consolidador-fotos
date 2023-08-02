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

public abstract class Actualizador {

    protected static final Logger LOGGER = LogManager.getLogger(Foto.class.getName());

    public abstract void actualizar() throws IOException;

    protected void copiarArchivo(String unArchivoOrigen, String unArchivoDestino) throws IOException {
        crearDirectorio(unArchivoDestino);
        try {
            Files.copy(Path.of(unArchivoOrigen), Path.of(unArchivoDestino), StandardCopyOption.COPY_ATTRIBUTES);
        } catch (FileAlreadyExistsException e) {
            LOGGER.error("archivo ya existe {} -> {}", unArchivoOrigen, unArchivoDestino, e);
        }
    }

    protected void crearDirectorio(String unArchivo) throws IOException {
        String directorioDestino = FilenameUtils.getFullPath(unArchivo);
        File dir = new File(directorioDestino);
        if (!dir.exists()) {
            FileUtils.forceMkdir(dir);
        }
    }

}
