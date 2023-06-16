package pe.net.sdp.foto.fecha;

import org.apache.logging.log4j.LogManager;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.image.TiffParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;
import pe.net.sdp.foto.FotoException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import static pe.net.sdp.foto.Foto.FECHA_DEFAULT;

public class ExtractorFechaTIFF extends ExtractorFecha {

    private static final HashMap<String, String> DESIRED_TAGS;
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(ExtractorFechaTIFF.class);

    static {
        DESIRED_TAGS = new HashMap<>(3);
        DESIRED_TAGS.put("Digital Date Created", "yyyy:MM:dd");
        DESIRED_TAGS.put("Date Created", "yyyy:MM:dd");
        DESIRED_TAGS.put("exif:DateTimeOriginal", "yyyy-MM-dd'T'HH:mm:ss");
        DESIRED_TAGS.put("xmpMM:History:When", "yyyy-MM-dd'T'HH:mm:ss'Z'");
    }

    public ExtractorFechaTIFF(String unNombreArchivo, ByteArrayInputStream unStream) {
        super(unNombreArchivo, unStream);
    }

    public LocalDate extraerFecha() throws FotoException {
        LocalDate fecha = null;
        try {
            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();
            Parser parser = new TiffParser();
            context.set(Parser.class, parser);
            parser.parse(getByteStream(), handler, metadata, context);
            String tagFecha = DESIRED_TAGS.keySet().stream()
                    .filter(tag -> metadata.get(tag) != null)
                    .findFirst()
                    .orElse(null);
            if (tagFecha == null) {
                throw new FotoException("error al parsear fecha tiff");
            } else {
                String fechaExif = metadata.get(tagFecha);
                String pattern = DESIRED_TAGS.get(tagFecha);
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(pattern);
                try {
                    LocalDateTime localDateTime = LocalDateTime.parse(fechaExif, dateFormat);
                    fecha = localDateTime.toLocalDate();
                } catch (DateTimeException e) {
                    fecha = FECHA_DEFAULT;
                }
            }
        } catch (IOException | SAXException | TikaException e) {
            LOGGER.error("error al leer tiff - {}", e.getMessage());
        }
        return fecha;
    }

}
