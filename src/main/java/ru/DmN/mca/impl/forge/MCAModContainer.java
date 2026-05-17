package ru.DmN.mca.impl.forge;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.fml.client.FMLFileResourcePack;
import net.minecraftforge.fml.client.FMLFolderResourcePack;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.ModMetadata;
import ru.DmN.mca.impl.MCAMod;

import java.io.File;

public final class MCAModContainer extends DummyModContainer {
    private final MCAMod original;

    public MCAModContainer(MCAMod original) {
        super(mcaModToForgeMetadata(original));
        this.original = original;
    }

    public MCAMod getOriginal() {
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

    private static ModMetadata mcaModToForgeMetadata(MCAMod mod) {
        ModMetadata metadata = new ModMetadata();
        metadata.modId = mod.getModid();
        metadata.name = mod.getName();
        metadata.description = mod.getDescription();
        metadata.logoFile = mod.getLogo();
        MCAMod.Contacts contacts = mod.getContacts();
        if (contacts != null)
            metadata.url = contacts.getHomepage();
        metadata.version = mod.getVersion();
        String[] authors = mod.getAuthors();
        metadata.authorList = authors == null ? ImmutableList.of() : ImmutableList.copyOf(authors);
        return metadata;
    }
}