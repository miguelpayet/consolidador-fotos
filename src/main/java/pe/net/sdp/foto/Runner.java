package pe.net.sdp.foto;

import pe.net.sdp.ConfiguradorLog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class Runner {

    private static final Logger LOGGER = Logger.getLogger(Foto.class.getName());
    private static String RUTA_ORIGEN;
    private static String RUTA_DESTINO;

    private static void leerConfiguracion() {
        ConfiguradorLog configuradorLog = new ConfiguradorLog();
        configuradorLog.configurar();
        RUTA_ORIGEN = SinglePropertyLoader.getInstance().getProperty("ruta_input");
        RUTA_DESTINO = SinglePropertyLoader.getInstance().getProperty("ruta_output");
    }

    public static void main(String[] args) {
        LOGGER.info("inicio");
        leerConfiguracion();
        Consolidador consolidador = new Consolidador(RUTA_ORIGEN, RUTA_DESTINO);
        Visitador pf = new Visitador(consolidador);
        pf.setTipoFoto(Foto.ORIGEN);
        try {
            Files.walkFileTree(Path.of(Runner.RUTA_ORIGEN), pf);
        } catch (IOException e) {
            LOGGER.severe("error al leer ruta origen");
        }
        pf.setTipoFoto(Foto.DESTINO);
        try {
            Files.walkFileTree(Path.of(Runner.RUTA_DESTINO), pf);
        } catch (IOException e) {
            LOGGER.severe("error al leer ruta destino");
        }
        LOGGER.info("final");
    }
}
