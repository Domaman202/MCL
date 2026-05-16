package ru.DmN.mca.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MCAMod {
    private final @NotNull String modid;
    private final @NotNull String version;
    private final @NotNull String name;
    private final @Nullable String description;
    private final @Nullable String[] authors;
    private final @Nullable Contacts contacts;
    private final @Nullable Dependency[] dependencies;

    public MCAMod(
            @NotNull String modid,
            @NotNull String version,
            @NotNull String name,
            @Nullable String description,
            @Nullable String[] authors,
            @Nullable Contacts contacts,
            @Nullable Dependency[] dependencies
    ) {
        this.modid = modid;
        this.version = version;
        this.name = name;
        this.description = description;
        this.authors = authors;
        this.contacts = contacts;
        this.dependencies = dependencies;
    }

    public @NotNull String getModid() {
        return this.modid;
    }

    public @NotNull String getVersion() {
        return this.version;
    }

    public @NotNull String getName() {
        return this.name;
    }

    public @Nullable String getDescription() {
        return this.description;
    }

    public @Nullable String[] getAuthors() {
        return this.authors;
    }

    public @Nullable Contacts getContacts() {
        return this.contacts;
    }

    public @Nullable Dependency[] getDependencies() {
        return this.dependencies;
    }

    public static final class Contacts {
        private final @Nullable String homepage;
        private final @Nullable String sources;

        public Contacts(@Nullable String homepage, @Nullable String sources) {
            this.homepage = homepage;
            this.sources = sources;
        }

        public @Nullable String getHomepage() {
            return this.homepage;
        }

        public @Nullable String getSources() {
            return this.sources;
        }
    }

    public static final class Dependency {
        private final @NotNull String modid;
        private final @NotNull String version;

        public Dependency(@NotNull String modid, @NotNull String version) {
            this.modid = modid;
            this.version = version;
        }

        public @NotNull String getModid() {
            return this.modid;
        }

        public @NotNull String getVersion() {
            return this.version;
        }
    }
}
