package ru.DmN.mca.api;

public interface IModInitializer {
    default void preInit() {}
    default void init() {}
    default void postInit() {}
}
