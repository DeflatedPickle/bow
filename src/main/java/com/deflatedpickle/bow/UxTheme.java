package com.deflatedpickle.bow;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface UxTheme extends StdCallLibrary, WinUser, WinNT {
    UxTheme INSTANCE = Native.load("UxTheme", UxTheme.class, W32APIOptions.DEFAULT_OPTIONS);

    long GBF_DIRECT = 0x00000001;
    long GBF_COPY = 0x00000002;
    long GBF_VALIDBITS = (GBF_DIRECT | GBF_COPY);

    HTHEME OpenThemeData(HWND hwnd, String pszClassList);

    HRESULT GetThemeBitmap(HTHEME hTheme, int iPartId, int iStateId, int iPropId, long dwFlags, Pointer phBitmap);
}
