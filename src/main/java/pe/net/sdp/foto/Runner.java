package pe.net.sdp.foto;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Runner {

    private static final Logger LOGGER = LogManager.getLogger(Foto.class);
    private static String RUTA_DESTINO;
    private static String RUTA_ORIGEN;

    private static void leerConfiguracion() {
        RUTA_ORIGEN = SinglePropertyLoader.getInstance().getProperty("ruta_input");
        RUTA_DESTINO = SinglePropertyLoader.getInstance().getProperty("ruta_output");
    }

    public static void main(String[] args) {
        LOGGER.info("inicio");
        leerConfiguracion();
        Consolidador consolidador = new Consolidador(RUTA_DESTINO);
        Visitador pf = new Visitador(consolidador);
        pf.setTipoFoto(Foto.DESTINO);
        try {
            Files.walkFileTree(Path.of(Runner.RUTA_DESTINO), pf);
        } catch (IOException e) {
            LOGGER.error("error al leer ruta destino {}", e.getMessage());
        }
        pf.setTipoFoto(Foto.ORIGEN);
        try {
            Files.walkFileTree(Path.of(Runner.RUTA_ORIGEN), pf);
        } catch (IOException e) {
            LOGGER.error("error al leer ruta destino {}", e.getMessage());
        }
        consolidador.consolidar();
        consolidador.procesar();
        consolidador.imprimirCuentas();
        consolidador.identificarCambios();
        consolidador.realizarCambios();
        LOGGER.info("final");
    }
}
