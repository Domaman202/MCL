package ru.DmN.mca.impl.forge;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import ru.DmN.mca.impl.MCALoader;
import ru.DmN.mca.impl.exception.MCALoaderException;

import java.io.File;
import java.net.URLClassLoader;

public final class MCALoaderImpl extends MCALoader {
    public static void init0() {
        if (MCALoader.INSTANCE != null)
            throw new MCALoaderException("Loader already initialized!");
        MCALoader.INSTANCE = new MCALoaderImpl();
        ((MCALoaderImpl) MCALoader.INSTANCE).init((URLClassLoader) MCALoaderImpl.class.getClassLoader());
    }

    public static void launchPreInitInitialization0() {
        ((MCALoaderImpl) MCALoader.INSTANCE).launchPreInitInitialization();
    }

    public static void launchInitInitialization0() {
        ((MCALoaderImpl) MCALoader.INSTANCE).launchInitInitialization();

    }

    public static void launchPostInitInitialization0() {
        ((MCALoaderImpl) MCALoader.INSTANCE).launchPostInitInitialization();
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
}
