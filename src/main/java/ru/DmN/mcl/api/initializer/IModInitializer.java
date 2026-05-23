package ru.DmN.mcl.api.initializer;

public interface IModInitializer {
    default void preInit() {}
    default void init() {}
    default void postInit() {}
}
