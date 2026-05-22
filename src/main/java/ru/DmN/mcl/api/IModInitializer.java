package ru.DmN.mcl.api;

public interface IModInitializer {
    default void preInit() {}
    default void init() {}
    default void postInit() {}
}
