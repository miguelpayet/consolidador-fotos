package pe.net.sdp.foto.fecha;

import pe.net.sdp.foto.FotoException;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;

public abstract class ExtractorFecha {

    private final String archivo;
    private final ByteArrayInputStream byteStream;

    public ExtractorFecha(String unArchivo, ByteArrayInputStream unStream) {
        archivo = unArchivo;
        byteStream = unStream;
    }

    public abstract LocalDate extraerFecha() throws FotoException;

    public String getArchivo() {
        return archivo;
    }

    protected ByteArrayInputStream getByteStream() {
        return byteStream;
    }

}
