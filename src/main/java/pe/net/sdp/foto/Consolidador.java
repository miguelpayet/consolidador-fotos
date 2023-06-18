package pe.net.sdp.foto;

import dev.brachtendorf.jimagehash.hash.Hash;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Consolidador {

    private static final Logger LOGGER = LogManager.getLogger(Foto.class.getName());
    private List<Foto> cambios;
    private final Map<Long, Foto> fotos;
    private final ArrayList<ArrayList<Foto>> fotosLimpias;
    private long iguales = 0;
    private long repetidas = 0;
    private final String rutaDestino;
    private long total = 0;

    public Consolidador(String rutaDestino) {
        this.cambios = new ArrayList<>();
        this.fotos = new HashMap<>();
        this.fotosLimpias = new ArrayList<>();
        this.rutaDestino = rutaDestino;
    }

    public void consolidar() {
        Iterator<Map.Entry<Long, Foto>> iteratorExterno = fotos.entrySet().iterator();
        while (iteratorExterno.hasNext()) {
            Map.Entry<Long, Foto> entryExterno = iteratorExterno.next();
            Foto fotoExterna = entryExterno.getValue();
            if (fotoExterna != null) {
                Hash entryExternoHash = entryExterno.getValue().getImageHash();
                ArrayList<Foto> listaFoto = new ArrayList<>(1);
                listaFoto.add(entryExterno.getValue());
                fotosLimpias.add(listaFoto);
                iteratorExterno.remove();
                for (Map.Entry<Long, Foto> entryInterno : fotos.entrySet()) {
                    Foto fotoInterna = entryInterno.getValue();
                    if (fotoInterna != null) {
                        double similarityScore = entryExternoHash.normalizedHammingDistance(fotoInterna.getImageHash());
                        if (similarityScore < 0.111) {
                            iguales++;
                            listaFoto.add(fotoInterna);
                            entryInterno.setValue(null);
                        }
                    }
                }
            } else {
                iteratorExterno.remove();
            }
        }
    }

    public String getRutaDestino() {
        return rutaDestino;
    }

    public void identificarCambios() {
        cambios = fotosLimpias.stream().flatMap(List::stream).filter(f -> !f.getArchivoOrigen().equals(f.getArchivoDestino())).toList();
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
            if (total % 100 == 0) {
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
        listarFechas();
    }

    public void listarFechas() {
        List<LocalDate> listaFechas = cambios.stream().map(Foto::getFechaCreacion).distinct().sorted().toList();
        for (LocalDate fecha : listaFechas) {
            LOGGER.info(fecha.toString());
        }
    }

    public void procesar() {
        for (List<Foto> listaFotos : fotosLimpias) {
            if (listaFotos.size() == 1) {
                procesarDestinoUnico(listaFotos);
            } else {
                procesarDestinoConjunto(listaFotos);
            }
        }
    }

    private void procesarDestinoConjunto(List<Foto> listaFotos) {
        LocalDate fechaRuta;
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
        for (Foto foto : cambios) {
            try {
                String directorio = new File(foto.getArchivoDestino()).getParent();
                File dir = new File(directorio);
                if (!dir.exists()) {
                    FileUtils.forceMkdir(new File(directorio));
                }
                Actualizador actualizador = new Actualizador(foto);
                actualizador.actualizar();
            } catch (IOException e) {
                LOGGER.info("error al realizar cambio - {}", foto.getArchivoOrigen());
                e.printStackTrace(System.out);
            }
        }
    }

    public void registrarFoto(Foto foto) {
        if (!fotos.containsKey(foto.getFileHash())) {
            fotos.put(foto.getFileHash(), foto);
        } else {
            repetidas++;
        }
    }
}
