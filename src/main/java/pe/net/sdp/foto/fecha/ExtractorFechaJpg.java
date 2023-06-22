package pe.net.sdp.foto.fecha;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;
import pe.net.sdp.foto.FotoException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ExtractorFechaJpg extends ExtractorFecha {

    private final static String EXIF_TAG = "Exif SubIFD:Date/Time Original";
    private final static DateTimeFormatter FORMATTER;
    private static final Parser PARSER = new AutoDetectParser();
    private final static String PATTERN = "yyyy:MM:dd HH:mm:ss";

    static {
        FORMATTER = DateTimeFormatter.ofPattern(PATTERN);
    }

    public ExtractorFechaJpg(String unNombreArchivo, ByteArrayInputStream unStream) {
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
            if (fechaExif == null) {
                throw new FotoException("fechaExif es nulo");
            }
            if (fechaExif.equals("")) {
                throw new FotoException("fechaExif en blanco");
            }
            LocalDateTime localDateTime = LocalDateTime.parse(fechaExif, FORMATTER);
            fecha = localDateTime.toLocalDate();
        } catch (Exception e) {
            throw new FotoException(String.format("error al leer imagen - %s", e.getMessage()));
        }
        return fecha;
    }

}
