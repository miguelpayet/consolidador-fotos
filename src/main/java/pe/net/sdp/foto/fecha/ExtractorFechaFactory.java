package pe.net.sdp.foto.fecha;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pe.net.sdp.foto.FotoException;
import pe.net.sdp.foto.Visitador;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class ExtractorFechaFactory {

    private static final HashMap<String, Class<? extends ExtractorFecha>> CLASES;
    private static final Logger LOGGER = LogManager.getLogger(Visitador.class);

    static {
        CLASES = new HashMap<>(5);
        CLASES.put("HEIC", ExtractorFechaJpg.class);
        CLASES.put("JPEG", ExtractorFechaJpg.class);
        CLASES.put("JPG", ExtractorFechaJpg.class);
        CLASES.put("TIF", ExtractorFechaTIFF.class);
        CLASES.put("TIFF", ExtractorFechaTIFF.class);
    }

    public static ExtractorFecha getExtractor(String unNombreArchivo, ByteArrayInputStream unStream) throws FotoException {
        ExtractorFecha extractor = null;
        String extension = FilenameUtils.getExtension(unNombreArchivo.toUpperCase());
        try {
            Class<? extends ExtractorFecha> clase = CLASES.getOrDefault(extension, null);
            if (clase != null) {
                extractor = clase.getDeclaredConstructor(String.class, ByteArrayInputStream.class).newInstance(unNombreArchivo, unStream);
            } else {
                throw new FotoException("no existe clase extractora");
            }
        } catch (NoSuchMethodException | InstantiationException | InvocationTargetException |
                 IllegalAccessException e) {
            throw new FotoException(String.format("error al instanciar clase extractora - %s", e.getMessage()), e);
        }
        return extractor;
    }

}
