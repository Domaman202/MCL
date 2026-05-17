package ru.DmN.mca.impl.forge;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import ru.DmN.mca.impl.MCALoader;
import ru.DmN.mca.impl.exception.MCALoaderException;

import java.io.File;
import java.net.URLClassLoader;

public final class MCALoaderImpl extends MCALoader {
    static void init0() {
        if (MCALoader.INSTANCE != null)
            throw new MCALoaderException("Loader already initialized!");
        MCALoaderImpl instance = new MCALoaderImpl((URLClassLoader) Loader.instance().getModClassLoader());
        MCALoader.INSTANCE = instance;
        instance.init((URLClassLoader) MCALoaderImpl.class.getClassLoader());
    }

    static void launchPreInitInitialization0() {
        ((MCALoaderImpl) MCALoader.INSTANCE).launchPreInitInitialization();
    }

    static void launchInitInitialization0() {
        ((MCALoaderImpl) MCALoader.INSTANCE).launchInitInitialization();

    }

    static void launchPostInitInitialization0() {
        ((MCALoaderImpl) MCALoader.INSTANCE).launchPostInitInitialization();
    }

    private MCALoaderImpl(URLClassLoader classLoader) {
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
    protected @NotNull File getLoaderSource() {
        return Loader.instance().getActiveModList().stream().filter(it -> it.getModId().equals("mca")).findFirst().get().getSource();
    }
}
