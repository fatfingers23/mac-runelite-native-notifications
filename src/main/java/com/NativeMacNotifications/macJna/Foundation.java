package com.NativeMacNotifications.macJna;

import com.sun.jna.Function;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Proxy;
import javax.annotation.Nullable;

/**
 * This code is from Intellij-community Github repo. Some has been edited or removed. But all credit goes to those contributors
 * https://github.com/JetBrains/intellij-community/blob/master/platform/util/ui/src/com/intellij/ui/mac/foundation/Foundation.java
 */
public final class Foundation
{
	private static final FoundationLibrary myFoundationLibrary;
	private static final Function myObjcMsgSend;

	static
	{
		myFoundationLibrary = Native.loadLibrary("Foundation", FoundationLibrary.class);
		NativeLibrary nativeLibrary = ((Library.Handler) Proxy.getInvocationHandler(myFoundationLibrary)).getNativeLibrary();
		myObjcMsgSend = nativeLibrary.getFunction("objc_msgSend");
	}

	public static void init()
	{ /* fake method to init foundation */ }

	private Foundation()
	{
	}

	/**
	 * Get the ID of the NSClass with className
	 */
	public static ID getObjcClass(String className)
	{
		return myFoundationLibrary.objc_getClass(className);
	}

	public static Pointer createSelector(String s)
	{
		return myFoundationLibrary.sel_registerName(s);
	}

	private static Object[] prepInvoke(ID id, Pointer selector, Object[] args)
	{
		Object[] invokArgs = new Object[args.length + 2];
		invokArgs[0] = id;
		invokArgs[1] = selector;
		System.arraycopy(args, 0, invokArgs, 2, args.length);
		return invokArgs;
	}

	public static ID invoke(final ID id, final Pointer selector, Object... args)
	{
		// objc_msgSend is called with the calling convention of the target method
		// on x86_64 this does not make a difference, but arm64 uses a different calling convention for varargs
		// it is therefore important to not call objc_msgSend as a vararg function
		return new ID(myObjcMsgSend.invokeLong(prepInvoke(id, selector, args)));
	}

	public static ID invoke(final String cls, final String selector, Object... args)
	{
		return invoke(getObjcClass(cls), createSelector(selector), args);
	}

	public static ID invoke(final ID id, final String selector, Object... args)
	{
		return invoke(id, createSelector(selector), args);
	}

	private static final class NSString
	{
		private static final ID nsStringCls = getObjcClass("NSString");
		private static final Pointer stringSel = createSelector("string");
		private static final Pointer allocSel = createSelector("alloc");
		private static final Pointer autoreleaseSel = createSelector("autorelease");
		private static final Pointer initWithBytesLengthEncodingSel = createSelector("initWithBytes:length:encoding:");
		private static final long nsEncodingUTF16LE = convertCFEncodingToNS(FoundationLibrary.kCFStringEncodingUTF16LE);

		public static ID create(String s)
		{
			// Use a byte[] rather than letting jna do the String -> char* marshalling itself.
			// Turns out about 10% quicker for long strings.
			if (s.isEmpty())
			{
				return invoke(nsStringCls, stringSel);
			}

			byte[] utf16Bytes = s.getBytes(StandardCharsets.UTF_16LE);
			return invoke(invoke(invoke(nsStringCls, allocSel),
				initWithBytesLengthEncodingSel, utf16Bytes, utf16Bytes.length, nsEncodingUTF16LE),
				autoreleaseSel);
		}
	}

	public static ID nsString(@Nullable String s)
	{
		return s == null ? ID.NIL : NSString.create(s);
	}

	private static long convertCFEncodingToNS(long cfEncoding)
	{
		return myFoundationLibrary.CFStringConvertEncodingToNSStringEncoding(cfEncoding) & 0xffffffffffL;  // trim to C-type limits
	}
}
