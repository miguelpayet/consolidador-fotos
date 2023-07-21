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
        try {
            Files.copy(Path.of(foto.getArchivoOrigen()), Path.of(foto.getArchivoDestino()), StandardCopyOption.COPY_ATTRIBUTES);
        } catch (FileAlreadyExistsException e) {
            LOGGER.error("archivo ya existe {} -> {}", foto.getArchivoOrigen(), foto.getArchivoDestino(), e);
        }
    }

    private void crearDirectorio() throws IOException {
        String directorioDestino = FilenameUtils.getFullPath(foto.getArchivoDestino());
        File dir = new File(directorioDestino);
        if (!dir.exists()) {
            FileUtils.forceMkdir(dir);
        }
    }

    private void moverArchivo() throws IOException {
        crearDirectorio();
        Files.move(Path.of(foto.getArchivoOrigen()), Path.of(foto.getArchivoDestino()));
    }


}
