package pe.net.sdp.foto;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Runner {

    private static final Logger LOGGER = LogManager.getLogger(Foto.class);

    public static void main(String[] args) {
        LOGGER.info("inicio");
        Configuracion.leerConfiguracion();
        Consolidador consolidador = new Consolidador(Configuracion.RUTA_ORIGEN, Configuracion.RUTA_DESTINO);
        consolidador.leerArchivos();
        consolidador.consolidar();
        consolidador.procesar();
        LOGGER.info("fin");
    }

}
