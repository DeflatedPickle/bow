package com.deflatedpickle.bow;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinNT;

public class HTHEME extends WinNT.HANDLE {
    public HTHEME() {
    }

    public HTHEME(Pointer p) {
        super(p);
    }
}
