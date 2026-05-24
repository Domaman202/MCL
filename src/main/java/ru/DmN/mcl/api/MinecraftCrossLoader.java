package ru.DmN.mcl.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import ru.DmN.mcl.api.initializer.IModClientInitializer;
import ru.DmN.mcl.api.initializer.IModInitializer;
import ru.DmN.mcl.api.exception.MCLException;
import ru.DmN.mcl.impl.MCLSystemMod;
import ru.DmN.mcl.impl.exception.MCLModLoadException;
import ru.DmN.mcl.impl.exception.MCLModInitException;
import sun.misc.Unsafe;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public abstract class MinecraftCrossLoader {
    public final Logger LOGGER = LogManager.getLogger(MinecraftCrossLoader.class);

    protected final ClassLoader classLoader;
    protected List<MCLMod> mods = new ArrayList<>();

    protected static MinecraftCrossLoader _INSTANCE; // Инициализируется в реализации
    private Pair<List<IModInitializer>, List<IModClientInitializer>> _modsInitCache;

    protected MinecraftCrossLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public static @NotNull MinecraftCrossLoader getInstance() {
        return _INSTANCE;
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    public List<MCLMod> getMods() {
        return this.mods;
    }

    public boolean isModLoaded(String modid, String version) {
        Optional<MCLMod> mod = this.mods.stream().filter(it -> it.getModId().equals(modid)).findFirst();
        return mod.isPresent() && Semver.satisfies(mod.get().getVersion(), version);
    }

    protected void init() {
        try {
            this.initModsDirectoryAndAddToLoader();
            this.initModsList();
        } catch (Exception e) {
            throw new MCLException(e);
        }
    }

    protected void launchPreInitInitialization() {
        for (IModInitializer initializer : this._modsInitCache.getLeft()) {
            initializer.preInit();
        }
        if (this.isMinecraftClient()) {
            for (IModClientInitializer initializer : this._modsInitCache.getRight()) {
                initializer.preInitClient();
            }
        }
    }

    protected void launchInitInitialization() {
        for (IModInitializer initializer : this._modsInitCache.getLeft()) {
            initializer.init();
        }
        if (this.isMinecraftClient()) {
            for (IModClientInitializer initializer : this._modsInitCache.getRight()) {
                initializer.initClient();
            }
        }
    }

    protected void launchPostInitInitialization() {
        for (IModInitializer initializer : this._modsInitCache.getLeft()) {
            initializer.postInit();
        }
        if (this.isMinecraftClient()) {
            for (IModClientInitializer initializer : this._modsInitCache.getRight()) {
                initializer.postInitClient();
            }
        }
        this._modsInitCache = null;
    }

    private void initModsDirectoryAndAddToLoader() {
        File modsDir = new File(this.getMinecraftDirectory(), "mods-mcl");
        if (!modsDir.exists())
            modsDir.mkdir();
        File[] modFiles = modsDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (modFiles != null) {
            this.expandClassLoaderClassPath(modFiles);
        }
    }

    private void initModsList() {
        this.mods.add(new MCLSystemMod.MCL());
        this.mods.add(new MCLSystemMod.MCA());

        List<String> commonEntries = new ArrayList<>();
        List<String> clientEntries = new ArrayList<>();

        Gson gson = new Gson();
        Enumeration<URL> metadataURLs = ClassLoader$findResources(this.classLoader, "mcl.mod.json");
        while (metadataURLs.hasMoreElements()) {
            URL metadataURL = metadataURLs.nextElement();
            try {
                JsonObject metadata = gson.fromJson(new InputStreamReader(metadataURL.openStream()), JsonObject.class);

                File source;
                String modid;
                String version;
                String name;
                String description;
                String logo;
                String[] authors;
                MCLMod.Contacts contacts;
                MCLMod.Dependency[] dependencies;

                source = extractModDirectory(metadataURL).toFile();

                if (metadata.has("modid"))
                    modid = metadata.get("modid").getAsString();
                else throw new MCLModLoadException(String.format("Modid for '%s' not founded", metadataURL));

                if (metadata.has("version"))
                    version = metadata.get("version").getAsString();
                else throw new MCLModLoadException(String.format("Version for '%s' not founded", metadataURL));

                if (metadata.has("name"))
                    name = metadata.get("name").getAsString();
                else name = modid;

                if (metadata.has("description"))
                    description = metadata.get("description").getAsString();
                else description = null;

                if (metadata.has("logo"))
                    logo = metadata.get("logo").getAsString();
                else logo = null;

                if (metadata.has("authors")) {
                    JsonArray authorsJson = metadata.get("authors").getAsJsonArray();
                    authors = new String[authorsJson.size()];
                    for (int i = 0; i < authorsJson.size(); i++) {
                        authors[i] = authorsJson.get(i).getAsString();
                    }
                } else authors = null;

                if (metadata.has("contact")) {
                    JsonObject contactJson = metadata.get("contact").getAsJsonObject();
                    contacts = new MCLMod.Contacts.Default(
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
                    dependencies = new MCLMod.Dependency[dependenciesMap.size()];
                    int i = 0;
                    for (Map.Entry<String, JsonElement> entry : dependenciesMap) {
                        dependencies[i] = new MCLMod.Dependency.Default(
                                entry.getKey(),
                                entry.getValue().getAsString()
                        );
                        i++;
                    }
                } else dependencies = null;

                LOGGER.info("Successful parsed \"{}\" metadata", modid);

                if (this.mods.stream().anyMatch(it -> it.getModId().equals(modid)))
                    throw new MCLModLoadException(String.format("Modid duplication for '%s'", modid));
                this.mods.add(new MCLMod.Default(source, modid, version, name, description, logo, authors, contacts, dependencies));
            } catch (IOException e) {
                LOGGER.error("Error on loading \"{}\" mod", metadataURL);
                throw new MCLModLoadException(e);
            }
        }

        for (MCLMod mod : this.mods) {
            if (mod.getDependencies() == null)
                continue;
            for (MCLMod.Dependency dependency : mod.getDependencies()) {
                Optional<MCLMod> find = this.mods.stream().filter(it -> it.getModId().equals(dependency.getModId())).findFirst();
                if (!find.isPresent())
                    throw new MCLModLoadException(String.format("Missing dependency: mod '%s' requires '%s', but it is not found", mod.getModId(), dependency.getModId()));
                if (!Semver.satisfies(find.get().getVersion(), dependency.getVersion()))
                    throw new MCLModLoadException(String.format("Dependency version mismatch: mod '%s' requires '%s' version %s, but found version %s", mod.getModId(), dependency.getModId(), dependency.getVersion(), find.get().getVersion()));
            }
        }

        this._modsInitCache = Pair.of(
                commonEntries.stream().map((clazz) -> {
                    try {
                        return (IModInitializer) Class.forName(clazz, false, this.classLoader).newInstance();
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                        throw new MCLModInitException(e);
                    }
                }).collect(Collectors.toList()),
                clientEntries.stream().map((clazz) -> {
                    try {
                        return (IModClientInitializer) Class.forName(clazz, false, this.classLoader).newInstance();
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                        throw new MCLModInitException(e);
                    }
                }).collect(Collectors.toList())
        );
    }

    public abstract boolean isMinecraftClient();
    public abstract @NotNull File getMinecraftDirectory();
    public abstract @NotNull File getLoaderSource();
    protected abstract void expandClassLoaderClassPath(File[] paths);

    private static Path extractModDirectory(URL modJsonURL) {
        try {
            URI uri = modJsonURL.toURI();

            if ("jar".equals(uri.getScheme())) {
                String schemeSpecificPart = uri.getSchemeSpecificPart();
                int separator = schemeSpecificPart.indexOf("!/");
                if (separator == -1) {
                    throw new IllegalArgumentException("Invalid jar URL: missing '!/'");
                }

                String jarUrlPart = schemeSpecificPart.substring(0, separator);
                URI jarUri = new URI(jarUrlPart);
                return new File(jarUri).toPath();
            }

            if ("file".equals(uri.getScheme())) {
                Path resourcePath = new File(uri).toPath();
                return resourcePath.getParent();
            }

            throw new IllegalArgumentException("Unsupported URL scheme: " + uri.getScheme());
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract mod directory from URL: " + modJsonURL, e);
        }
    }

    protected static Enumeration<URL> ClassLoader$findResources(ClassLoader loader, String name) {
        try {
            Method method;
            try {
                method = loader.getClass().getDeclaredMethod("findResources", String.class);
            } catch (NoSuchMethodException ignored) {
                method = URLClassLoader.class.getDeclaredMethod("findResources", String.class);
            }
            try {
                method.setAccessible(true);
            } catch (Exception ignored) {
                Field fieldTheUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
                fieldTheUnsafe.setAccessible(true);
                ((Unsafe) fieldTheUnsafe.get(null)).putBoolean(method, 12, true);
            }
            return (Enumeration<URL>) method.invoke(loader, name);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | NoSuchFieldException e) {
            throw new MCLException(e);
        }
    }

    protected static void URLClassLoader$expandClassPath(URLClassLoader loader, File[] urls) {
        try {
            Method method;
            try {
                method = loader.getClass().getDeclaredMethod("addURL", URL.class);
            } catch (NoSuchMethodException ignored) {
                method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            }
            try {
                method.setAccessible(true);
            } catch (Exception ignored) {
                Field fieldTheUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
                fieldTheUnsafe.setAccessible(true);
                ((Unsafe) fieldTheUnsafe.get(null)).putBoolean(method, 12, true);
            }
            for (File url : urls) {
                method.invoke(loader, url.toURI().toURL());
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | MalformedURLException | NoSuchFieldException e) {
            throw new MCLException(e);
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
