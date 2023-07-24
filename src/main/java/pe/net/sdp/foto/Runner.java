package pe.net.sdp.foto;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Runner {

    public static Integer CADA_CUANTOS;
    private static final Logger LOGGER = LogManager.getLogger(Foto.class);
    public static String RUTA_DESTINO;
    public static String RUTA_ORIGEN;

    private static void leerConfiguracion() {
        RUTA_ORIGEN = SinglePropertyLoader.getInstance().getProperty("ruta_input");
        RUTA_DESTINO = SinglePropertyLoader.getInstance().getProperty("ruta_output");
        CADA_CUANTOS = Integer.valueOf(SinglePropertyLoader.getInstance().getProperty("cada_cuantos"));
    }

    public static void main(String[] args) {
        LOGGER.info("inicio");
        leerConfiguracion();
        Consolidador consolidador = new Consolidador(RUTA_ORIGEN, RUTA_DESTINO);
        consolidador.leerArchivos();
        consolidador.consolidar();
        consolidador.procesar();
        LOGGER.info("final");
    }

}
