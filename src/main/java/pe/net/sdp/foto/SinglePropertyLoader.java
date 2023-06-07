package pe.net.sdp.foto;

import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class SinglePropertyLoader {

    private static SinglePropertyLoader instance;
    private FileBasedConfiguration configuration;

    private SinglePropertyLoader() {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                        .configure(params.properties().setFileName("consolidador.properties"));
        try {
            configuration = builder.getConfiguration();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static synchronized SinglePropertyLoader getInstance() {
        if (instance == null) {
            instance = new SinglePropertyLoader();
        }
        return instance;
    }

    public String getProperty(String key) {
        return (String) configuration.getProperty(key);
    }
}