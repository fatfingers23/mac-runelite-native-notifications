package com.NativeMacNotifications.macJna;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

/**
 * This code is from Intellij-community Github repo. Some has been edited or removed. But all credit goes to those contributors
 * https://github.com/JetBrains/intellij-community/blob/master/platform/util/ui/src/com/intellij/ui/mac/foundation/FoundationLibrary.java
 */
public interface FoundationLibrary extends Library
{
	long CFStringConvertEncodingToNSStringEncoding(long cfEncoding);

	ID objc_getClass(String className);

	Pointer sel_registerName(String selectorName);

	int kCFStringEncodingUTF16LE = 0x14000100;
}
