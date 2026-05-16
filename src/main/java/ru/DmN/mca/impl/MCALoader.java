package ru.DmN.mca.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import ru.DmN.mca.api.IModClientInitializer;
import ru.DmN.mca.api.IModInitializer;
import ru.DmN.mca.impl.exception.MCALoaderException;
import ru.DmN.mca.impl.exception.MCAMetadataReadException;
import ru.DmN.mca.impl.exception.MCAModInitException;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

public abstract class MCALoader {
    protected static MCALoader INSTANCE; // Инициализируется в реализации
    public final Logger LOGGER = LogManager.getLogger(MCALoader.class);
    private final List<MCAMod> MCA_MODS = new ArrayList<>();
    private Pair<List<IModInitializer>, List<IModClientInitializer>> MCA_MODS_INIT_CACHE;

    public static @NotNull MCALoader getInstance() {
        return INSTANCE;
    }

    public List<MCAMod> getMods() {
        return MCA_MODS;
    }

    protected void init(URLClassLoader loader) {
        try {
            this.initModsDirectoryAndAddToLoader(loader);
            this.initModsList(loader);
        } catch (Exception e) {
            throw new MCALoaderException(e);
        }
    }

    protected void launchPreInitInitialization() {
        for (IModInitializer initializer : MCA_MODS_INIT_CACHE.getLeft()) {
            initializer.preInit();
        }
        if (this.isMinecraftClient()) {
            for (IModClientInitializer initializer : MCA_MODS_INIT_CACHE.getRight()) {
                initializer.preInitClient();
            }
        }
    }

    protected void launchInitInitialization() {
        for (IModInitializer initializer : MCA_MODS_INIT_CACHE.getLeft()) {
            initializer.init();
        }
        if (this.isMinecraftClient()) {
            for (IModClientInitializer initializer : MCA_MODS_INIT_CACHE.getRight()) {
                initializer.initClient();
            }
        }
    }

    protected void launchPostInitInitialization() {
        for (IModInitializer initializer : MCA_MODS_INIT_CACHE.getLeft()) {
            initializer.postInit();
        }
        if (this.isMinecraftClient()) {
            for (IModClientInitializer initializer : MCA_MODS_INIT_CACHE.getRight()) {
                initializer.postInitClient();
            }
        }
        MCA_MODS_INIT_CACHE = null;
    }

    private void initModsDirectoryAndAddToLoader(URLClassLoader loader) {
        File modsDir = new File(this.getMinecraftDirectory(), "mods-mca");
        if (!modsDir.exists())
            modsDir.mkdir();
        File[] modFiles = modsDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (modFiles != null) {
            expandClassLoaderURLs(loader, modFiles);
        }
    }

    private void initModsList(URLClassLoader loader) throws IOException {
        List<String> commonEntries = new ArrayList<>();
        List<String> clientEntries = new ArrayList<>();

        Gson gson = new Gson();
        Enumeration<URL> metadataURLs = loader.findResources("mca.mod.json");
        while (metadataURLs.hasMoreElements()) {
            URL metadataURL = metadataURLs.nextElement();
            try {
                JsonObject metadata = gson.fromJson(new InputStreamReader(metadataURL.openStream()), JsonObject.class);

                String modid;
                String version;
                String name;
                String[] authors;
                MCAMod.Contacts contacts;
                MCAMod.Dependency[] dependencies;

                if (metadata.has("modid"))
                    modid = metadata.get("modid").getAsString();
                else throw new MCAMetadataReadException("Modid for \"" + metadataURL + "\" not founded");

                if (metadata.has("version"))
                    version = metadata.get("version").getAsString();
                else throw new MCAMetadataReadException("Version for \"" + metadataURL + "\" not founded");

                if (metadata.has("name"))
                    name = metadata.get("name").getAsString();
                else throw new MCAMetadataReadException("Name for \"" + metadataURL + "\" not founded");

                if (metadata.has("authors")) {
                    JsonArray authorsJson = metadata.get("authors").getAsJsonArray();
                    authors = new String[authorsJson.size()];
                    for (int i = 0; i < authorsJson.size(); i++) {
                        authors[i] = authorsJson.get(i).getAsString();
                    }
                } else authors = null;

                if (metadata.has("contact")) {
                    JsonObject contactJson = metadata.get("contact").getAsJsonObject();
                    contacts = new MCAMod.Contacts(
                            contactJson.has("homepage") ? contactJson.get("homepage").getAsString() : null,
                            contactJson.has("sources") ? contactJson.get("sources").getAsString() : null
                    );
                } else contacts = null;

                if (metadata.has("entry")) {
                    JsonObject entryJson = metadata.get("entry").getAsJsonObject();

                    if (entryJson.has("common")) {
                        JsonArray commonsJson = entryJson.get("common").getAsJsonArray();
                        for (int i = 0; i < commonsJson.size(); i++) {
                            commonEntries.add(commonsJson.get(i).getAsString());
                        }
                    }

                    if (entryJson.has("client")) {
                        JsonArray clientsJson = entryJson.get("client").getAsJsonArray();
                        for (int i = 0; i < clientsJson.size(); i++) {
                            clientEntries.add(clientsJson.get(i).getAsString());
                        }
                    }
                }

                if (metadata.has("dependencies")) {
                    JsonObject dependenciesJson = metadata.get("dependencies").getAsJsonObject();
                    Map<String, JsonElement> dependenciesMap = dependenciesJson.asMap();
                    dependencies = new MCAMod.Dependency[dependenciesMap.size()];
                    int i = 0;
                    for (Map.Entry<String, JsonElement> entry : dependenciesMap.entrySet()) {
                        dependencies[i] = new MCAMod.Dependency(entry.getKey(), entry.getValue().getAsString());
                        i++;
                    }
                } else dependencies = null;

                LOGGER.info("Parsed \"{}\" mod metadata", modid);
                MCA_MODS.add(new MCAMod(modid, version, name, authors, contacts, dependencies));
            } catch (IOException e) {
                LOGGER.error("Error on loading \"{}\" mod", metadataURL);
                throw new MCALoaderException(e);
            }
        }

        // todo: check dependencies

        MCA_MODS_INIT_CACHE = Pair.of(
                commonEntries.stream().map((clazz) -> {
                    try {
                        return (IModInitializer) Class.forName(clazz).newInstance();
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                        throw new MCAModInitException(e);
                    }
                }).collect(Collectors.toList()),
                clientEntries.stream().map((clazz) -> {
                    try {
                        return (IModClientInitializer) Class.forName(clazz).newInstance();
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                        throw new MCAModInitException(e);
                    }
                }).collect(Collectors.toList())
        );
    }

    public abstract boolean isMinecraftClient();
    public abstract @NotNull File getMinecraftDirectory();

    private static void expandClassLoaderURLs(URLClassLoader loader, File[] urls) {
        try {
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            for (File url : urls) {
                method.invoke(loader, url.toURI().toURL());
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | MalformedURLException e) {
            throw new MCALoaderException(e);
        }
    }
}
