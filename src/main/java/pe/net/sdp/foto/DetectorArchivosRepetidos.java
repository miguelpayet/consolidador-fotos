package pe.net.sdp.foto;

import java.util.HashMap;

public class DetectorArchivosRepetidos {

    private final HashMap<Long, Long> fileHashes;

    public DetectorArchivosRepetidos() {
        fileHashes = new HashMap<>();
    }

    public boolean esRepetido(Foto unaFoto) {
        Long fileHash = unaFoto.getFileHash();
        boolean repetido = fileHashes.containsKey(fileHash);
        if (!repetido) {
            fileHashes.put(fileHash, fileHash);
        }
        return repetido;
    }
}
