package pe.net.sdp.foto;

import java.util.HashSet;

public final class ExtensionesImagen {

    public static final HashSet<String> VALID_EXTENSIONS;

    static {
        VALID_EXTENSIONS = new HashSet<>();
        VALID_EXTENSIONS.add("NEF");
        VALID_EXTENSIONS.add("JPG");
        VALID_EXTENSIONS.add("JPEG");
        VALID_EXTENSIONS.add("TIF");
        VALID_EXTENSIONS.add("TIFF");
        VALID_EXTENSIONS.add("HEIC");
    }

    public static boolean esImagen(String extension) {
        return VALID_EXTENSIONS.contains(extension.toUpperCase());
    }

}
