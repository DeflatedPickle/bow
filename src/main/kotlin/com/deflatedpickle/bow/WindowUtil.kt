package com.deflatedpickle.bow

import com.sun.jna.Native
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef

/**
 * A utility object for working with windows on-screen
 */
object WindowUtil {
    /**
     * Gets the title of a window as a string
     */
    fun getTitle(hwnd: WinDef.HWND): String {
        val length = User32.INSTANCE.GetWindowTextLength(hwnd) + 1
        val windowText = CharArray(length)
        User32.INSTANCE.GetWindowText(hwnd, windowText, length)

        return Native.toString(windowText)
    }

    /**
     * Gets the class of the window
     */
    fun getClass(hwnd: WinDef.HWND): String {
        val className = CharArray(80)
        User32.INSTANCE.GetClassName(hwnd, className, 80)

        return Native.toString(className)
    }
}