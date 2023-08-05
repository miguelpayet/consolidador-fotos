package pe.net.sdp.foto;

import dev.brachtendorf.jimagehash.hash.Hash;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Consolidador {

    private static final Logger LOGGER = LogManager.getLogger(Foto.class.getName());
    private static final double SIMILARITY_THRESHOLD = 0.0834;
    private long filtradas = 0;
    private final Filtro filtroFiltrados;
    private final Filtro filtroThumbnails;
    private final Map<Hash, ArrayList<Foto>> fotosConsolidadas;
    private final Map<Long, Foto> fotosLeidas;
    private final GeneradorRutasUnicas generadorRutas;
    private long iguales = 0;
    private long repetidas = 0;
    private final String rutaDestino;
    private final String rutaOrigen;
    private final List<String> thumbnails;
    private long total = 0;

    public Consolidador(String unaRutaOrigen, String unaRutaDestino) {
        filtroFiltrados = new Filtro(Configuracion.REGEX_FILTRAR);
        filtroThumbnails = new Filtro(Configuracion.REGEX_THUMBNAILS);
        fotosConsolidadas = new HashMap<>();
        fotosLeidas = new HashMap<>();
        generadorRutas = new GeneradorRutasUnicas();
        rutaDestino = unaRutaDestino;
        rutaOrigen = unaRutaOrigen;
        thumbnails = new ArrayList<>();
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

    private boolean esFiltrado(String unFilename) {
        return Arrays.stream(Configuracion.FILTRAR).anyMatch(unFilename::contains) || filtroFiltrados.estaFiltrado(unFilename);
    }

    private boolean esThumbnail(String unFilename) {
        return filtroThumbnails.estaFiltrado(unFilename);
    }

    public String getRutaDestino() {
        return rutaDestino;
    }

    public String getRutaOrigen() {
        return rutaOrigen;
    }

    public void imprimirCuentas() {
        LOGGER.info("files: {}, iguales: {}, repetidas: {}, filtradas: {}, thumbnails: {}",
                total, iguales, repetidas, filtradas, thumbnails.size());
    }

    public void leerFoto(Path filePath, int tipoFoto) {
        String filename = filePath.toString();
        String extension = FilenameUtils.getExtension(filePath.toString());
        if (ExtensionesImagen.esImagen(extension)) {
            if (++total % Configuracion.CADA_CUANTOS == 0) {
                imprimirCuentas();
            }
            if (esFiltrado(filename)) {
                filtradas++;
            } else {
                try {
                    Foto foto = new Foto(filename, tipoFoto);
                    if (esThumbnail(filename) && foto.getFechaCreacion() == Foto.FECHA_DEFAULT)
                        thumbnails.add(filename);
                    else {
                        registrarFoto(foto);
                    }
                } catch (FotoException e) {
                    LOGGER.error("error al procesar {} - {}", filename, e.getMessage());
                }
            }
        }
    }

    private String obtenerRutaDestino(List<Foto> unaListaFotos) {
        List<String> destinos = unaListaFotos.stream()
                .filter(f -> f.getTipo() == Foto.DESTINO)
                .map(f -> FilenameUtils.getFullPath(f.getArchivoDestino()))
                .toList();
        String miRutaDestino;
        if (destinos.size() > 0) {
            miRutaDestino = destinos.get(0);
        } else {
            List<Foto> fotosFechaReal = unaListaFotos.stream()
                    .filter(f -> f.getFechaCreacion() != Foto.FECHA_DEFAULT)
                    .sorted(Comparator.comparing(Foto::getFechaCreacion).reversed())
                    .toList();
            Foto primeraFoto = fotosFechaReal.isEmpty() ? unaListaFotos.get(0) : fotosFechaReal.get(0);
            String rutaFecha = primeraFoto.getFechaCreacion().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String nombreArchivoRaiz = FilenameUtils.getName(primeraFoto.getArchivoOrigen());
            miRutaDestino = String.format("%s/%s/%s", rutaDestino, rutaFecha, nombreArchivoRaiz);
            miRutaDestino = generadorRutas.obtenerNombreDirectorioUnico(miRutaDestino);
        }
        return miRutaDestino;
    }

    public void procesar() {
        LOGGER.info("procesar fotos");
        for (List<Foto> fotos : fotosConsolidadas.values()) {
            if (fotos.size() == 1) {
                procesarDestinoUnico(fotos);
            } else {
                procesarDestinoConjunto(fotos);
            }
        }
        LOGGER.info("procesar thumbnails");
        for (String archivo : thumbnails) {
            procesarThumbnail(archivo);
        }
    }

    private void procesarActualizacion(Foto f) {
        if (f.haCambiado()) {
            ActualizadorFoto actualizador = new ActualizadorFoto(f);
            try {
                actualizador.actualizar();
            } catch (IOException e) {
                LOGGER.error("error al actualizar {} -> {}", f.getArchivoOrigen(), f.getArchivoDestino(), e);
            }
        }
    }

    private void procesarDestinoConjunto(List<Foto> unaListaFotos) {
        String rutaFinal = obtenerRutaDestino(unaListaFotos);
        for (Foto f : unaListaFotos) {
            f.setRutaArchivoDestino(rutaFinal);
            f.setArchivoDestino(generadorRutas.obtenerNombreArchivoUnico(f.getArchivoDestino()));
            procesarActualizacion(f);
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

    private void procesarThumbnail(String unArchivo) {
        String archivoDestino = String.format("%s/%s", Configuracion.RUTA_THUMBNAILS, FilenameUtils.getName(unArchivo));
        ActualizadorArchivo actualizador = new ActualizadorArchivo(unArchivo, generadorRutas.obtenerNombreArchivoUnico(archivoDestino));
        try {
            actualizador.actualizar();
        } catch (IOException e) {
            LOGGER.error("error al actualizar thumbnail {} -> {}", unArchivo, archivoDestino, e);
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
