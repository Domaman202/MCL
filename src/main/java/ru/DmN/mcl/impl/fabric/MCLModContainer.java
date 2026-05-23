package ru.DmN.mcl.impl.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.*;
import net.fabricmc.loader.impl.ModContainerImpl;
import net.fabricmc.loader.impl.discovery.ModCandidateImpl;
import net.fabricmc.loader.impl.metadata.ContactInformationImpl;
import net.fabricmc.loader.impl.metadata.EntrypointMetadata;
import net.fabricmc.loader.impl.metadata.LoaderModMetadata;
import net.fabricmc.loader.impl.metadata.NestedJarEntry;
import ru.DmN.mcl.api.MCLMod;
import ru.DmN.mcl.api.exception.MCLException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public final class MCLModContainer extends ModContainerImpl {
    private final MCLMod original;

    public MCLModContainer(MCLMod original) {
        super(mclModToFabricModCandidate(original));
        this.original = original;
    }

    public MCLMod getOriginal() {
        return this.original;
    }

    private static ModCandidateImpl mclModToFabricModCandidate(MCLMod original) {
        try {
            Method method = ModCandidateImpl.class.getDeclaredMethod("createPlain", List.class, LoaderModMetadata.class, boolean.class, Collection.class);
            method.setAccessible(true);
            return (ModCandidateImpl) method.invoke(
                    null,
                    List.of(original.getSource().toPath()),
                    new MCLLoaderModMetadata(original),
                    false,
                    List.of()
            );
        } catch (NoSuchMethodException | SecurityException | InvocationTargetException | IllegalAccessException e) {
            throw new MCLException(e);
        }
    }

    private static final class MCLLoaderModMetadata implements LoaderModMetadata {
        private final MCLMod original;

        public MCLLoaderModMetadata(MCLMod original) {
            this.original = original;
        }

        @Override
        public int getSchemaVersion() {
            return 0;
        }

        @Override
        public Map<String, String> getLanguageAdapterDefinitions() {
            return Map.of();
        }

        @Override
        public Collection<NestedJarEntry> getJars() {
            return List.of();
        }

        @Override
        public Collection<String> getMixinConfigs(EnvType type) {
            return List.of();
        }

        @Override
        public String getClassTweaker() {
            return "";
        }

        @Override
        public boolean loadsInEnvironment(EnvType type) {
            return false;
        }

        @Override
        public Collection<String> getOldInitializers() {
            return List.of();
        }

        @Override
        public List<EntrypointMetadata> getEntrypoints(String type) {
            return List.of();
        }

        @Override
        public Collection<String> getEntrypointKeys() {
            return List.of();
        }

        @Override
        public void emitFormatWarnings() {

        }

        @Override
        public void setVersion(Version version) {

        }

        @Override
        public void setDependencies(Collection<ModDependency> dependencies) {

        }

        @Override
        public String getType() {
            return "mcl";
        }

        @Override
        public String getId() {
            return this.original.getModId();
        }

        @Override
        public Collection<String> getProvides() {
            return List.of();
        }

        @Override
        public Version getVersion() {
            try {
                return Version.parse(this.original.getVersion());
            } catch (VersionParsingException e) {
                throw new MCLException(e);
            }
        }

        @Override
        public ModEnvironment getEnvironment() {
            return ModEnvironment.UNIVERSAL;
        }

        @Override
        public Collection<ModDependency> getDependencies() {
            return List.of();
        }

        @Override
        public String getName() {
            return this.original.getName();
        }

        @Override
        public String getDescription() {
            return this.original.getDescription();
        }

        @Override
        public Collection<Person> getAuthors() {
            List<Person> authors = new ArrayList<>();
            for (int i = 0; i < this.original.getAuthors().length; i++)
                authors.add(new SimplePerson(this.original.getAuthors()[i]));
            return authors;
        }

        @Override
        public Collection<Person> getContributors() {
            return List.of();
        }

        @Override
        public ContactInformation getContact() {
            if (this.original.getContacts() == null)
                return ContactInformationImpl.EMPTY;
            Map<String, String> map = new HashMap<>();
            map.put("homepage", this.original.getContacts().getHomepage());
            map.put("sources", this.original.getContacts().getSources());
            return new ContactInformationImpl(map);
        }

        @Override
        public Collection<String> getLicense() {
            return List.of();
        }

        @Override
        public Optional<String> getIconPath(int size) {
            return Optional.ofNullable(this.original.getLogo());
        }

        @Override
        public boolean containsCustomValue(String key) {
            return false;
        }

        @Override
        public CustomValue getCustomValue(String key) {
            return null;
        }

        @Override
        public Map<String, CustomValue> getCustomValues() {
            return Map.of();
        }

        @Override
        public boolean containsCustomElement(String key) {
            return false;
        }
    }

    private static final class SimplePerson implements Person {
        private final String name;

        public SimplePerson(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public ContactInformation getContact() {
            return ContactInformation.EMPTY;
        }
    }
}
