package com.deflatedpickle.bow;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.win32.W32APIOptions;

public interface User32Extended extends User32 {
    User32Extended INSTANCE = Native.load("user32", User32Extended.class, W32APIOptions.DEFAULT_OPTIONS);

    long SB_SETTEXTW = WM_USER + 11;
    long SB_SETTEXTA = WM_USER + 2;

    long SB_SETPARTS = WM_USER + 4;

    boolean SetWindowText(HWND hwnd, String lpString);
}
