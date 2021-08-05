package com.NativeMacNotifications;

import com.google.common.base.Strings;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.NotificationFired;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Native Mac Notifications"
)
public class NativeMacNotificationsPlugin extends Plugin
{
	@Inject
	private Client client;

	@Subscribe
	public void onNotificationFired(NotificationFired notificationFired)
	{
		Notification.notify(buildTitle(), notificationFired.getMessage());
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
