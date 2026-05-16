package ru.DmN.mca.impl.forge;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;

import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import ru.DmN.mca.impl.MCALoader;
import ru.DmN.mca.impl.MCAMod;
import ru.DmN.mca.impl.Tags;
import ru.DmN.mca.impl.exception.MCALoaderException;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@cpw.mods.fml.common.Mod(modid = Mod.MODID, version = Tags.VERSION, name = "MCA", acceptedMinecraftVersions = "[1.7.10]")
public class Mod {
    public static final String MODID = "mca";

    @SidedProxy(clientSide = "ru.DmN.mca.impl.forge.ClientProxy", serverSide = "ru.DmN.mca.impl.forge.CommonProxy")
    public static CommonProxy proxy;

    @cpw.mods.fml.common.Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
        MCALoaderImpl.launchPreInitInitialization0();
    }

    @cpw.mods.fml.common.Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
        MCALoaderImpl.launchInitInitialization0();
    }

    @cpw.mods.fml.common.Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
        MCALoaderImpl.launchPostInitInitialization0();
        injectMCAModsToForgeList();
    }

    private static void injectMCAModsToForgeList() {
        try {
            Loader loader = Loader.instance();

            Field fieldMods = Loader.class.getDeclaredField("mods");
            Field fieldNamedMods = Loader.class.getDeclaredField("namedMods");
            fieldMods.setAccessible(true);
            fieldNamedMods.setAccessible(true);
            List<ModContainer> mods = (List<ModContainer>) fieldMods.get(loader);
            Map<String, ModContainer> namedMods = (Map<String, ModContainer>) fieldNamedMods.get(loader);
            List<ModContainer> activeModList = loader.getActiveModList();

            List<ModContainer> newMods = Lists.newArrayList(mods);
            Map<String, ModContainer> newNamedMods = new HashMap<>(namedMods);
            for (MCAMod mod : MCALoader.getInstance().getMods()) {
                ModContainer decorator = new MCAModContainerDecorator(mod);
                newMods.add(decorator);
                newNamedMods.put(mod.getModid(), decorator);
                activeModList.add(decorator);
            }
            fieldMods.set(loader, ImmutableList.copyOf(newMods));
            fieldNamedMods.set(loader, ImmutableMap.copyOf(newNamedMods));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new MCALoaderException(e);
        }
    }

    static {
        try {
            MCALoaderImpl.init0();
        } catch (MCALoaderException e) {
            throw new RuntimeException(e);
        }
    }
}
