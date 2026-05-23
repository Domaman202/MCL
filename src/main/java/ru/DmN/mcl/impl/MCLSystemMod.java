package ru.DmN.mcl.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.DmN.mcl.api.MCLMod;
import ru.DmN.mcl.api.MinecraftCrossLoader;

import java.io.File;

public abstract class MCLSystemMod extends MCLMod {
    @Override
    public @NotNull File getSource() {
        return MinecraftCrossLoader.getInstance().getLoaderSource();
    }

    @Override
    public @Nullable Dependency[] getDependencies() {
        return new Dependency[0];
    }

    @Override
    public @Nullable String getLogo() {
        return "assets/mcl/icon.png";
    }

    @Override
    public @Nullable String[] getAuthors() {
        return new String[] { "DomamaN202" };
    }

    @Override
    public @Nullable Contacts getContacts() {
        return new Contacts() {
            @Override
            public @Nullable String getHomepage() {
                return "https://github.com/Domaman202/MCL";
            }

            @Override
            public @Nullable String getSources() {
                return "https://github.com/Domaman202/MCL/tree/api";
            }
        };
    }

    public static final class MCL extends MCLSystemMod {
        @Override
        public @NotNull String getModId() {
            return "mcl";
        }

        @Override
        public @NotNull String getVersion() {
            return "1.10.0";
        }

        @Override
        public @NotNull String getName() {
            return "MCL";
        }

        @Override
        public @Nullable String getDescription() {
            return "Minecraft Cross Loader.";
        }
    }

    public static final class MCA extends MCLSystemMod {
        @Override
        public @NotNull String getModId() {
            return "mca";
        }

        @Override
        public @NotNull String getVersion() {
            return "1.0.0";
        }

        @Override
        public @NotNull String getName() {
            return "MCA";
        }

        @Override
        public @Nullable String getDescription() {
            return "Minecraft Cross Accessor.";
        }
    }
}
