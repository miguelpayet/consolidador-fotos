package pe.net.sdp.foto;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import dev.brachtendorf.jimagehash.hash.Hash;
import dev.brachtendorf.jimagehash.hashAlgorithms.HashingAlgorithm;
import dev.brachtendorf.jimagehash.hashAlgorithms.PerceptiveHash;
import net.jpountz.xxhash.XXHash64;
import net.jpountz.xxhash.XXHashFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Foto {

    public static final int DESTINO = 2;
    private static final Date FECHA_DEFAULT = new Date(0, 0, 1);
    private static final HashingAlgorithm HASHER = new PerceptiveHash(32);
    private static final XXHashFactory HASH_FACTORY = XXHashFactory.fastestInstance();
    private static final Logger LOGGER = LogManager.getLogger(Foto.class.getName());
    public static final int ORIGEN = 1;
    private String archivoDestino = null;
    private final String archivoOrigen;
    private Date fechaCreacion = null;
    private long fileHash = 0;
    private Hash imageHash = null;
    private long tamaño = 0;
    private final int tipo;
    private final XXHash64 xxHash64 = HASH_FACTORY.hash64();

    public Foto(String archivo, int tipo) throws FotoException {
        this.archivoOrigen = archivo;
        this.tipo = tipo;
        reset();
    }

    private void calcularDatosFoto() throws FotoException {
        File file = new File(archivoOrigen);
        byte[] fileBytes;
        try {
            fileBytes = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new FotoException(String.format("Excepción al abrir %s", archivoOrigen), e);
        }
        final ExecutorService executor = Executors.newFixedThreadPool(3);
        executor.execute(() -> {
            try {
                String extension = FilenameUtils.getExtension(archivoOrigen).toUpperCase();
                leerFechaCreacion(fileBytes, extension);
            } catch (ImageProcessingException | IOException | ParseException | FotoException e) {
                LOGGER.error(String.format("error al leer metadata %s - %s", archivoOrigen, e.getMessage()));
                fechaCreacion = FECHA_DEFAULT;
            }
        });
        executor.execute(() -> {
            ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes);
            BufferedImage bufferedImage = null;
            try {
                bufferedImage = ImageIO.read(bais);
                imageHash = HASHER.hash(bufferedImage);
            } catch (IOException | IllegalArgumentException e) {
                imageHash = null;
            }
        });
        executor.execute(() -> {
            fileHash = xxHash64.hash(fileBytes, 0, fileBytes.length, 123456789);
        });
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            throw new FotoException("Execution interrupted", e);
        }
        if (imageHash == null) {
            throw new FotoException(String.format("error al hashear: %s", archivoOrigen));
        }
    }

    public String getArchivoDestino() {
        return archivoDestino;
    }

    public String getArchivoOrigen() {
        return archivoOrigen;
    }

    public long getFileHash() {
        return fileHash;
    }

    public Hash getImageHash() {
        return imageHash;
    }

    public String getRutaDestino(String directorioDestino) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        return directorioDestino + "/" + dateFormat.format(this.fechaCreacion);
    }

    public long getTamaño() {
        return tamaño;
    }

    public int getTipo() {
        return tipo;
    }

    private Directory leerDirectorio(Metadata metadata, String extension) {
        Class<? extends Directory> clase;
        if (extension.equals("JPG") || extension.equals("JPEG")) {
            clase = JpegDirectory.class;
        } else {
            clase = ExifSubIFDDirectory.class;
        }
        return metadata.getFirstDirectoryOfType(clase);
    }

    public void leerFechaCreacion(byte[] fileBytes, String extension) throws ImageProcessingException, IOException, ParseException, FotoException {
        ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes);
        Metadata metadata = ImageMetadataReader.readMetadata(bais);
        Directory directory = leerDirectorio(metadata, extension);
        if (directory != null) {
            Date fecha = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            fechaCreacion = fecha != null ? dateFormat.parse(dateFormat.format(fecha)) : FECHA_DEFAULT;
        } else {
            for (Directory testdir : metadata.getDirectories()) {
                LOGGER.info(testdir.getClass().getName());
                for (Tag tag : testdir.getTags()) {
                    LOGGER.info("{} - {} = {}", testdir.getName(), tag.getTagName(), tag.getDescription());
                }
                if (testdir.hasErrors()) {
                    for (String error : testdir.getErrors()) {
                        System.err.format("ERROR: %s", error);
                    }
                }
            }
            throw new FotoException("directorio de metadata nulo");
        }
    }

    public void reset() throws FotoException {
        setTamaño();
        calcularDatosFoto();
    }

    public void setArchivoDestino(String archivoDestino) {
        this.archivoDestino = archivoDestino;
    }

    private void setTamaño() {
        try {
            tamaño = Files.size(Path.of(archivoOrigen));
        } catch (IOException e) {
            tamaño = 0;
        }
    }
}