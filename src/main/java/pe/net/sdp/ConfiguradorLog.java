package pe.net.sdp;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

public class ConfiguradorLog {

    public void configurar() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("logging.properties");
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
            System.err.println("logging.properties file not found in the classpath.");
        }
    }

}
