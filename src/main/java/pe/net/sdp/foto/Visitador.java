package pe.net.sdp.foto;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;

public class Visitador extends SimpleFileVisitor<Path> {

    private static final Logger LOGGER = LogManager.getLogger(Visitador.class.getName());
    private final Consolidador consolidador;
    private int tipoFoto;

    public Visitador(Consolidador consolidador) {
        this.consolidador = consolidador;
    }

    public void setTipoFoto(int tipoFoto) {
        this.tipoFoto = tipoFoto;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
        boolean isHidden;
        try {
            isHidden = Files.isHidden(file);
        } catch (IOException e) {
            isHidden = false;
        }
        if (!isHidden) {
            //LOGGER.info(String.format("File: %s", file));
            consolidador.leerFoto(file, tipoFoto);
        }
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        LOGGER.error(String.format("%s -> %s", exc.getMessage(), file), exc);
        return CONTINUE;
    }
}