package com.NativeMacNotifications;

import com.google.common.base.Strings;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.NotificationFired;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.util.OSType;

@Slf4j
@PluginDescriptor(
	name = "Native Mac Notifications"
)
public class NativeMacNotificationsPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private PluginManager pluginManager;

	@Inject
	private RuneLiteConfig runeLiteConfig;

	@Inject
	private ClientUI clientUI;

	@Inject
	private ConfigManager configManager;

	private Boolean resetTrayNotificationsAtShutDown;

	@Subscribe
	public void onNotificationFired(NotificationFired notificationFired)
	{
		try
		{
			if (!runeLiteConfig.sendNotificationsWhenFocused() && clientUI.isFocused())
			{
				return;
			}

			Notification.notify(buildTitle(), notificationFired.getMessage());
		}
		catch (Exception e)
		{
			stopPlugin();
		}
	}

	@Override
	protected void startUp()
	{
		if (OSType.getOSType() != OSType.MacOS)
		{
			stopPlugin();
		}
		else
		{
			if (runeLiteConfig.enableTrayNotifications())
			{
				resetTrayNotificationsAtShutDown = true;
				configManager.setConfiguration(RuneLiteConfig.GROUP_NAME, "notificationTray", false);
			}
		}
	}

	@Override
	protected void shutDown()
	{
		if (resetTrayNotificationsAtShutDown)
		{
			configManager.setConfiguration(RuneLiteConfig.GROUP_NAME, "notificationTray", true);
		}
	}

	private void stopPlugin()
	{
		try
		{
			pluginManager.setPluginEnabled(this, false);
			pluginManager.stopPlugin(this);
		}
		catch (PluginInstantiationException ex)
		{
			log.error("error stopping plugin", ex);
		}
	}

	private String buildTitle()
	{
		String notificationTitle = "RuneLite";

		Player player = client.getLocalPlayer();
		if (player == null)
		{
			return notificationTitle;
		}

		String name = player.getName();
		if (Strings.isNullOrEmpty(name))
		{
			return notificationTitle;
		}

		return notificationTitle + " - " + name;
	}
}
