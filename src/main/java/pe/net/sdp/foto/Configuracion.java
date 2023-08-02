package pe.net.sdp.foto;

public class Configuracion {

    public static Integer CADA_CUANTOS;
    public static String[] FILTRAR;
    public static String REGEX_FILTRAR;
    public static String[] REGEX_THUMBNAILS;
    public static String RUTA_DESTINO;
    public static String RUTA_ORIGEN;
    public static String RUTA_THUMBNAILS;

    static void leerConfiguracion() {
        String palabrasFiltradas = SinglePropertyLoader.getInstance().getProperty("filtrar");
        CADA_CUANTOS = Integer.valueOf(SinglePropertyLoader.getInstance().getProperty("cada_cuantos"));
        FILTRAR = palabrasFiltradas.split(",");
        String regexThumbnails = SinglePropertyLoader.getInstance().getProperty("regex_thumbnails");
        REGEX_THUMBNAILS = regexThumbnails.split(",");
        REGEX_FILTRAR = SinglePropertyLoader.getInstance().getProperty("regex_filtrar");
        RUTA_DESTINO = SinglePropertyLoader.getInstance().getProperty("ruta_output");
        RUTA_ORIGEN = SinglePropertyLoader.getInstance().getProperty("ruta_input");
        RUTA_THUMBNAILS = SinglePropertyLoader.getInstance().getProperty("ruta_thumbnails");
    }

}
