package pe.net.sdp.foto;

import pe.net.sdp.ConfiguradorLog;

import java.util.logging.Logger;

public class Runner {

    private static final Logger logger = Logger.getLogger(Foto.class.getName());
    private static String rutaOrigen;
    private static String rutaDestino;

    private static void leerConfiguracion() {
        ConfiguradorLog configuradorLog = new ConfiguradorLog();
        configuradorLog.configurar();
        rutaOrigen = SinglePropertyLoader.getInstance().getProperty("ruta_input");
        rutaDestino = SinglePropertyLoader.getInstance().getProperty("ruta_output");
    }

    public static void main(String[] args) {
        logger.info("inicio");
        leerConfiguracion();
        Consolidador consolidador = new Consolidador(rutaOrigen, rutaDestino);
        consolidador.leer();
        logger.info("final");
    }
}
