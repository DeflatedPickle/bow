package com.deflatedpickle.bow

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.Font
import org.eclipse.swt.internal.win32.MSG
import org.eclipse.swt.internal.win32.NONCLIENTMETRICSW
import org.eclipse.swt.internal.win32.OS
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.List
import org.eclipse.swt.widgets.Shell

fun notepadFixerUpper(parent: WinDef.HWND, edit: WinDef.HWND?, statusBar: WinDef.HWND?) {
    // TODO: Add a CoolBar
    // TODO: Add tabs

    val display = Display.getDefault()
    val shell = Shell(display, SWT.POP_UP or SWT.ON_TOP)
    shell.layout = GridLayout(2, false).apply {
        marginWidth = 0
        marginHeight = 0
    }

    val shellHandle = WinDef.HWND(Pointer(shell.handle))
    User32.INSTANCE.SetParent(shellHandle, parent)
    val sizeRect = WinDef.RECT()
    User32.INSTANCE.GetClientRect(parent, sizeRect)

    shell.setLocation(0, 0)
    shell.setSize(sizeRect.right, sizeRect.bottom)

    val editPointer = Pointer.nativeValue(edit!!.pointer)

    val nonClientMetrics = NONCLIENTMETRICSW()
    OS.SystemParametersInfoW(OS.SPI_GETNONCLIENTMETRICS, nonClientMetrics.cbSize, nonClientMetrics, 0)

    val win32Font = OS.CreateFontIndirectW(nonClientMetrics.lfCaptionFont)
    val swtFont = Font.win32_new(Display.getDefault(), win32Font)

    val list = List(shell, SWT.BORDER).apply {
        font = swtFont
        layoutData = GridData(GridData.BEGINNING, GridData.FILL, false, true).apply {
            widthHint = 20
        }
    }
    var selectStart = -1L
    var selectEnd = -1L

    // Takes a hold of the Edit widget and puts it in the grid
    val editComposite = Composite(shell, SWT.BORDER or SWT.EMBEDDED).apply {
        layoutData = GridData(GridData.FILL, GridData.FILL, true, true)
    }
    val editCompositeHandle = WinDef.HWND(Pointer(editComposite.handle))
    User32.INSTANCE.SetParent(edit, editCompositeHandle)
    OS.SendMessage(editPointer, OS.WM_SETFONT, win32Font, 0)

    // Takes a hold of the StatusBar widget and puts it in the grid
    val statusBarComposite = Composite(shell, SWT.EMBEDDED).apply {
        layoutData = GridData(GridData.FILL, GridData.END, true, false, 2, 0).apply {
            heightHint = 22
        }
    }
    val statusBarCompositeHandle = WinDef.HWND(Pointer(statusBarComposite.handle))
    User32.INSTANCE.SetParent(statusBar, statusBarCompositeHandle)

    shell.layout()
    shell.open()

    val msg = MSG()
    while (!shell.isDisposed) {
        val selection = OS.SendMessage(editPointer, OS.EM_GETSEL, 0, 0)
        val tempStart = OS.LOWORD(selection).toLong()
        val tempEnd = OS.HIWORD(selection).toLong()

        if (tempStart != selectStart) {
            val lineCount = OS.SendMessage(editPointer, OS.EM_GETLINECOUNT, 0, 0).toInt()
            list.removeAll()
            for (i in 1..lineCount) {
                list.add("$i")
            }
        }
        selectStart = tempStart
        selectEnd = tempEnd

        if (!display.readAndDispatch()) {
            display.sleep()
        }
    }
}

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

                    notepadFixerUpper(hWnd, editWidget, statusBarWidget)
                }
            }
        }

        true
    }, null)
}