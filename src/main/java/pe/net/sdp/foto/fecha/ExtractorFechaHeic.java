package pe.net.sdp.foto.fecha;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import pe.net.sdp.foto.FotoException;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static pe.net.sdp.foto.Foto.FECHA_DEFAULT;

public class ExtractorFechaHeic extends ExtractorFecha {

    private final static String EXIF_TAG = "Exif SubIFD:Date/Time Original";
    private final static DateTimeFormatter FORMATTER;
    private static final Parser PARSER = new AutoDetectParser();
    private final static String PATTERN = "yyyy:MM:dd HH:mm:ss";

    static {
        FORMATTER = DateTimeFormatter.ofPattern(PATTERN);
    }

    public ExtractorFechaHeic(String unNombreArchivo, ByteArrayInputStream unStream) {
        super(unNombreArchivo, unStream);
    }

    public LocalDate extraerFecha() throws FotoException {
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();
        LocalDate fecha = null;
        try {
            BodyContentHandler handler = new BodyContentHandler();
            PARSER.parse(getByteStream(), handler, metadata, context);
            String fechaExif = metadata.get(EXIF_TAG);
            if (fechaExif == null || fechaExif.equals("")) {
                fecha = FECHA_DEFAULT;
            } else {
                LocalDateTime localDateTime = LocalDateTime.parse(fechaExif, FORMATTER);
                fecha = localDateTime.toLocalDate();
            }
        } catch (Exception e) {
            throw new FotoException(String.format("error al leer imagen - %s", e.getMessage()));
        }
        return fecha;
    }

}
