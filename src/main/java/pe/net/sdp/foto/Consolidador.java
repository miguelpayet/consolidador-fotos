package pe.net.sdp.foto;

import dev.brachtendorf.jimagehash.hash.Hash;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class Consolidador {
    private static final int THREADS = 8;
    private static final double THRESHOLD = 0.15;
    private static final Logger logger = Logger.getLogger(Foto.class.getName());
    private final List<Foto> cambios;
    private final Map<Hash, List<Foto>> fotos;
    private final String rutaOrigen;
    private String rutaDestino;
    private long total;
    private long distintas;
    private long iguales;

    public Consolidador(String rutaOrigen, String rutaDestino) {
        this.rutaOrigen = rutaOrigen;
        this.rutaDestino = rutaDestino;
        this.cambios = new ArrayList<>();
        this.fotos = new HashMap<>();
    }

    public String getRutaDestino() {
        return rutaDestino;
    }

    public void identificarCambios() {
        for (List<Foto> fotos : this.fotos.values()) {
            for (Foto foto : fotos) {
                if (foto.getArchivoDestino() != null && !foto.getArchivoOrigen().equals(foto.getArchivoDestino())) {
                    cambios.add(foto);
                }
            }
        }
    }

    public void imprimirCambio(String filename) {
        String base = new File(filename).getParent();
        System.out.println(base + " " + filename);
    }

    private void imprimirCuentas() {
        total++;
        if (total % 1 == 0) {
            logger.info(String.format("files: %d, ùnicos: %d, repetidos: %d", total, distintas, iguales));
        }
    }

    public void leer() {
        total = 0;
        leerDirectorio(rutaOrigen, Foto.ORIGEN);
        leerDirectorio(rutaDestino, Foto.DESTINO);
    }

    public void leerDirectorio(String filename, int tipoFoto) {
        try (Stream<Path> stream = Files.walk(Paths.get(filename))) {
            stream.filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            this.leerFoto(path.toString(), tipoFoto);
                        } catch (Exception e) {
                            System.err.println("Error processing file: " + path);
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void leerFoto(String filename, int tipo_foto) {
        try {
            imprimirCuentas();
            Foto foto = new Foto(filename, tipo_foto);
            registrarFoto(foto);
        } catch (Exception e) {
            logger.warning("error al calcular " + filename);
            logger.warning(e.getMessage());
        }
    }

    public void procesar() {
        for (List<Foto> fotos : this.fotos.values()) {
            if (fotos.size() == 1) {
                procesarUnUnico(fotos.get(0));
            } else {
                procesarUnMultiple(fotos);
            }
        }
    }

    public void procesarUnMultiple(List<Foto> arreglo_fotos) {
        arreglo_fotos.forEach(this::procesarUnUnico);
        Directorio directorio = new Directorio(arreglo_fotos, this);
        directorio.aplicarRutaPrincipal();
        directorio.encontrarDuplicados();
    }

    public void procesarUnUnico(Foto foto) {
        foto.setArchivoDestino(foto.getRutaDestino(rutaDestino) + "/" + FilenameUtils.getName(foto.getArchivoOrigen()));
    }

    public void realizarCambios() throws IOException {
        for (Foto foto : cambios) {
            String directorio = new File(foto.getArchivoDestino()).getParent();
            File dir = new File(directorio);
            if (!dir.exists()) {
                FileUtils.forceMkdir(new File(directorio));
            }
            if (foto.getTipo() == Foto.ORIGEN) {
                Files.copy(Path.of(foto.getArchivoOrigen()), Path.of(foto.getArchivoDestino()), StandardCopyOption.COPY_ATTRIBUTES);
            } else {
                Files.move(Path.of(foto.getArchivoOrigen()), Path.of(foto.getArchivoDestino()));
            }
        }
    }

    public void registrarFoto(Foto foto) {
        boolean agregado = false;
        for (Map.Entry<Hash, List<Foto>> entry : fotos.entrySet()) {
            Hash imageHash = entry.getKey();
            List<Foto> fotoList = entry.getValue();
            double similarity = imageHash.normalizedHammingDistance(foto.getImageHash());
            if (similarity < Consolidador.THRESHOLD) {
                fotoList.add(foto);
                agregado = true;
                iguales++;
                break;
            }
        }
        if (!agregado) {
            ArrayList<Foto> fotoList = new ArrayList<>(1);
            fotoList.add(foto);
            fotos.put(foto.getImageHash(), fotoList);
            distintas++;
        }
    }
}
