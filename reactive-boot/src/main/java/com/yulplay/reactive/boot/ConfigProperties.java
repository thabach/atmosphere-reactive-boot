package com.yulplay.reactive.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.yulplay.reactive.boot.ReactiveBootstrap.YULPLAY_CONFIG_PATH;

public class ConfigProperties {

    public final static String INSTALL_PATH = "${install.path}";

    private final Logger logger = LoggerFactory.getLogger(ConfigProperties.class);
    private final Properties prop = new Properties();

    public ConfigProperties() {
        InputStream input = null;
        String filename = "config.properties";
        try {
            input = getClass().getClassLoader().getResourceAsStream(filename);

            if (input == null) {
                logger.info("ConfigProperties.path {}", System.getProperty(YULPLAY_CONFIG_PATH, "config.properties"));
                input = new FileInputStream(System.getProperty(YULPLAY_CONFIG_PATH) + File.separator + "config.properties");
            }

            prop.load(input);
            logger.info("Properties file loaded {}", prop);

            prop.values().stream().map(m -> ((String) m).contains(INSTALL_PATH));


        } catch (IOException ex) {
            logger.warn("Unable to local {}", filename, ex);
            logger.trace("Unable to local {}", filename, ex);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public Properties getProperties() {
        return prop;
    }

    public Boolean getBoolean(String key) {
        return Boolean.valueOf(prop.getProperty(key, "false"));
    }

    public String getString(String key) {
        String b = prop.getProperty(key);
        if (b != null && b.contains(INSTALL_PATH)) {
            b = b.replace(INSTALL_PATH, System.getProperty(ReactiveBootstrap.YULPLAY_CONFIG_PATH));
            prop.put(key, b);
        }
        return b;
    }

    public int getInt(String key) {
        return Integer.valueOf(prop.getProperty(key));
    }

    public Boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.valueOf(prop.getProperty(key, String.valueOf(defaultValue)));
    }

    public String getString(String key, String defaultValue) {
        String b = prop.getProperty(key);
        if (b == null) {
            b = defaultValue;
        }

        if (b.contains(INSTALL_PATH)) {
            b = b.replace(INSTALL_PATH, System.getProperty(ReactiveBootstrap.YULPLAY_CONFIG_PATH));
            prop.put(key, b);
        }
        return b;
    }

    public int getInt(String key, int defaultValue) {
        return Integer.valueOf(prop.getProperty(key, String.valueOf(defaultValue)));
    }

}
