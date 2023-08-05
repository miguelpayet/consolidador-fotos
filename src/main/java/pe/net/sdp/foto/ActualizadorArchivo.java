package pe.net.sdp.foto;

import java.io.File;
import java.io.IOException;

public class ActualizadorArchivo extends Actualizador {

    private final String archivoDestino;
    private final String archivoOrigen;

    public ActualizadorArchivo(String unArchivoOrigen, String unArchivoDestino) {
        archivoOrigen = unArchivoOrigen;
        archivoDestino = unArchivoDestino;
    }

    public void actualizar() throws IOException {
        copiarArchivo();
    }

    private void copiarArchivo() throws IOException {
        File f = new File(archivoDestino);
        if (!f.exists()) {
            copiarArchivo(archivoOrigen, archivoDestino);
        }
    }

}
