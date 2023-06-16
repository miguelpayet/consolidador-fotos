package pe.net.sdp.foto;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

public class ConfiguradorLog {

    public void configurar() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("log4j2.xml");
        if (inputStream != null) {
            try {
                LogManager.getLogManager().readConfiguration(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.err.println("log4j2.xml file not found in the classpath.");
        }
    }

}
