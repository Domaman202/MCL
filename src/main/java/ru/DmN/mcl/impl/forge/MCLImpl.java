package ru.DmN.mcl.impl.forge;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import org.jetbrains.annotations.NotNull;
import ru.DmN.mcl.impl.MinecraftCrossLoader;
import ru.DmN.mcl.impl.exception.MCLException;

import java.io.File;
import java.net.URLClassLoader;

public final class MCLImpl extends MinecraftCrossLoader {
    static void init0() {
        if (MinecraftCrossLoader._INSTANCE != null)
            throw new MCLException("Loader already initialized!");
        MCLImpl instance = new MCLImpl((URLClassLoader) Mod.class.getClassLoader());
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

    private MCLImpl(URLClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public boolean isMinecraftClient() {
        return FMLCommonHandler.instance().getSide().isClient();
    }

    @Override
    public @NotNull File getMinecraftDirectory() {
        if (this.isMinecraftClient()) {
            return Minecraft.getMinecraft().gameDir;
        } else {
            return new File(".");
        }
    }

    @Override
    protected @NotNull File getLoaderSource() {
        return Loader.instance().getActiveModList().stream().filter(it -> it.getModId().equals("mcl_impl")).findFirst().get().getSource();
    }
}
