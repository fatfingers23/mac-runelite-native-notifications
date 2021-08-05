package com.NativeMacNotifications;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class NativeMacNotificationsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(NativeMacNotificationsPlugin.class);
		RuneLite.main(args);
	}
}