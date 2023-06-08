package pe.net.sdp.foto;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import dev.brachtendorf.jimagehash.hash.Hash;
import dev.brachtendorf.jimagehash.hashAlgorithms.HashingAlgorithm;
import dev.brachtendorf.jimagehash.hashAlgorithms.PerceptiveHash;
import net.jpountz.xxhash.XXHash64;
import net.jpountz.xxhash.XXHashFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class Foto {

    private static final XXHashFactory HASH_FACTORY = XXHashFactory.fastestInstance();
    public static final int DESTINO = 2;
    public static final int ORIGEN = 1;
    private static final Logger LOGGER = Logger.getLogger(Foto.class.getName());
    private static final Date FECHA_DEFAULT = new Date(0, 0, 1);
    private static final HashingAlgorithm HASHER = new PerceptiveHash(32);
    private final String archivoOrigen;
    private final int tipo;
    private String archivoDestino;
    private Date fechaCreacion;
    private long fileHash;
    private Hash imageHash;
    private long tamaño;

    public Foto(String archivo, int tipo) throws Exception {
        this.archivoOrigen = archivo;
        this.tipo = tipo;
        this.archivoDestino = null;
        this.fechaCreacion = null;
        this.fileHash = 0;
        this.imageHash = null;
        this.tamaño = 0;
        reset();
    }

    private void calcularFileHash() {
        try {
            byte[] fileData = Files.readAllBytes(Paths.get(archivoOrigen));
            XXHash64 xxHash64 = HASH_FACTORY.hash64();
            fileHash = xxHash64.hash(fileData, 0, fileData.length, 123456789);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void calcularImageHash() throws Exception {
        imageHash = HASHER.hash(new File(archivoOrigen));
    }

    public String getArchivoDestino() {
        return archivoDestino;
    }

    public void setArchivoDestino(String archivoDestino) {
        this.archivoDestino = archivoDestino;
    }

    public String getArchivoOrigen() {
        return archivoOrigen;
    }

    public void getFechaCreacion() {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(new File(archivoOrigen));
            ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (directory != null) {
                Date fecha = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                fechaCreacion = fecha != null ? dateFormat.parse(dateFormat.format(fecha)) : FECHA_DEFAULT;
            } else {
                fechaCreacion = FECHA_DEFAULT;
            }
        } catch (ImageProcessingException | IOException | ParseException e) {
            fechaCreacion = FECHA_DEFAULT;
        }
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

    public void reset() throws Exception {
        try {
            tamaño = Files.size(Path.of(archivoOrigen));
        } catch (IOException e) {
            tamaño = 0;
        }
        calcularFileHash();
        calcularImageHash();
        getFechaCreacion();
    }

}