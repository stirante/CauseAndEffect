package com.stirante.cae.util;

import com.stirante.cae.CauseAndEffect;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JarScanner {

    public static void registerListeners(JavaPlugin plugin, File file, String scannedPackage) {
		//get all classes inside specified package and interate through them
        List<Class<?>> classes = scan(file, scannedPackage);
        for (Class<?> aClass : classes) {
			//class has to implement Listener class
            if (Listener.class.isAssignableFrom(aClass)) {
                try {
					//create instance of listener
                    Listener l = (Listener) aClass.newInstance();//TODO: iterate through constructors and find also JavaPlugin constructor
					//register listener
                    plugin.getServer().getPluginManager().registerEvents(l, plugin);
                    plugin.getLogger().info("Registered " + aClass.getSimpleName());
					//iterate through all methods and if some method parameter is an event then log warning, that it might be EventHandler without annotation
                    Method[] declaredMethods = l.getClass().getDeclaredMethods();
                    for (Method method : declaredMethods) {
                        if (method.getParameterCount() == 1 && Event.class.isAssignableFrom(method.getParameterTypes()[0]) && method.getAnnotation(EventHandler.class) == null) {
                            plugin.getLogger().warning("Method " + method.getName() + " looks like event handler, but is missing EventHandler annotation");
                        }
                    }
                } catch (InstantiationException | IllegalAccessException e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to instantiate class " + aClass.getSimpleName(), e);
                }
            } else {
                if (!aClass.isAnonymousClass() && !aClass.isSynthetic())
                    plugin.getLogger().warning(aClass.toString() + " does not implement Listener");
            }
        }
    }

	//returns list of classes in specified package inside provided jar file
    public static List<Class<?>> scan(File file, String scannedPackage) {
		//prepare package as path
        String s = scannedPackage.replaceAll("\\.", "/");
        List<Class<?>> classes = new ArrayList<>();
        try {
            ZipFile zipFile = new ZipFile(file);
            Enumeration<? extends ZipEntry> it = zipFile.entries();
			//iterate through all files inside zip
            while (it.hasMoreElements()) {
                ZipEntry next = it.nextElement();
				//add class to list if it ends with .class and starts with specified path (which is our scanned package)
                if (next.getName().startsWith(s) && next.getName().endsWith(".class")) {
                    try {
                        classes.add(Class.forName(next.getName().replaceAll("\\.class", "").replaceAll("/", ".")));
                    } catch (ClassNotFoundException e) {
                        CauseAndEffect.LOGGER.log(Level.WARNING, "Couldn't find class " + next.getName(), e);
                    }
                }
            }
        } catch (IOException e) {
            CauseAndEffect.LOGGER.log(Level.SEVERE, "Couldn't access plugin jar", e);
        }
        return classes;
    }

}