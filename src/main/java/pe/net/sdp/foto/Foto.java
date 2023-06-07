package pe.net.sdp.foto;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import dev.brachtendorf.jimagehash.hash.Hash;
import dev.brachtendorf.jimagehash.hashAlgorithms.HashingAlgorithm;
import dev.brachtendorf.jimagehash.hashAlgorithms.PerceptiveHash;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class Foto {

    public static final int DESTINO = 2;
    public static final int ORIGEN = 1;
    private static final Logger LOGGER = Logger.getLogger(Foto.class.getName());
    private static final Date FECHA_DEFAULT = new Date(0, 0, 1);
    private static final HashingAlgorithm HASHER = new PerceptiveHash(32);
    private final String archivoOrigen;
    private final int tipo;
    private String archivoDestino;
    private Date fechaCreacion;
    private String fileHash;
    private Hash imageHash;
    private long tamaño;

    public Foto(String archivo, int tipo) throws Exception {
        this.archivoOrigen = archivo;
        this.tipo = tipo;
        this.archivoDestino = null;
        this.fechaCreacion = null;
        this.fileHash = null;
        this.imageHash = null;
        this.tamaño = 0;
        reset();
    }

    private void calcularFileHash() {
        try {
            MessageDigest md = MessageDigest.getInstance("XXHASH64");
            try (FileInputStream fis = new FileInputStream(archivoOrigen)) {
                byte[] dataBytes = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(dataBytes)) != -1) {
                    md.update(dataBytes, 0, bytesRead);
                }
            }
            byte[] mdBytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte mdByte : mdBytes) {
                sb.append(Integer.toString((mdByte & 0xff) + 0x100, 16).substring(1));
            }
            fileHash = sb.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            fileHash = null;
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

    public String getFileHash() {
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