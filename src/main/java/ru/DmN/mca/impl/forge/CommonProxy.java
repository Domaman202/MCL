package ru.DmN.mca.impl.forge;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
        MCALoaderImpl.launchPreInitInitialization0();
    }

    public void init(FMLInitializationEvent event) {
        MCALoaderImpl.launchInitInitialization0();
    }

    public void postInit(FMLPostInitializationEvent event) {
        MCALoaderImpl.launchPostInitInitialization0();
    }
}
