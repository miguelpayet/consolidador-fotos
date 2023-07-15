package pe.net.sdp.foto;

import dev.brachtendorf.jimagehash.hash.Hash;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Consolidador {

    private static final Logger LOGGER = LogManager.getLogger(Foto.class.getName());
    private List<Foto> cambios;
    private final DetectorArchivosRepetidos detectorRepetidos;
    private long iguales = 0;
    private final ArrayList<Foto> listafotos;
    private Map<Hash, ArrayList<Foto>> mapaFotos;
    private long repetidas = 0;
    private final String rutaDestino;
    private long total = 0;

    public Consolidador(String unaRutaDestino) {
        cambios = new ArrayList<>();
        detectorRepetidos = new DetectorArchivosRepetidos();
        listafotos = new ArrayList<>();
        mapaFotos = new HashMap<>();
        rutaDestino = unaRutaDestino;
    }

    public void consolidar() {
        Iterator<Foto> iterator = listafotos.iterator();
        while (iterator.hasNext()) {
            Foto laFoto = iterator.next();
            ArrayList<Foto> lasFotos = new ArrayList<>(1);
            mapaFotos.put(laFoto.getImageHash(), lasFotos);
            iterator.remove();
            Hash hashLaFoto = laFoto.getImageHash();
            ArrayList<Foto> fotosCopiadas = new ArrayList<>(1);
            for (Foto fotoCandidata : listafotos) {
                double similarityScore = hashLaFoto.normalizedHammingDistance(fotoCandidata.getImageHash());
                if (similarityScore < 0.083) {
                    iguales++;
                    lasFotos.add(fotoCandidata);
                    fotosCopiadas.add(fotoCandidata);
                }
            }
            listafotos.removeAll(fotosCopiadas);
            iterator = listafotos.iterator();
        }
    }

    public String getRutaDestino() {
        return rutaDestino;
    }

    public void identificarCambios() {
        cambios = mapaFotos.values()
                .stream()
                .flatMap(List::stream)
                .filter(f -> !f.getArchivoOrigen().equals(f.getArchivoDestino())).toList();
        LOGGER.info("cantidad de cambios: {}", cambios.size());
    }

    public void imprimirCuentas() {
        LOGGER.info("files: {}, iguales: {}, repetidas: {}", total, iguales, repetidas);
    }

    public void leerFoto(Path filePath, int tipoFoto) {
        String filename = filePath.toString();
        String extension = FilenameUtils.getExtension(filePath.toString());
        if (ExtensionesImagen.esImagen(extension)) {
            total++;
            if (total % 2 == 0) {
                imprimirCuentas();
            }
            try {
                Foto foto = new Foto(filename, tipoFoto);
                registrarFoto(foto);
            } catch (FotoException e) {
                LOGGER.error("error al procesar {} - {}", filename, e.getMessage());
            }
        }
    }

    public void listarCambios() {
        for (Foto foto : cambios) {
            if (foto.getTipo() == Foto.ORIGEN) {
                LOGGER.info(String.format("copiar %s -> %s", foto.getArchivoOrigen(), foto.getArchivoDestino()));
            } else {
                LOGGER.info(String.format("mover %s -> %s", foto.getArchivoOrigen(), foto.getArchivoDestino()));
            }
        }
    }

    public void listarFechas() {
        List<LocalDate> listaFechas = cambios.stream().map(Foto::getFechaCreacion).distinct().sorted().toList();
        for (LocalDate fecha : listaFechas) {
            LOGGER.info(fecha.toString());
        }
    }

    public void procesar() {
        for (List<Foto> listaFotos : mapaFotos.values()) {
            if (listaFotos.size() == 1) {
                procesarDestinoUnico(listaFotos);
            } else {
                procesarDestinoConjunto(listaFotos);
            }
        }
    }

    private void procesarDestinoConjunto(List<Foto> listaFotos) {
        String nombreRuta;
        List<Foto> listaElegida;
        List<Foto> fechas = listaFotos.stream().filter(f -> f.getFechaCreacion() != Foto.FECHA_DEFAULT).sorted(Comparator.comparing(Foto::getFechaCreacion)).toList();
        if (fechas.size() > 0) {
            listaElegida = fechas;
        } else {
            listaElegida = listaFotos;
        }
        nombreRuta = FilenameUtils.getName(listaElegida.get(0).getArchivoOrigen());
        String rutaFecha = listaElegida.get(0).getFechaCreacion().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        listaFotos.forEach(f -> f.setRutaArchivoDestino(String.format("%s/%s/%s", rutaDestino, rutaFecha, nombreRuta)));
    }

    private void procesarDestinoUnico(List<Foto> foto) {
        foto.forEach(f -> f.setRutaFechaArchivoDestino(getRutaDestino()));
    }

    public void realizarCambios() {
        long total = 0;
        for (Foto foto : cambios) {
            if (++total % 100 == 0) {
                LOGGER.info(String.format("operaciones realizadas: %d", total));
            }
            try {
                Actualizador actualizador = new Actualizador(foto);
                actualizador.actualizar();
            } catch (IOException e) {
                LOGGER.error("error al realizar cambio - {}", foto.getArchivoOrigen(), e);
            }
        }
    }

    public void registrarFoto(Foto unaFoto) {
        if (detectorRepetidos.esRepetido(unaFoto)) {
            repetidas++;
        } else {
            listafotos.add(unaFoto);
        }
    }
}
