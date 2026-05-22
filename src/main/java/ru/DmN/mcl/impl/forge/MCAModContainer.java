package ru.DmN.mcl.impl.forge;

import com.google.common.collect.ImmutableList;
import cpw.mods.fml.client.FMLFileResourcePack;
import cpw.mods.fml.client.FMLFolderResourcePack;
import cpw.mods.fml.common.*;
import ru.DmN.mcl.impl.MCLMod;

import java.io.File;

public final class MCAModContainer extends DummyModContainer {
    private final MCLMod original;

    public MCAModContainer(MCLMod original) {
        super(mcaModToForgeMetadata(original));
        this.original = original;
    }

    public MCLMod getOriginal() {
        return this.original;
    }

    @Override
    public File getSource() {
        return this.original.getSource();
    }

    @Override
    public boolean isImmutable() {
        return true;
    }

    @Override
    public Class<?> getCustomResourcePackClass() {
        File source = this.original.getSource();
        if (source == null || !source.exists())
            return null;
        if (source.isDirectory())
            return FMLFolderResourcePack.class;
        return FMLFileResourcePack.class;
    }

    @Override
    public String toString() {
        return "MCAMod:"+this.getModId()+"{"+this.getVersion()+"}";
    }

    private static ModMetadata mcaModToForgeMetadata(MCLMod mod) {
        ModMetadata metadata = new ModMetadata();
        metadata.modId = mod.getModid();
        metadata.name = mod.getName();
        metadata.description = mod.getDescription();
        metadata.logoFile = mod.getLogo();
        MCLMod.Contacts contacts = mod.getContacts();
        if (contacts != null)
            metadata.url = contacts.getHomepage();
        metadata.version = mod.getVersion();
        String[] authors = mod.getAuthors();
        metadata.authorList = authors == null ? ImmutableList.of() : ImmutableList.copyOf(authors);
        return metadata;
    }
}
