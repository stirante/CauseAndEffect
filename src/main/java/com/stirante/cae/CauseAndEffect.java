package com.stirante.cae;

import com.stirante.cae.util.JarScanner;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class CauseAndEffect extends JavaPlugin {

    public static Logger LOGGER;

    @Override
    public void onEnable() {
        LOGGER = getLogger();
        JarScanner.registerListeners(this, getFile(), "com.stirante.cae.listener");
    }

}
