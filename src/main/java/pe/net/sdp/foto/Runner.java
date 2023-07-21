package pe.net.sdp.foto;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Runner {

    private static final Logger LOGGER = LogManager.getLogger(Foto.class);
    public static String rutaDestino;
    public static String rutaOrigen;

    private static void leerConfiguracion() {
        rutaOrigen = SinglePropertyLoader.getInstance().getProperty("ruta_input");
        rutaDestino = SinglePropertyLoader.getInstance().getProperty("ruta_output");
    }

    public static void main(String[] args) {
        LOGGER.info("inicio");
        leerConfiguracion();
        Consolidador consolidador = new Consolidador(rutaOrigen, rutaDestino);
        consolidador.leerArchivos();
        consolidador.consolidar();
        consolidador.procesar();
        LOGGER.info("final");
    }

}
