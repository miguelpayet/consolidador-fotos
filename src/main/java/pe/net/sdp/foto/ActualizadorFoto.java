package pe.net.sdp.foto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ActualizadorFoto extends Actualizador {

    private final Foto foto;

    public ActualizadorFoto(Foto unaFoto) {
        foto = unaFoto;
    }

    public void actualizar() throws IOException {
        if (foto.getTipo() == Foto.ORIGEN) {
            copiarArchivo();
        } else {
            moverArchivo();
        }
    }

    private void copiarArchivo() throws IOException {
        copiarArchivo(foto.getArchivoOrigen(), foto.getArchivoDestino());
    }

    private void crearDirectorio() throws IOException {
        crearDirectorio(foto.getArchivoDestino());
    }

    private void moverArchivo() throws IOException {
        crearDirectorio();
        Files.move(Path.of(foto.getArchivoOrigen()), Path.of(foto.getArchivoDestino()));
    }


}
