package pe.net.sdp.foto;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.FileVisitResult;
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
        //LOGGER.info(String.format("File: %s", file));
        consolidador.leerFoto(file, tipoFoto);
        return CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        //LOGGER.info(String.format("Directory: %s", dir));
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        System.err.println(exc);
        return CONTINUE;
    }
}