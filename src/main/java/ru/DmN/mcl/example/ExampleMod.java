package ru.DmN.mcl.example;

import ru.DmN.mcl.api.IModClientInitializer;
import ru.DmN.mcl.api.IModInitializer;

public class ExampleMod implements IModInitializer, IModClientInitializer {
    @Override
    public void preInit() {
        System.out.println("[MCL-Example] Pre-init invoked!");
    }

    @Override
    public void init() {
        System.out.println("[MCL-Example] Init invoked!");
    }

    @Override
    public void postInit() {
        System.out.println("[MCL-Example] Post-Init invoked!");
    }

    @Override
    public void preInitClient() {
        System.out.println("[MCL-Example] Client pre-init invoked!");
    }

    @Override
    public void initClient() {
        System.out.println("[MCL-Example] Client init invoked!");
    }

    @Override
    public void postInitClient() {
        System.out.println("[MCL-Example] Client post-init invoked!");
    }
}
