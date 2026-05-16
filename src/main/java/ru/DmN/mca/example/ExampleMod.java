package ru.DmN.mca.example;

import ru.DmN.mca.api.IClientMod;
import ru.DmN.mca.api.IMod;

public class ExampleMod implements IMod, IClientMod {
    @Override
    public void preInitClient() {
        System.out.println("[MCA-Example] Client pre-init invoked!");
    }

    @Override
    public void initClient() {
        System.out.println("[MCA-Example] Client init invoked!");
    }

    @Override
    public void preInit() {
        System.out.println("[MCA-Example] Pre-init invoked!");
    }

    @Override
    public void init() {
        System.out.println("[MCA-Example] Init invoked!");
    }
}
