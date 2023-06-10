package pe.net.sdp.foto;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.*;

public class Directorio {
    private static final List<String> extensiones = List.of(".NEF", ".TIFF", ".HEIC", ".JPG", ".JPEG");
    private final Consolidador consolidador;
    private Foto foto_principal;
    private final List<Foto> fotos;
    private String ruta_final;

    public Directorio(List<Foto> arreglo_fotos, Consolidador consolidador) {
        this.fotos = arreglo_fotos;
        this.consolidador = consolidador;
        this.ruta_final = null;
        this.foto_principal = null;
    }

    public void aplicarRutaPrincipal() {
        foto_principal = encontrarFotoPrincipal();
        if (foto_principal != null) {
            ruta_final = calcularRutaFinal(foto_principal);
            for (Foto foto : fotos) {
                if (foto != foto_principal && !new File(foto.getArchivoOrigen()).getParent().equals(ruta_final)) {
                    foto.setArchivoDestino(ruta_final + "/" + new File(foto.getArchivoOrigen()).getName());
                }
            }
        }
    }

    public String calcularRutaFinal(Foto foto) {
        String file_base_name = FilenameUtils.getName(foto.getArchivoOrigen());
        return foto.getRutaDestino(consolidador.getRutaDestino()) + "/" + file_base_name + ".DIR";
    }

    public Foto encontrarFotoPrincipal() {
        for (String extension : extensiones) {
            List<Foto> fotos = new ArrayList<>();
            for (Foto foto : this.fotos) {
                if (new File(foto.getArchivoOrigen()).getName().toUpperCase().endsWith(extension.toUpperCase())) {
                    fotos.add(foto);
                }
            }
            if (!fotos.isEmpty()) {
                fotos.sort(Comparator.comparingLong(Foto::getTamaño).reversed());
                return fotos.get(0);
            }
        }
        return null;
    }
}
