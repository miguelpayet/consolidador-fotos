package pe.net.sdp.foto;

import dev.brachtendorf.jimagehash.hash.Hash;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Consolidador {

    private static final Logger LOGGER = LogManager.getLogger(Foto.class.getName());
    private static final double THRESHOLD = 0.15;
    private final List<Foto> cambios;
    private long distintas = 0;
    private final Map<Hash, List<Foto>> fotos;
    private long iguales = 0;
    private long repetidas = 0;
    private final String rutaDestino;
    private long total = 0;

    public Consolidador(String rutaDestino) {
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
        LOGGER.info(String.format("cantidad de cambios: %d", cambios.size()));
    }

    public void imprimirCuentas() {
        LOGGER.info(String.format("files: %d, únicos: %d, iguales: %d, repetidas: %d", total, distintas, iguales, repetidas));
    }

    public void leerFoto(Path filePath, int tipoFoto) {
        total++;
        if (total % 100 == 0) {
            imprimirCuentas();
        }
        try {
            Foto foto = new Foto(filePath.toString(), tipoFoto);
            registrarFoto(foto);
        } catch (FotoException e) {
            LOGGER.error(String.format("error al procesar %s - %s", filePath, e.getMessage()));
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

    public void procesarUnMultiple(List<Foto> listaFotos) {
        listaFotos.forEach(this::procesarUnUnico);
        Directorio directorio = new Directorio(listaFotos, this);
        directorio.aplicarRutaPrincipal();
    }

    public void procesarUnUnico(Foto foto) {
        foto.setArchivoDestino(foto.getRutaDestino(rutaDestino) + "/" + FilenameUtils.getName(foto.getArchivoOrigen()));
    }

    public void realizarCambios() {
        for (Foto foto : cambios) {
            try {
                String directorio = new File(foto.getArchivoDestino()).getParent();
                File dir = new File(directorio);
                if (!dir.exists()) {
                    FileUtils.forceMkdir(new File(directorio));
                }
                if (foto.getTipo() == Foto.ORIGEN) {
                    Files.copy(Path.of(foto.getArchivoOrigen()), Path.of(foto.getArchivoDestino()), StandardCopyOption.COPY_ATTRIBUTES);
                } else {
                    Path movido = Files.move(Path.of(foto.getArchivoOrigen()), Path.of(foto.getArchivoDestino()));
                }
            } catch (IOException e) {
                LOGGER.info("error al realizar cambio");
                e.printStackTrace(System.out);
            }
        }
    }

    public void registrarFoto(Foto foto) {
        boolean agregado = false;
        boolean existeFoto = false;
        for (Map.Entry<Hash, List<Foto>> entry : fotos.entrySet()) {
            Hash imageHash = entry.getKey();
            List<Foto> fotoList = entry.getValue();
            existeFoto = fotoList.stream().anyMatch(laFoto -> laFoto.getFileHash() == foto.getFileHash());
            if (!existeFoto) {
                double similarity = imageHash.normalizedHammingDistance(foto.getImageHash());
                if (similarity < Consolidador.THRESHOLD) {
                    fotoList.add(foto);
                    agregado = true;
                    iguales++;
                    break;
                }
            } else {
                repetidas++;
                break;
            }
        }
        if (!agregado && !existeFoto) {
            ArrayList<Foto> fotoList = new ArrayList<>(1);
            fotoList.add(foto);
            fotos.put(foto.getImageHash(), fotoList);
            distintas++;
        }
    }
}
