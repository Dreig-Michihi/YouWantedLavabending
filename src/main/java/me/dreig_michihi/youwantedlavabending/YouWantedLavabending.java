package me.dreig_michihi.youwantedlavabending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.event.BendingReloadEvent;
import com.projectkorra.projectkorra.event.WorldTimeEvent;
import com.projectkorra.projectkorra.util.TempBlock;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Map;

public final class YouWantedLavabending extends JavaPlugin {

	public static YouWantedLavabending plugin;

	@Override
	public void onEnable() {
		// Plugin startup logic
		plugin = this;
		plugin.getLogger().info(" PK side-plugin made by Dreig_Michihi enabled.");
		Bukkit.getServer().getPluginManager().registerEvents(new BendingReloadListener(), YouWantedLavabending.plugin);
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					Field field = TempBlock.class.getDeclaredField("instances_");
					field.setAccessible(true);
					Map<Block, LinkedList<TempBlock>> instances = ((Map<Block, LinkedList<TempBlock>>) field.get(null));
					for (Map.Entry<Block, LinkedList<TempBlock>> entry : instances.entrySet()) {
						long time = entry.getKey().getWorld().getTime();
						if (time > 0 && time < 12300) { // day
							for (TempBlock block : entry.getValue()) {
								Material mat = block.getBlockData().getMaterial();
								if (mat == Material.WATER) {
									Levelled lava = (Levelled) Material.LAVA.createBlockData();
									lava.setLevel(((Levelled) block.getBlockData()).getLevel());
									block.setType(lava);
								} else if (mat == Material.ICE) {
									block.setType(Material.MAGMA_BLOCK);
								}
							}
						}
					}
				} catch (NoSuchFieldException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}.runTaskTimer(YouWantedLavabending.plugin, 0, 1);
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll(plugin);
		Bukkit.getScheduler().cancelTasks(plugin);
		plugin.getLogger().info(" PK side-plugin made by Dreig_Michihi disabled.");
		// Plugin shutdown logic
	}

	private static class BendingReloadListener implements Listener {
		@EventHandler
		public void onDayStart(WorldTimeEvent event) {
			WorldTimeEvent.Time from = event.getFrom();
			WorldTimeEvent.Time to = event.getTo();
			World world = event.getWorld();
			if (from != to) {
				for (final Player player : world.getPlayers()) {
					final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
					if (bPlayer == null) continue;

					if (bPlayer.hasElement(Element.WATER) && player.hasPermission("bending.message.daymessage") && to == WorldTimeEvent.Time.DAY) {
						String s = "Your waterbending has become LAVABENDING due to the SUN rising.";
						player.sendMessage(Element.LAVA.getColor() + s);
					}
				}
			}
		}

		@EventHandler
		public void onBendingReload(BendingReloadEvent event) {
			YouWantedLavabending.plugin.onDisable();
			Bukkit.getScheduler().runTask(plugin, () -> YouWantedLavabending.plugin.onEnable());
		}
	}

}
