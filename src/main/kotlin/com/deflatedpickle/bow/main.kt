package com.deflatedpickle.bow

import com.deflatedpickle.bow.notepad.BetterNotepad
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef

fun main(args: Array<String>) {
    User32.INSTANCE.EnumWindows({ hWnd: WinDef.HWND, _: Pointer? ->
        val title = WindowUtil.getTitle(hWnd)

        if (title !in listOf("")
                && User32.INSTANCE.IsWindowVisible(hWnd)
                && User32.INSTANCE.IsWindowEnabled(hWnd)) {
            when (WindowUtil.getClass(hWnd)) {
                "Notepad" -> {
                    println("Launching Notepad Improvements")

                    var editWidget: WinDef.HWND? = null
                    var statusBarWidget: WinDef.HWND? = null

                    User32.INSTANCE.EnumChildWindows(hWnd, { childHWnd: WinDef.HWND, _: Pointer? ->
                        when (WindowUtil.getClass(childHWnd)) {
                            "Edit" -> editWidget = childHWnd
                            "msctls_statusbar32" -> statusBarWidget = childHWnd
                        }

                        true
                    }, null)

                    BetterNotepad(hWnd, editWidget, statusBarWidget)
                }
            }
        }

        true
    }, null)
}