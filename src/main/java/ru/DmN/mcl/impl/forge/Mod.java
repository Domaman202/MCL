package ru.DmN.mcl.impl.forge;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import ru.DmN.mcl.impl.*;
import ru.DmN.mcl.impl.exception.MCLException;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@cpw.mods.fml.common.Mod(modid = Mod.MODID, version = Tags.VERSION, name = "MCA", acceptedMinecraftVersions = "[1.7.10]")
public class Mod {
    public static final String MODID = "mca-loader-impl";

    @cpw.mods.fml.common.Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MCLImpl.launchPreInitInitialization0();
    }

    @cpw.mods.fml.common.Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MCLImpl.launchInitInitialization0();
    }

    @cpw.mods.fml.common.Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        MCLImpl.launchPostInitInitialization0();
        injectMCAModsToForgeList();
    }

    private static void injectMCAModsToForgeList() {
        try {
            Loader loader = Loader.instance();
            boolean isClient = MCLImpl.getInstance().isMinecraftClient();

            Field fieldMods = Loader.class.getDeclaredField("mods");
            Field fieldNamedMods = Loader.class.getDeclaredField("namedMods");
            fieldMods.setAccessible(true);
            fieldNamedMods.setAccessible(true);
            List<ModContainer> mods = (List<ModContainer>) fieldMods.get(loader);
            Map<String, ModContainer> namedMods = (Map<String, ModContainer>) fieldNamedMods.get(loader);
            List<ModContainer> activeModList = loader.getActiveModList();

            List<ModContainer> newMods = Lists.newArrayList(mods);
            Map<String, ModContainer> newNamedMods = new HashMap<>(namedMods);
            for (MCLMod mod : MinecraftCrossLoader.getInstance().getMods()) {
                ModContainer decorator = new MCAModContainer(mod);
                newMods.add(decorator);
                newNamedMods.put(mod.getModid(), decorator);
                activeModList.add(decorator);
                if (isClient) {
                    FMLClientHandler.instance().addModAsResource(decorator);
                }
            }
            fieldMods.set(loader, ImmutableList.copyOf(newMods));
            fieldNamedMods.set(loader, ImmutableMap.copyOf(newNamedMods));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new MCLException(e);
        }
    }

    static {
        try {
            MCLImpl.init0();
        } catch (MCLException e) {
            throw new RuntimeException(e);
        }
    }
}
