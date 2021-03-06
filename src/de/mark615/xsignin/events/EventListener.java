package de.mark615.xsignin.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.mark615.xsignin.XSignIn;
import de.mark615.xsignin.object.XPlayerSubject;
import de.mark615.xsignin.object.XUtil;

public class EventListener implements Listener
{
	private XSignIn plugin;

	public EventListener(XSignIn instance)
	{
		this.plugin = instance;
		registerTask();
	}

	
	public void messageAllPlayer()
	{
		for (Player p : Bukkit.getServer().getOnlinePlayers())
		{
			plugin.getLoginManager().getXSubjectPlayer(p.getUniqueId()).setLastLoginInfo(0);
		}
	}
	
	@EventHandler (priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		final Player p = e.getPlayer();
		
		if (!plugin.getLoginManager().getListManager().isPlayerAllowedToJoin(p))
		{
			if (plugin.getLoginManager().getListManager().isWhitelist())
			{
				p.kickPlayer(XUtil.getMessage("message.not-on-whitelist"));
			}
			else
			if (plugin.getLoginManager().getListManager().isBlacklist())
			{
				p.kickPlayer(XUtil.getMessage("message.on-blacklist"));
			}
			return;
		}
		
		if (plugin.isMaintenanceMode() && !p.hasPermission("xsignin.xmaintenance.join"))
		{
			p.kickPlayer(XUtil.getMessage("message.maintenance"));
			return;
		}
		
		checkLoggedIn(p);
		plugin.getLoginManager().registerPlayer(p);
		plugin.getLoginManager().getXSubjectPlayer(p.getUniqueId()).setLastLoginInfo(0);
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e)
	{
		this.plugin.getLoginManager().unregisterPlayer(e.getPlayer());
	}
	
	@EventHandler
	public void onPlayerMoveEvent(PlayerMoveEvent e)
	{
		final Player p = e.getPlayer();
		if (!checkLoggedIn(p))
			e.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockBreakEvent(BlockBreakEvent e)
	{
		final Player p = e.getPlayer();
		if (!checkLoggedIn(p))
			e.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerChatEvent(AsyncPlayerChatEvent e)
	{
		final Player p = e.getPlayer();
		if (!checkLoggedIn(p))
			e.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockPlayeEvent(BlockPlaceEvent e)
	{
		final Player p = e.getPlayer();
		if (!checkLoggedIn(p))
			e.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent e)
	{
		final Player p = e.getPlayer();
		if (!checkLoggedIn(p))
			e.setCancelled(true);
	}

	@EventHandler
	public void PlayerDropEvent(PlayerDropItemEvent e)
	{
		final Player p = e.getPlayer();
		if (!checkLoggedIn(p))
			e.setCancelled(true);
	}
	
	
	
	private boolean checkLoggedIn(Player p)
	{
		boolean value = false;
		if (hasPlayer(p))
		{
			if (!plugin.getLoginManager().isEnabled() || this.plugin.getLoginManager().isPlayerLoggedIn(p))
				value = true;
			else
				return false;
			
			if (plugin.getLoginManager().getAGBManager().isEnabled()) {
				if (this.plugin.getLoginManager().getAGBManager().hasXPlayerAcceptAGB(p.getUniqueId()))
					value = true;
				else
					return false;
			}
			else {
				value = true;
			}
		}
		return value;
	}
	
	private void registerTask()
	{
		Bukkit.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
			
			@Override
			public void run()
			{
				for (Player p : Bukkit.getServer().getOnlinePlayers())
				{
					XPlayerSubject subject = plugin.getLoginManager().getXSubjectPlayer(p.getUniqueId());
					if (System.currentTimeMillis() - subject.getLastLoginInfo() > plugin.getSettingManager().getLoginMessageIntervall())
					{
						if (!plugin.getLoginManager().hasAccount(p) && plugin.getLoginManager().isEnabled())
						{
							XUtil.sendFileMessage(p, "message.register", true);
						}
						else
						if (!subject.isLoggedIn() && plugin.getLoginManager().isEnabled())
						{
							XUtil.sendFileMessage(p, "message.login", true);
						}
						else
						if (!subject.hasAGBAccepted() && plugin.getLoginManager().getAGBManager().isEnabled())
						{
							XUtil.sendFileMessage(p, "message.agb", true);
						}
						subject.setLastLoginInfo(System.currentTimeMillis());
					}
				}
			}
		}, 1, 4);
	}
	
	private boolean hasPlayer(Player p)
	{
		return p == null ? false : (plugin.getLoginManager().getXSubjectPlayer(p.getUniqueId()) == null ? false : true);
	}
	
}
