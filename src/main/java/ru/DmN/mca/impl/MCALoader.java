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
import ru.DmN.mca.impl.exception.MCAModLoadException;
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

    private final URLClassLoader classLoader;
    private List<MCAMod> mcaMods = new ArrayList<>();

    private Pair<List<IModInitializer>, List<IModClientInitializer>> mcaModsInitCache;

    protected MCALoader(URLClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public static @NotNull MCALoader getInstance() {
        return INSTANCE;
    }

    public List<MCAMod> getMods() {
        return this.mcaMods;
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
        for (IModInitializer initializer : this.mcaModsInitCache.getLeft()) {
            initializer.preInit();
        }
        if (this.isMinecraftClient()) {
            for (IModClientInitializer initializer : this.mcaModsInitCache.getRight()) {
                initializer.preInitClient();
            }
        }
    }

    protected void launchInitInitialization() {
        for (IModInitializer initializer : this.mcaModsInitCache.getLeft()) {
            initializer.init();
        }
        if (this.isMinecraftClient()) {
            for (IModClientInitializer initializer : this.mcaModsInitCache.getRight()) {
                initializer.initClient();
            }
        }
    }

    protected void launchPostInitInitialization() {
        for (IModInitializer initializer : this.mcaModsInitCache.getLeft()) {
            initializer.postInit();
        }
        if (this.isMinecraftClient()) {
            for (IModClientInitializer initializer : this.mcaModsInitCache.getRight()) {
                initializer.postInitClient();
            }
        }
        this.mcaModsInitCache = null;
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
        this.mcaMods.add(
                new MCAMod(
                        "mca-loader",
                        "1.0.0",
                        "MCA Loader",
                        "Minecraft-Cross-API Loader",
                        new String[]{"DomamaN202"},
                        new MCAMod.Contacts(
                                "https://github.com/Domaman202/MCA",
                                "https://github.com/Domaman202/MCA/tree/api"
                        ),
                        null
                )
        );

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
                String description;
                String[] authors;
                MCAMod.Contacts contacts;
                MCAMod.Dependency[] dependencies;

                if (metadata.has("modid"))
                    modid = metadata.get("modid").getAsString();
                else throw new MCAModLoadException(String.format("Modid for '%s' not founded", metadataURL));

                if (metadata.has("version"))
                    version = metadata.get("version").getAsString();
                else throw new MCAModLoadException(String.format("Version for '%s' not founded", metadataURL));

                if (metadata.has("name"))
                    name = metadata.get("name").getAsString();
                else name = modid;

                if (metadata.has("description"))
                    description = metadata.get("description").getAsString();
                else description = null;

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
                    Set<Map.Entry<String, JsonElement>> dependenciesMap = dependenciesJson.entrySet();
                    dependencies = new MCAMod.Dependency[dependenciesMap.size()];
                    int i = 0;
                    for (Map.Entry<String, JsonElement> entry : dependenciesMap) {
                        dependencies[i] = new MCAMod.Dependency(entry.getKey(), entry.getValue().getAsString());
                        i++;
                    }
                } else dependencies = null;

                LOGGER.info("Successful parsed \"{}\" metadata", modid);

                if (this.mcaMods.stream().anyMatch(it -> it.getModid().equals(modid)))
                    throw new MCAModLoadException(String.format("Modid duplication for '%s'", modid));
                this.mcaMods.add(new MCAMod(modid, version, name, description, authors, contacts, dependencies));
            } catch (IOException e) {
                LOGGER.error("Error on loading \"{}\" mod", metadataURL);
                throw new MCAModLoadException(e);
            }
        }

        for (MCAMod mod : this.mcaMods) {
            if (mod.getDependencies() == null)
                continue;
            for (MCAMod.Dependency dependency : mod.getDependencies()) {
                Optional<MCAMod> find = this.mcaMods.stream().filter(it -> it.getModid().equals(dependency.getModid())).findFirst();
                if (!find.isPresent())
                    throw new MCAModLoadException(String.format("Missing dependency: mod '%s' requires '%s', but it is not found", mod.getModid(), dependency.getModid()));
                if (!Semver.satisfies(find.get().getVersion(), dependency.getVersion()))
                    throw new MCAModLoadException(String.format("Dependency version mismatch: mod '%s' requires '%s' version %s, but found version %s", mod.getModid(), dependency.getModid(), dependency.getVersion(), find.get().getVersion()));
            }
        }

        this.mcaModsInitCache = Pair.of(
                commonEntries.stream().map((clazz) -> {
                    try {
                        return (IModInitializer) Class.forName(clazz, false, this.classLoader).newInstance();
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                        throw new MCAModInitException(e);
                    }
                }).collect(Collectors.toList()),
                clientEntries.stream().map((clazz) -> {
                    try {
                        return (IModClientInitializer) Class.forName(clazz, false, this.classLoader).newInstance();
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

    private static class Semver implements Comparable<Semver> {
        final int major;
        final int minor;
        final int patch;
        final String prerelease; // может быть null

        private Semver(int major, int minor, int patch, String prerelease) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.prerelease = prerelease;
        }

        public static Semver parse(String version) {
            String[] parts = version.split("-", 2);
            String core = parts[0];
            String prerelease = parts.length > 1 ? parts[1] : null;

            String[] numbers = core.split("\\.");
            int major = numbers.length > 0 ? Integer.parseInt(numbers[0]) : 0;
            int minor = numbers.length > 1 ? Integer.parseInt(numbers[1]) : 0;
            int patch = numbers.length > 2 ? Integer.parseInt(numbers[2]) : 0;

            return new Semver(major, minor, patch, prerelease);
        }

        public static boolean satisfies(String version, String requirement) {
            String operator = "=";
            String targetVersionStr = requirement.trim();

            if (targetVersionStr.startsWith(">=")) {
                operator = ">=";
                targetVersionStr = targetVersionStr.substring(2).trim();
            } else if (targetVersionStr.startsWith("<=")) {
                operator = "<=";
                targetVersionStr = targetVersionStr.substring(2).trim();
            } else if (targetVersionStr.startsWith(">")) {
                operator = ">";
                targetVersionStr = targetVersionStr.substring(1).trim();
            } else if (targetVersionStr.startsWith("<")) {
                operator = "<";
                targetVersionStr = targetVersionStr.substring(1).trim();
            } else if (targetVersionStr.startsWith("~")) {
                operator = "~";
                targetVersionStr = targetVersionStr.substring(1).trim();
            } else if (targetVersionStr.startsWith("^")) {
                operator = "^";
                targetVersionStr = targetVersionStr.substring(1).trim();
            }

            Semver targetVer = Semver.parse(targetVersionStr);
            Semver actualVer = Semver.parse(version);

            switch (operator) {
                case "=":
                    return actualVer.compareTo(targetVer) == 0;
                case ">=":
                    return actualVer.compareTo(targetVer) >= 0;
                case "<=":
                    return actualVer.compareTo(targetVer) <= 0;
                case ">":
                    return actualVer.compareTo(targetVer) > 0;
                case "<":
                    return actualVer.compareTo(targetVer) < 0;
                case "~":
                    Semver upper = new Semver(targetVer.major, targetVer.minor + 1, 0, null);
                    return actualVer.compareTo(targetVer) >= 0 && actualVer.compareTo(upper) < 0;
                case "^":
                    Semver upperCaret = new Semver(targetVer.major + 1, 0, 0, null);
                    return actualVer.compareTo(targetVer) >= 0 && actualVer.compareTo(upperCaret) < 0;
                default:
                    return false;
            }
        }


        @Override
        public int compareTo(Semver other) {
            int cmp = Integer.compare(this.major, other.major);
            if (cmp != 0) return cmp;
            cmp = Integer.compare(this.minor, other.minor);
            if (cmp != 0) return cmp;
            cmp = Integer.compare(this.patch, other.patch);
            if (cmp != 0) return cmp;

            if (this.prerelease == null && other.prerelease == null) return 0;
            if (this.prerelease == null) return 1;
            if (other.prerelease == null) return -1;
            return this.prerelease.compareTo(other.prerelease);
        }
    }
}
