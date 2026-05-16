package ru.DmN.mca.impl.forge;

import com.google.common.collect.ImmutableList;
import cpw.mods.fml.common.*;
import ru.DmN.mca.impl.MCALoader;
import ru.DmN.mca.impl.MCAMod;

import java.io.File;

public final class MCAModContainerDecorator extends DummyModContainer {
    private final MCAMod original;

    public MCAModContainerDecorator(MCAMod original) {
        super(mcaModToForgeMetadata(original));
        this.original = original;
    }

    public MCAMod getOriginal() {
        return this.original;
    }

    @Override
    public File getSource() {
        return new File(MCALoader.getInstance().getMinecraftDirectory(), "mods-mca");
    }

    @Override
    public boolean isImmutable() {
        return true;
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
        MCAMod.Contacts contacts = mod.getContacts();
        if (contacts != null)
            metadata.url = contacts.getHomepage();
        metadata.version = mod.getVersion();
        String[] authors = mod.getAuthors();
        metadata.authorList = authors == null ? ImmutableList.of() : ImmutableList.copyOf(authors);
        return metadata;
    }
}
