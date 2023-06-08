package pe.net.sdp.foto;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Logger;

import static java.nio.file.FileVisitResult.CONTINUE;

public class Visitador extends SimpleFileVisitor<Path> {

    private static final Logger LOGGER = Logger.getLogger(Visitador.class.getName());
    private final Consolidador consolidador;
    private int tipoFoto;

    public Visitador(Consolidador consolidador) {
        this.consolidador = consolidador;
    }

    public int getTipoFoto() {
        return tipoFoto;
    }

    public void setTipoFoto(int tipoFoto) {
        this.tipoFoto = tipoFoto;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
        //LOGGER.info(String.format("File: %s", file));
        consolidador.consolidar(file, tipoFoto);
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