package ru.DmN.mca.api;

public interface IModClientInitializer {
    default void preInitClient() {}
    default void initClient() {}
    default void postInitClient() {}
}
