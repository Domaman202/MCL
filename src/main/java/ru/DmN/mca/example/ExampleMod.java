package ru.DmN.mca.example;

import ru.DmN.mca.api.IModClientInitializer;
import ru.DmN.mca.api.IModInitializer;

public class ExampleMod implements IModInitializer, IModClientInitializer {
    @Override
    public void preInit() {
        System.out.println("[MCA-Example] Pre-init invoked!");
    }

    @Override
    public void init() {
        System.out.println("[MCA-Example] Init invoked!");
    }

    @Override
    public void postInit() {
        System.out.println("[MCA-Example] Post-Init invoked!");
    }

    @Override
    public void preInitClient() {
        System.out.println("[MCA-Example] Client pre-init invoked!");
    }

    @Override
    public void initClient() {
        System.out.println("[MCA-Example] Client init invoked!");
    }

    @Override
    public void postInitClient() {
        System.out.println("[MCA-Example] Client post-init invoked!");
    }
}
