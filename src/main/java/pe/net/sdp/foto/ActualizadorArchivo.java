package pe.net.sdp.foto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
        copiarArchivo(archivoOrigen, archivoDestino);
    }

}
