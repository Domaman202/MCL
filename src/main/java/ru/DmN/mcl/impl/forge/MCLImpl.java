package ru.DmN.mcl.impl.forge;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModClassLoader;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import ru.DmN.mcl.api.MinecraftCrossLoader;
import ru.DmN.mcl.api.exception.MCLException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URLClassLoader;

public final class MCLImpl extends MinecraftCrossLoader {
    static void init0() {
        if (MinecraftCrossLoader._INSTANCE != null)
            throw new MCLException("Loader already initialized!");
        MCLImpl instance = new MCLImpl(Loader.instance().getModClassLoader());
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
        return FMLCommonHandler.instance().getSide().isClient();
    }

    @Override
    public @NotNull File getMinecraftDirectory() {
        if (this.isMinecraftClient()) {
            return Minecraft.getMinecraft().mcDataDir;
        } else {
            return new File(".");
        }
    }

    @Override
    public @NotNull File getLoaderSource() {
        return Loader.instance().getActiveModList().stream().filter(it -> it.getModId().equals("mcl_impl")).findFirst().get().getSource();
    }

    @Override
    protected void expandClassLoaderClassPath(File[] paths) {
        try {
            ModClassLoader loader = (ModClassLoader) Loader.instance().getModClassLoader();
            for (File path : paths)
                loader.addFile(path);
            URLClassLoader$expandClassPath(loader, paths);
        } catch (MalformedURLException e) {
            throw new MCLException(e);
        }
    }
}
