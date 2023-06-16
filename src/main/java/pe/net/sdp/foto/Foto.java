package pe.net.sdp.foto;

import dev.brachtendorf.jimagehash.hash.Hash;
import dev.brachtendorf.jimagehash.hashAlgorithms.HashingAlgorithm;
import dev.brachtendorf.jimagehash.hashAlgorithms.PerceptiveHash;
import net.jpountz.xxhash.XXHash64;
import net.jpountz.xxhash.XXHashFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pe.net.sdp.foto.fecha.ExtractorFecha;
import pe.net.sdp.foto.fecha.ExtractorFechaFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;


public class Foto {

    public static final int DESTINO = 2;
    public static final LocalDate FECHA_DEFAULT = LocalDate.of(1990, 1, 1);
    private static final HashingAlgorithm HASHER = new PerceptiveHash(32);
    private static final XXHashFactory HASH_FACTORY = XXHashFactory.fastestInstance();
    private static final Logger LOGGER = LogManager.getLogger(Foto.class.getName());
    public static final int ORIGEN = 1;
    private String archivoDestino = null;
    private final String archivoOrigen;
    private LocalDate fechaCreacion = null;
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
        String extension = FilenameUtils.getExtension(archivoOrigen).toUpperCase();
        byte[] fileBytes;
        try {
            fileBytes = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new FotoException(String.format("Excepción al abrir %s", archivoOrigen), e);
        }
        Stream.of(calcularFechaCreacion(fileBytes), calcularImageHash(fileBytes, extension), calcularFileHash(fileBytes))
                .map(Thread::new)
                .peek(Thread::start)
                .forEachOrdered(thread -> {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        LOGGER.error("hilo interrumpido - {}", e.getMessage());
                    }
                });
        if (imageHash == null) {
            if (extension.equals("HEIC")) {
                BigInteger bigIntegerValue = BigInteger.valueOf(fileHash);
                imageHash = new Hash(bigIntegerValue, 32, 123);
            } else {
                throw new FotoException("error al hashear");
            }
        }
    }

    private Runnable calcularFechaCreacion(byte[] fileBytes) {
        return () -> {
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes);
                ExtractorFecha extractor = ExtractorFechaFactory.getExtractor(archivoOrigen, bais);
                if (extractor != null) {
                    fechaCreacion = extractor.extraerFecha();
                }
            } catch (FotoException e) {
                fechaCreacion = FECHA_DEFAULT;
            }
        };
    }

    private Runnable calcularFileHash(byte[] fileBytes) {
        return () -> {
            fileHash = xxHash64.hash(fileBytes, 0, fileBytes.length, 123456789);
        };
    }

    private Runnable calcularImageHash(byte[] fileBytes, String extension) {
        return () -> {
            if (!extension.equals("HEIC")) {
                ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes);
                BufferedImage bufferedImage = null;
                try {
                    bufferedImage = ImageIO.read(bais);
                    imageHash = HASHER.hash(bufferedImage);
                } catch (IOException | IllegalArgumentException e) {
                    imageHash = null;
                }
            }
        };
    }

    public String getArchivoDestino() {
        return archivoDestino;
    }

    public String getArchivoOrigen() {
        return archivoOrigen;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public long getFileHash() {
        return fileHash;
    }

    public Hash getImageHash() {
        return imageHash;
    }

    public String getRutaDestino(String directorioDestino) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        return directorioDestino + "/" + fechaCreacion.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    }

    public long getTamaño() {
        return tamaño;
    }

    public int getTipo() {
        return tipo;
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