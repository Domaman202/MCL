package ru.DmN.mcl.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public abstract class MCLMod {
    public abstract @NotNull File getSource();
    public abstract @NotNull String getModId();
    public abstract @NotNull String getVersion();
    public abstract @Nullable Dependency[] getDependencies();

    public abstract @NotNull String getName();
    public abstract @Nullable String getDescription();
    public abstract @Nullable String getLogo();
    public abstract @Nullable String[] getAuthors();
    public abstract @Nullable Contacts getContacts();

    public static final class Default extends MCLMod {
        private final @NotNull File source;
        private final @NotNull String modId;
        private final @NotNull String version;
        private final @NotNull String name;
        private final @Nullable String description;
        private final @Nullable String logo;
        private final @Nullable String[] authors;
        private final @Nullable Contacts contacts;
        private final @Nullable Dependency[] dependencies;

        public Default(
                @NotNull File source,
                @NotNull String modId,
                @NotNull String version,
                @NotNull String name,
                @Nullable String description,
                @Nullable String logo,
                @Nullable String[] authors,
                @Nullable Contacts contacts,
                @Nullable Dependency[] dependencies
        ) {
            this.source = source;
            this.modId = modId;
            this.version = version;
            this.name = name;
            this.description = description;
            this.logo = logo;
            this.authors = authors;
            this.contacts = contacts;
            this.dependencies = dependencies;
        }

        @Override
        public @NotNull File getSource() {
            return this.source;
        }

        @Override
        public @NotNull String getModId() {
            return this.modId;
        }

        @Override
        public @NotNull String getVersion() {
            return this.version;
        }

        @Override
        public @Nullable Dependency[] getDependencies() {
            return this.dependencies;
        }

        @Override
        public @NotNull String getName() {
            return this.name;
        }

        @Override
        public @Nullable String getDescription() {
            return this.description;
        }

        @Override
        public @Nullable String getLogo() {
            return this.logo;
        }

        @Override
        public @Nullable String[] getAuthors() {
            return this.authors;
        }

        @Override
        public @Nullable Contacts getContacts() {
            return this.contacts;
        }
    }

    public static abstract class Contacts {
        public abstract @Nullable String getHomepage();
        public abstract @Nullable String getSources();

        public static final class Default extends Contacts {
            private final @Nullable String homepage;
            private final @Nullable String sources;

            public Default(@Nullable String homepage, @Nullable String sources) {
                this.homepage = homepage;
                this.sources = sources;
            }

            @Override
            public @Nullable String getHomepage() {
                return this.homepage;
            }

            @Override
            public @Nullable String getSources() {
                return this.sources;
            }
        }
    }

    public static abstract class Dependency {
        public abstract @NotNull String getModId();
        public abstract @NotNull String getVersion();

        public static final class Default extends Dependency {
            private final @NotNull String modId;
            private final @NotNull String version;

            public Default(@NotNull String modId, @NotNull String version) {
                this.modId = modId;
                this.version = version;
            }

            @Override
            public @NotNull String getModId() {
                return this.modId;
            }

            @Override
            public @NotNull String getVersion() {
                return this.version;
            }
        }
    }
}
