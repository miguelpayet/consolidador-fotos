package pe.net.sdp.foto.fecha;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import pe.net.sdp.foto.FotoException;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExtractorFechaJpg extends ExtractorFecha {

    private final static String EXIF_TAG = "Exif SubIFD:Date/Time Original";
    private final static DateTimeFormatter FORMATTER;
    private final static String PATTERN = "yyyy:MM:dd HH:mm:ss";

    static {
        FORMATTER = DateTimeFormatter.ofPattern(PATTERN);
    }

    public ExtractorFechaJpg(String unNombreArchivo, ByteArrayInputStream unStream) {
        super(unNombreArchivo, unStream);
    }

    public LocalDate extraerFecha() throws FotoException {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(getByteStream());
            for (Directory directory : metadata.getDirectories()) {
                String fechaExif = directory.getString(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                if (fechaExif != null) {
                    LocalDateTime fechaLocal = LocalDateTime.parse(fechaExif, FORMATTER);
                    return fechaLocal.toLocalDate();
                }
            }
        } catch (Exception e) {
            throw new FotoException(String.format("error al leer imagen - %s", e.getMessage()));
        }
        return null;
    }

}
