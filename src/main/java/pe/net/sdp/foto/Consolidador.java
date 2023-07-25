package pe.net.sdp.foto;

import dev.brachtendorf.jimagehash.hash.Hash;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Consolidador {

    private static final Logger LOGGER = LogManager.getLogger(Foto.class.getName());
    private static final double SIMILARITY_THRESHOLD = 0.0834;
    private long filtradas = 0;
    private final Map<Hash, ArrayList<Foto>> fotosConsolidadas;
    private final Map<Long, Foto> fotosLeidas;
    private final GeneradorRutasUnicas generadorRutas;
    private long iguales = 0;
    private long repetidas = 0;
    private final String rutaDestino;
    private final String rutaOrigen;
    private long total = 0;

    public Consolidador(String unaRutaOrigen, String unaRutaDestino) {
        fotosConsolidadas = new HashMap<>();
        fotosLeidas = new HashMap<>();
        rutaDestino = unaRutaDestino;
        rutaOrigen = unaRutaOrigen;
        generadorRutas = new GeneradorRutasUnicas();
    }

    public void consolidar() {
        ArrayList<Foto> listaFotos = new ArrayList<>(fotosLeidas.values());
        Iterator<Foto> iterator = listaFotos.iterator();
        while (iterator.hasNext()) {
            Foto foto = iterator.next();
            ArrayList<Foto> fotos = new ArrayList<>(List.of(foto));
            fotosConsolidadas.put(foto.getImageHash(), fotos);
            iterator.remove();
            Hash hashLaFoto = foto.getImageHash();
            ArrayList<Foto> fotosCopiadas = new ArrayList<>(1);
            for (Foto fotoCandidata : listaFotos) {
                double similarityScore = 1;
                if (hashLaFoto != null && fotoCandidata.getImageHash() != null) {
                    similarityScore = hashLaFoto.normalizedHammingDistance(fotoCandidata.getImageHash());
                }
                LOGGER.debug("{} vs {} - {}", foto.getArchivoOrigen(), fotoCandidata.getArchivoOrigen(), similarityScore);
                if (similarityScore < SIMILARITY_THRESHOLD) {
                    iguales++;
                    fotos.add(fotoCandidata);
                    fotosCopiadas.add(fotoCandidata);
                }
            }
            listaFotos.removeAll(fotosCopiadas);
            iterator = listaFotos.iterator();
        }
        imprimirCuentas();
    }

    private boolean contienePalabraFiltrada(String unFilename) {
        return Arrays.stream(Runner.FILTRAR).anyMatch(unFilename::contains);
    }

    public String getRutaDestino() {
        return rutaDestino;
    }

    public void imprimirCuentas() {
        LOGGER.info("files: {}, iguales: {}, repetidas: {}, filtradas: {}", total, iguales, repetidas, filtradas);
    }

    public void leerArchivos() {
        Visitador pf = new Visitador(this);
        pf.setTipoFoto(Foto.DESTINO);
        try {
            Files.walkFileTree(Path.of(rutaDestino), pf);
        } catch (IOException e) {
            LOGGER.error("error al leer ruta destino {}", e.getMessage());
        }
        pf.setTipoFoto(Foto.ORIGEN);
        try {
            Files.walkFileTree(Path.of(rutaOrigen), pf);
        } catch (IOException e) {
            LOGGER.error("error al leer ruta destino {}", e.getMessage());
        }
    }

    public void leerFoto(Path filePath, int tipoFoto) {
        String filename = filePath.toString();
        String extension = FilenameUtils.getExtension(filePath.toString());
        if (ExtensionesImagen.esImagen(extension)) {
            total++;
            if (total % Runner.CADA_CUANTOS == 0) {
                imprimirCuentas();
            }
            if (!contienePalabraFiltrada(filename)) {
                try {
                    Foto foto = new Foto(filename, tipoFoto);
                    registrarFoto(foto);
                } catch (FotoException e) {
                    LOGGER.error("error al procesar {} - {}", filename, e.getMessage());
                }
            } else {
                filtradas++;
            }
        }
    }

    public void procesar() {
        for (List<Foto> fotos : fotosConsolidadas.values()) {
            if (fotos.size() == 1) {
                procesarDestinoUnico(fotos);
            } else {
                procesarDestinoConjunto(fotos);
            }
        }
    }

    private void procesarActualizacion(Foto f) {
        if (f.haCambiado()) {
            Actualizador actualizador = new Actualizador(f);
            try {
                actualizador.actualizar();
            } catch (IOException e) {
                LOGGER.error("error al actualizar {} -> {}", f.getArchivoOrigen(), f.getArchivoDestino(), e);
            }
        }
    }

    private void procesarDestinoConjunto(List<Foto> listaFotos) {
        List<Foto> fechas = listaFotos.stream()
                .filter(f -> f.getFechaCreacion() != Foto.FECHA_DEFAULT)
                .sorted(Comparator.comparing(Foto::getFechaCreacion))
                .toList();
        Iterator<Foto> iterator;
        if (fechas.size() > 0) {
            iterator = fechas.iterator();
        } else {
            iterator = listaFotos.iterator();
        }
        if (iterator.hasNext()) {
            Foto foto = iterator.next();
            String nombreRuta = FilenameUtils.getName(foto.getArchivoOrigen());
            String rutaFecha = foto.getFechaCreacion().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String rutaFinal = String.format("%s/%s/%s", rutaDestino, rutaFecha, nombreRuta);
            rutaFinal = generadorRutas.obtenerNombreDirectorioUnico(rutaFinal);
            for (Foto f : listaFotos) {
                f.setRutaArchivoDestino(rutaFinal);
                f.setArchivoDestino(generadorRutas.obtenerNombreArchivoUnico(f.getArchivoDestino()));
                procesarActualizacion(f);
            }
        }
    }

    private void procesarDestinoUnico(List<Foto> listaFotos) {
        Iterator<Foto> iterator = listaFotos.iterator();
        if (iterator.hasNext()) {
            Foto f = iterator.next();
            f.setRutaFechaArchivoDestino(getRutaDestino());
            f.setArchivoDestino(generadorRutas.obtenerNombreArchivoUnico(f.getArchivoDestino()));
            procesarActualizacion(f);
        }
    }

    public void registrarFoto(Foto unaFoto) {
        if (fotosLeidas.containsKey(unaFoto.getFileHash())) {
            repetidas++;
        } else {
            fotosLeidas.put(unaFoto.getFileHash(), unaFoto);
        }
    }

}
