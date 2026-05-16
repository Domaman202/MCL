package ru.DmN.mca.impl.forge;

import cpw.mods.fml.common.event.FMLPostInitializationEvent;

import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import ru.DmN.mca.impl.Tags;
import ru.DmN.mca.impl.exception.MCALoaderException;

@cpw.mods.fml.common.Mod(modid = Mod.MODID, version = Tags.VERSION, name = "MCA", acceptedMinecraftVersions = "[1.7.10]")
public class Mod {
    public static final String MODID = "mca";

    @SidedProxy(clientSide = "ru.DmN.mca.impl.forge.ClientProxy", serverSide = "ru.DmN.mca.impl.forge.CommonProxy")
    public static CommonProxy proxy;

    @cpw.mods.fml.common.Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @cpw.mods.fml.common.Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @cpw.mods.fml.common.Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    static {
        try {
            MCALoaderImpl.init0();
        } catch (MCALoaderException e) {
            throw new RuntimeException(e);
        }
    }
}
