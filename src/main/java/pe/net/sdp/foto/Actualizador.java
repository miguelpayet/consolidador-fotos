package pe.net.sdp.foto;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
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
        String archivoDestino = obtenerNombreUnico(foto.getArchivoDestino());
        Files.copy(Path.of(foto.getArchivoOrigen()), Path.of(archivoDestino), StandardCopyOption.COPY_ATTRIBUTES);
    }

    private void moverArchivo() throws IOException {
        String archivoDestino = obtenerNombreUnico(foto.getArchivoDestino());
        Files.move(Path.of(foto.getArchivoOrigen()), Path.of(archivoDestino));
    }

    private String obtenerNombreUnico(String archivoDestino) {
        int posicion = 0;
        File fileDestino = new File(archivoDestino);
        String baseName = FilenameUtils.getBaseName(archivoDestino);
        String extension = FilenameUtils.getExtension(archivoDestino);
        while (fileDestino.exists()) {
            archivoDestino = String.format("%s-%02d.%s", baseName, ++posicion, extension);
            fileDestino = new File(archivoDestino);
        }
        return archivoDestino;
    }

}
