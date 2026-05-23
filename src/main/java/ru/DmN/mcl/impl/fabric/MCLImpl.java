package ru.DmN.mcl.impl.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import org.jetbrains.annotations.NotNull;
import ru.DmN.mcl.api.MinecraftCrossLoader;
import ru.DmN.mcl.api.exception.MCLException;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URLClassLoader;

public final class MCLImpl extends MinecraftCrossLoader {
    static void init0() {
        if (MinecraftCrossLoader._INSTANCE != null)
            throw new MCLException("Loader already initialized!");
        MCLImpl instance = new MCLImpl(FabricLauncherBase.getLauncher().getTargetClassLoader());
        MinecraftCrossLoader._INSTANCE = instance;
        instance.init();
    }

    static void launchPreInitInitialization0() {
        ((MCLImpl) MinecraftCrossLoader._INSTANCE).launchPreInitInitialization();
    }

    static void launchInitInitialization0() {
        ((MCLImpl) MinecraftCrossLoader._INSTANCE).launchInitInitialization();

    }

    static void launchPostInitInitialization0() {
        ((MCLImpl) MinecraftCrossLoader._INSTANCE).launchPostInitInitialization();
    }

    private MCLImpl(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public boolean isMinecraftClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public @NotNull File getMinecraftDirectory() {
        if (this.isMinecraftClient()) {
            return FabricLoader.getInstance().getGameDir().toFile();
        } else {
            return new File(".");
        }
    }

    @Override
    public @NotNull File getLoaderSource() {
        return FabricLoader.getInstance().getModContainer("mcl_impl").get().getOrigin().getPaths().get(0).toFile();
    }

    @Override
    protected void expandClassLoaderClassPath(File[] paths) {
        try {
            Class<?> clazzKnotClassLoader = Class.forName("net.fabricmc.loader.impl.launch.knot.KnotClassLoader");
            Field fieldUrlLoader = clazzKnotClassLoader.getDeclaredField("urlLoader");
            fieldUrlLoader.setAccessible(true);
            MinecraftCrossLoader.URLClassLoader$expandClassPath((URLClassLoader) fieldUrlLoader.get(Mod.class.getClassLoader()), paths);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            throw new MCLException(e);
        }
    }
}