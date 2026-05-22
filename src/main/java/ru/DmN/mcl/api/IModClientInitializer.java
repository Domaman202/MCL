package ru.DmN.mcl.api;

public interface IModClientInitializer {
    default void preInitClient() {}
    default void initClient() {}
    default void postInitClient() {}
}
