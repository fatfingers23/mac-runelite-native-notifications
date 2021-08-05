package com.NativeMacNotifications.macJna;

import com.sun.jna.Callback;
import com.sun.jna.FromNativeContext;
import com.sun.jna.Function;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.NativeMapped;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
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
		myFoundationLibrary = Native.load("Foundation", FoundationLibrary.class, Collections.singletonMap("jna.encoding", "UTF8"));
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

	public static ID getProtocol(String name)
	{
		return myFoundationLibrary.objc_getProtocol(name);
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

	/**
	 * Invokes the given vararg selector.
	 * Expects `NSArray arrayWithObjects:(id), ...` like signature, i.e. exactly one fixed argument, followed by varargs.
	 */
	public static ID invokeVarArg(final ID id, final Pointer selector, Object... args)
	{
		// c functions and objc methods have at least 1 fixed argument, we therefore need to separate out the first argument
		return myFoundationLibrary.objc_msgSend(id, selector, args[0], Arrays.copyOfRange(args, 1, args.length));
	}

	public static ID invoke(final String cls, final String selector, Object... args)
	{
		return invoke(getObjcClass(cls), createSelector(selector), args);
	}

	public static ID invokeVarArg(final String cls, final String selector, Object... args)
	{
		return invokeVarArg(getObjcClass(cls), createSelector(selector), args);
	}

	public static ID safeInvoke(final String stringCls, final String stringSelector, Object... args)
	{
		ID cls = getObjcClass(stringCls);
		Pointer selector = createSelector(stringSelector);
		if (!invoke(cls, "respondsToSelector:", selector).booleanValue())
		{
			throw new RuntimeException(String.format("Missing selector %s for %s", stringSelector, stringCls));
		}
		return invoke(cls, selector, args);
	}

	public static ID invoke(final ID id, final String selector, Object... args)
	{
		return invoke(id, createSelector(selector), args);
	}

	public static double invoke_fpret(ID receiver, Pointer selector, Object... args)
	{
		return myObjcMsgSend.invokeDouble(prepInvoke(receiver, selector, args));
	}

	public static double invoke_fpret(ID receiver, String selector, Object... args)
	{
		return invoke_fpret(receiver, createSelector(selector), args);
	}

	public static boolean isNil(ID id)
	{
		return id == null || ID.NIL.equals(id);
	}

	public static ID safeInvoke(final ID id, final String stringSelector, Object... args)
	{
		Pointer selector = createSelector(stringSelector);
		if (!id.equals(ID.NIL) && !invoke(id, "respondsToSelector:", selector).booleanValue())
		{
			throw new RuntimeException(String.format("Missing selector %s for %s", stringSelector, toStringViaUTF8(invoke(id, "description"))));
		}
		return invoke(id, selector, args);
	}

	public static ID allocateObjcClassPair(ID superCls, String name)
	{
		return myFoundationLibrary.objc_allocateClassPair(superCls, name, 0);
	}

	public static void registerObjcClassPair(ID cls)
	{
		myFoundationLibrary.objc_registerClassPair(cls);
	}

	public static boolean isClassRespondsToSelector(ID cls, Pointer selectorName)
	{
		return myFoundationLibrary.class_respondsToSelector(cls, selectorName);
	}

	/**
	 * @param cls          The class to which to add a method.
	 * @param selectorName A selector that specifies the name of the method being added.
	 * @param impl         A function which is the implementation of the new method. The function must take at least two arguments-self and _cmd.
	 * @param types        An array of characters that describe the types of the arguments to the method.
	 *                     See <a href="https://developer.apple.com/library/IOs/documentation/Cocoa/Conceptual/ObjCRuntimeGuide/Articles/ocrtTypeEncodings.html#//apple_ref/doc/uid/TP40008048-CH100"></a>
	 * @return true if the method was added successfully, otherwise false (for NativeMacNotifications, the class already contains a method implementation with that name).
	 */
	public static boolean addMethod(ID cls, Pointer selectorName, Callback impl, String types)
	{
		return myFoundationLibrary.class_addMethod(cls, selectorName, impl, types);
	}

	public static boolean addProtocol(ID aClass, ID protocol)
	{
		return myFoundationLibrary.class_addProtocol(aClass, protocol);
	}

	public static boolean addMethodByID(ID cls, Pointer selectorName, ID impl, String types)
	{
		return myFoundationLibrary.class_addMethod(cls, selectorName, impl, types);
	}

	public static boolean isMetaClass(ID cls)
	{
		return myFoundationLibrary.class_isMetaClass(cls);
	}

	public static String stringFromSelector(Pointer selector)
	{
		ID id = myFoundationLibrary.NSStringFromSelector(selector);
		return ID.NIL.equals(id) ? null : toStringViaUTF8(id);
	}

	public static String stringFromClass(ID aClass)
	{
		ID id = myFoundationLibrary.NSStringFromClass(aClass);
		return ID.NIL.equals(id) ? null : toStringViaUTF8(id);
	}

	public static Pointer getClass(Pointer clazz)
	{
		return myFoundationLibrary.objc_getClass(clazz);
	}

	public static String fullUserName()
	{
		return toStringViaUTF8(myFoundationLibrary.NSFullUserName());
	}

	public static ID class_replaceMethod(ID cls, Pointer selector, Callback impl, String types)
	{
		return myFoundationLibrary.class_replaceMethod(cls, selector, impl, types);
	}

	public static ID getMetaClass(String className)
	{
		return myFoundationLibrary.objc_getMetaClass(className);
	}

	public static boolean isPackageAtPath(final String path)
	{
		final ID workspace = invoke("NSWorkspace", "sharedWorkspace");
		final ID result = invoke(workspace, createSelector("isFilePackageAtPath:"), nsString(path));

		return result.booleanValue();
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

	@Nullable
	public static String toStringViaUTF8(ID cfString)
	{
		if (ID.NIL.equals(cfString))
		{
			return null;
		}

		int lengthInChars = myFoundationLibrary.CFStringGetLength(cfString);
		int potentialLengthInBytes = 3 * lengthInChars + 1; // UTF8 fully escaped 16 bit chars, plus nul

		byte[] buffer = new byte[potentialLengthInBytes];
		byte ok = myFoundationLibrary.CFStringGetCString(cfString, buffer, buffer.length, FoundationLibrary.kCFStringEncodingUTF8);
		if (ok == 0)
		{
			throw new RuntimeException("Could not convert string");
		}
		return Native.toString(buffer);
	}


	private static long convertCFEncodingToNS(long cfEncoding)
	{
		return myFoundationLibrary.CFStringConvertEncodingToNSStringEncoding(cfEncoding) & 0xffffffffffL;  // trim to C-type limits
	}


	private static Callback ourRunnableCallback;
	private static long ourCurrentRunnableCount = 0;
	private static final Object RUNNABLE_LOCK = new Object();

	static class RunnableInfo
	{
		RunnableInfo(Runnable runnable, boolean useAutoreleasePool)
		{
			myRunnable = runnable;
			myUseAutoreleasePool = useAutoreleasePool;
		}

		Runnable myRunnable;
		boolean myUseAutoreleasePool;
	}

//	public static void executeOnMainThread(final boolean withAutoreleasePool, final boolean waitUntilDone, final Runnable runnable) {
//		String runnableCountString;
//		synchronized (RUNNABLE_LOCK) {
//			initRunnableSupport();
//
//			runnableCountString = String.valueOf(++ourCurrentRunnableCount);
//			ourMainThreadRunnables.put(runnableCountString, new RunnableInfo(runnable, withAutoreleasePool));
//		}
//
//		// fixme: Use Grand Central Dispatch instead?
//		final ID ideaRunnable = getObjcClass("IdeaRunnable");
//		final ID runnableObject = invoke(invoke(ideaRunnable, "alloc"), "init");
//		final ID keyObject = invoke(nsString(runnableCountString), "retain");
//		invoke(runnableObject, "performSelectorOnMainThread:withObject:waitUntilDone:", createSelector("run:"),
//			keyObject, Boolean.valueOf(waitUntilDone));
//		invoke(runnableObject, "release");
//	}


	@Structure.FieldOrder({"origin", "size"})
	public static class NSRect extends Structure implements Structure.ByValue
	{
		public NSPoint origin;
		public NSSize size;

		public NSRect(double x, double y, double w, double h)
		{
			origin = new NSPoint(x, y);
			size = new NSSize(w, h);
		}
	}

	@Structure.FieldOrder({"x", "y"})
	public static class NSPoint extends Structure implements Structure.ByValue
	{
		public CGFloat x;
		public CGFloat y;

		@SuppressWarnings("UnusedDeclaration")
		public NSPoint()
		{
			this(0, 0);
		}

		public NSPoint(double x, double y)
		{
			this.x = new CGFloat(x);
			this.y = new CGFloat(y);
		}
	}

	@Structure.FieldOrder({"width", "height"})
	public static class NSSize extends Structure implements Structure.ByValue
	{
		public CGFloat width;
		public CGFloat height;

		@SuppressWarnings("UnusedDeclaration")
		public NSSize()
		{
			this(0, 0);
		}

		public NSSize(double width, double height)
		{
			this.width = new CGFloat(width);
			this.height = new CGFloat(height);
		}
	}

	public static class CGFloat implements NativeMapped
	{
		private final double value;

		@SuppressWarnings("UnusedDeclaration")
		public CGFloat()
		{
			this(0);
		}

		public CGFloat(double d)
		{
			value = d;
		}

		@Override
		public Object fromNative(Object o, FromNativeContext fromNativeContext)
		{
			switch (Native.LONG_SIZE)
			{
				case 4:
					return new CGFloat((Float) o);
				case 8:
					return new CGFloat((Double) o);
			}
			throw new IllegalStateException();
		}

		@Override
		public Object toNative()
		{
			switch (Native.LONG_SIZE)
			{
				case 4:
					return (float) value;
				case 8:
					return value;
			}
			throw new IllegalStateException();
		}

		@Override
		public Class<?> nativeType()
		{
			switch (Native.LONG_SIZE)
			{
				case 4:
					return Float.class;
				case 8:
					return Double.class;
			}
			throw new IllegalStateException();
		}
	}

	public static ID fillArray(final Object[] a)
	{
		final ID result = invoke("NSMutableArray", "array");
		for (Object s : a)
		{
			invoke(result, "addObject:", convertType(s));
		}

		return result;
	}

	public static Object[] convertTypes(Object[] v)
	{
		final Object[] result = new Object[v.length + 1];
		for (int i = 0; i < v.length; i++)
		{
			result[i] = convertType(v[i]);
		}
		result[v.length] = ID.NIL;
		return result;
	}

	private static Object convertType(Object o)
	{
		if (o instanceof Pointer || o instanceof ID)
		{
			return o;
		}
		else if (o instanceof String)
		{
			return nsString((String) o);
		}
		else
		{
			throw new IllegalArgumentException("Unsupported type! " + o.getClass());
		}
	}
}
