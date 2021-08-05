package com.NativeMacNotifications.macJna;

import com.sun.jna.NativeLong;

/**
 * Could be an address in memory (if pointer to a class or method) or a value (like 0 or 1)
 *
 * This code is from Intellij-community Github repo. Some has been edited or removed. But all credit goes to those contributors
 * https://github.com/JetBrains/intellij-community/blob/master/platform/core-api/src/com/intellij/util/indexing/ID.java
 * User: spLeaner
 */
public class ID extends NativeLong
{

	public ID()
	{
	}

	public ID(long peer)
	{
		super(peer);
	}

	public static final ID NIL = new ID(0L);

	public boolean booleanValue()
	{
		return intValue() != 0;
	}
}