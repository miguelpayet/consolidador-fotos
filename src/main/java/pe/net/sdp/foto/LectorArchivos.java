package pe.net.sdp.foto;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LectorArchivos {

    private static final Logger LOGGER = LogManager.getLogger(LectorArchivos.class.getName());
    private final Consolidador consolidador;
    private final Visitador visitador;

    public LectorArchivos(Consolidador unConsolidador) {
        consolidador = unConsolidador;
        visitador = new Visitador(consolidador);
    }

    public void leerArchivos() {
        LOGGER.info("visitando {}", consolidador.getRutaDestino());
        visitador.setTipoFoto(Foto.DESTINO);
        try {
            Files.walkFileTree(Path.of(consolidador.getRutaDestino()), visitador);
        } catch (IOException e) {
            LOGGER.error("error al leer ruta destino {}", e.getMessage());
        }
        LOGGER.info("visitando {}", consolidador.getRutaOrigen());
        visitador.setTipoFoto(Foto.ORIGEN);
        try {
            Files.walkFileTree(Path.of(consolidador.getRutaOrigen()), visitador);
        } catch (IOException e) {
            LOGGER.error("error al leer ruta destino {}", e.getMessage());
        }
    }

}
