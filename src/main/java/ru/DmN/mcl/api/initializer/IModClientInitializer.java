package ru.DmN.mcl.api.initializer;

public interface IModClientInitializer {
    default void preInitClient() {}
    default void initClient() {}
    default void postInitClient() {}
}
