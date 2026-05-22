package ru.DmN.mcl.impl.forge;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.FMLThrowingEventBus;
import ru.DmN.mcl.impl.MCLMod;
import ru.DmN.mcl.impl.MinecraftCrossLoader;
import ru.DmN.mcl.impl.exception.MCLException;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@net.minecraftforge.fml.common.Mod(modid = Mod.MOD_ID, name = Mod.NAME, version = Mod.VERSION)
public class Mod {
	public static final String MOD_ID = "mcl_impl";
	public static final String NAME = "Minecraft Cross Loader";
	public static final String VERSION = "0@VERSION@";

	@net.minecraftforge.fml.common.Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		MCLImpl.launchPreInitInitialization0();
	}

	@net.minecraftforge.fml.common.Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		MCLImpl.launchInitInitialization0();
	}

	@net.minecraftforge.fml.common.Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		MCLImpl.launchPostInitInitialization0();
		injectMCAModsToForgeList();
	}

	private static void injectMCAModsToForgeList() {
		try {
			Loader loader = Loader.instance();
			boolean isClient = MCLImpl.getInstance().isMinecraftClient();

			Field fieldMods = Loader.class.getDeclaredField("mods");
			Field fieldNamedMods = Loader.class.getDeclaredField("namedMods");
			Field fieldModController = Loader.class.getDeclaredField("modController");
			Field fieldActiveModsList = LoadController.class.getDeclaredField("activeModList");
			Field fieldModStates = LoadController.class.getDeclaredField("modStates");
			Field fieldEventChannels = LoadController.class.getDeclaredField("eventChannels");
			fieldMods.setAccessible(true);
			fieldNamedMods.setAccessible(true);
			fieldModController.setAccessible(true);
			fieldActiveModsList.setAccessible(true);
			fieldModStates.setAccessible(true);
			fieldEventChannels.setAccessible(true);
			List<ModContainer> mods = (List<ModContainer>) fieldMods.get(loader);
			Map<String, ModContainer> namedMods = (Map<String, ModContainer>) fieldNamedMods.get(loader);
			LoadController loadController = (LoadController) fieldModController.get(loader);
			List<ModContainer> activeModList = (List<ModContainer>) fieldActiveModsList.get(loadController);
			Multimap<String, LoaderState.ModState> modStates = (Multimap<String, LoaderState.ModState>) fieldModStates.get(loadController);
			ImmutableMap<String, EventBus> eventChannels = (ImmutableMap<String, EventBus>) fieldEventChannels.get(loadController);

			List<ModContainer> newMods = Lists.newArrayList(mods);
			Map<String, ModContainer> newNamedMods = new HashMap<>(namedMods);
			List<ModContainer> newActiveModsList = Lists.newArrayList(activeModList);
			ImmutableMap.Builder<String, EventBus> newEventChannels = ImmutableMap.builder();
			newEventChannels.putAll(eventChannels);

			for (MCLMod mod : MinecraftCrossLoader.getInstance().getMods()) {
				ModContainer decorator = new MCLModContainer(mod);

				newMods.add(decorator);
				newNamedMods.put(mod.getModid(), decorator);
				newActiveModsList.add(decorator);
				modStates.put(decorator.getModId(), LoaderState.ModState.AVAILABLE);
				newEventChannels.put(decorator.getModId(), new FMLThrowingEventBus((exception, context) -> loadController.errorOccurred(decorator, exception)));

				if (isClient) {
					FMLClientHandler.instance().addModAsResource(decorator);
				}
			}

			fieldMods.set(loader, ImmutableList.copyOf(newMods));
			fieldNamedMods.set(loader, ImmutableMap.copyOf(newNamedMods));
			fieldActiveModsList.set(loadController, newActiveModsList);
			fieldEventChannels.set(loadController, newEventChannels.build());
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new MCLException(e);
		}
	}

	static {
		try {
			MCLImpl.init0();
		} catch (MCLException e) {
			throw new RuntimeException(e);
		}
	}
}
