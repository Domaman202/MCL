package ru.DmN.mcl.impl.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.ModContainerImpl;
import net.fabricmc.loader.impl.entrypoint.EntrypointStorage;
import ru.DmN.mcl.api.MCLMod;
import ru.DmN.mcl.api.MinecraftCrossLoader;
import ru.DmN.mcl.api.exception.MCLException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class Mod implements ModInitializer {
	@Override
	public void onInitialize() {
		MCLImpl.launchInitInitialization0();
		injectPostLoadInitializer();
	}

	private static void injectPostLoadInitializer() {
		try {
			Field fieldEntrypointStorage = FabricLoaderImpl.class.getDeclaredField("entrypointStorage");
			fieldEntrypointStorage.setAccessible(true);
			EntrypointStorage entrypointStorage = (EntrypointStorage) fieldEntrypointStorage.get(FabricLoaderImpl.INSTANCE);
			Method methodGetOrCreateEntries = EntrypointStorage.class.getDeclaredMethod("getOrCreateEntries", String.class);
			methodGetOrCreateEntries.setAccessible(true);
			Constructor<?> ctorNewEntry = Class.forName("net.fabricmc.loader.impl.entrypoint.EntrypointStorage$NewEntry").getDeclaredConstructors()[0];
			ctorNewEntry.setAccessible(true);
			ModContainer container = FabricLoader.getInstance().getModContainer("mcl_impl").get();

			if (MinecraftCrossLoader.getInstance().isMinecraftClient()) {
				List<Object> entries = (List<Object>) methodGetOrCreateEntries.invoke(entrypointStorage, "client");
				entries.add(
						ctorNewEntry.newInstance(
								container,
								LanguageAdapter.getDefault(),
								"ru.DmN.mcl.impl.fabric.Mod$_ClientHelper"
						)
				);
			} else {
				List<Object> entries = (List<Object>) methodGetOrCreateEntries.invoke(entrypointStorage, "server");
				entries.add(
						ctorNewEntry.newInstance(
								container,
								LanguageAdapter.getDefault(),
								"ru.DmN.mcl.impl.fabric.Mod$_ServerHelper"
						)
				);
			}
		} catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException | InstantiationException e) {
			throw new MCLException(e);
		}
    }

	private static void injectMCAModsToFabricList() {
		try {
			FabricLoaderImpl loader = FabricLoaderImpl.INSTANCE;

			Field fieldModMap = FabricLoaderImpl.class.getDeclaredField("modMap");
			fieldModMap.setAccessible(true);

			Map<String, ModContainerImpl> modMap = (Map<String, ModContainerImpl>) fieldModMap.get(loader);
			List<ModContainerImpl> mods = loader.getModsInternal();

			for (MCLMod mod : MinecraftCrossLoader.getInstance().getMods()) {
				MCLModContainer container = new MCLModContainer(mod);
				modMap.put(mod.getModId(), container);
				mods.add(container);
			}
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new MCLException(e);
		}
	}

	static {
		MCLImpl.init0();
		injectMCAModsToFabricList();
		MCLImpl.launchPreInitInitialization0();
	}

	public static class _ClientHelper implements ClientModInitializer {
		@Override
		public void onInitializeClient() {
			MCLImpl.launchPostInitInitialization0();
		}
	}

	public static class _ServerHelper implements DedicatedServerModInitializer {
		@Override
		public void onInitializeServer() {
			MCLImpl.launchPostInitInitialization0();
		}
	}
}