package com.deflatedpickle.bow

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.Font
import org.eclipse.swt.internal.win32.*
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.*
import org.eclipse.swt.widgets.List
import org.eclipse.jface.util.Geometry.setSize
import org.eclipse.swt.widgets.CoolItem



class BetterNotepad(parent: WinDef.HWND, val edit: WinDef.HWND?, val statusBar: WinDef.HWND?) : Shell(Display.getDefault(), SWT.POP_UP or SWT.ON_TOP) {
    val editPointer: Long
    val win32Font: Long
    val swtFont: Font

    init {
        layout = GridLayout(2, false).apply {
            marginWidth = 0
            marginHeight = 0
        }

        val shellHandle = WinDef.HWND(Pointer(shell.handle))
        User32.INSTANCE.SetParent(shellHandle, parent)
        val sizeRect = WinDef.RECT()
        User32.INSTANCE.GetClientRect(parent, sizeRect)

        shell.setLocation(0, 0)
        shell.setSize(sizeRect.right, sizeRect.bottom)

       editPointer = Pointer.nativeValue(edit!!.pointer)

        val nonClientMetrics = NONCLIENTMETRICSW()
        OS.SystemParametersInfoW(OS.SPI_GETNONCLIENTMETRICS, nonClientMetrics.cbSize, nonClientMetrics, 0)

        win32Font = OS.CreateFontIndirectW(nonClientMetrics.lfCaptionFont)
        swtFont = Font.win32_new(Display.getDefault(), win32Font)

        var selectStart = -1L
        var selectEnd = -1L

        createCoolbar()
        val list = createLineNumbers()
        captureEdit()
        captureStatusBar()

        shell.layout()
        shell.open()

        while (!shell.isDisposed) {
            // Update the line numbers
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

            // Scroll the line numbers
            val scrollInfo = SCROLLINFO()
            scrollInfo.cbSize = OS.SCROLLINFO_sizeof()
            scrollInfo.fMask = OS.SIF_POS

            // TODO: The line numbers should scroll with the Edit widget
            // FIXME: Only the scrollbar moves, the list it's connected to doesn't
            OS.GetScrollInfo(editPointer, OS.SB_VERT, scrollInfo)
            OS.SetScrollInfo(list.handle, OS.SB_VERT, scrollInfo, true)
            // println("${scrollInfo.nPos} ${scrollInfo.nMin}, ${scrollInfo.nMax}, ${scrollInfo.nTrackPos}")

            if (!display.readAndDispatch()) {
                display.sleep()
            }
        }
    }

    fun createCoolbar() {
        CoolBar(this, SWT.NONE).apply {
            val coolBar = this
            layoutData = GridData(GridData.FILL, GridData.BEGINNING, true, false, 2, 0)

            CoolItem(this, SWT.BORDER).apply {
                ToolBar(coolBar, SWT.NONE).apply {
                    ToolItem(this, SWT.PUSH).apply {
                        text = "Cut"
                        addListener(SWT.Selection) {
                            OS.SendMessage(editPointer, OS.WM_CUT, 0, 0)
                        }
                    }
                    ToolItem(this, SWT.PUSH).apply {
                        text = "Copy"
                        addListener(SWT.Selection) {
                            OS.SendMessage(editPointer, OS.WM_COPY, 0, 0)
                        }
                    }
                    ToolItem(this, SWT.PUSH).apply {
                        text = "Paste"
                        addListener(SWT.Selection) {
                            OS.SendMessage(editPointer, OS.WM_PASTE, 0, 0)
                        }
                    }
                }.also {
                    control = it
                    calcSize(this)
                }
            }
        }
    }

    private fun calcSize(item: CoolItem) {
        val control = item.control
        var pt = control.computeSize(SWT.DEFAULT, SWT.DEFAULT)
        pt = item.computeSize(pt.x, pt.y)
        item.size = pt
    }

    fun createLineNumbers(): List {
        return List(shell, SWT.BORDER or SWT.V_SCROLL).apply {
            font = swtFont
            layoutData = GridData(GridData.BEGINNING, GridData.FILL, false, true).apply {
                widthHint = 20
            }

            addListener(SWT.Selection) {
                // TODO: Clicking on a number should select that line
                val index = this.selectionIndex.toLong()
                // OS.SetFocus(editPointer)
                // OS.SendMessage(editPointer, OS.EM_SETSEL, index, index)
            }
        }
    }

    fun captureEdit() {
        // Takes a hold of the Edit widget and puts it in the grid
        val editComposite = Composite(shell, SWT.BORDER or SWT.EMBEDDED).apply {
            layoutData = GridData(GridData.FILL, GridData.FILL, true, true)
        }
        val editCompositeHandle = WinDef.HWND(Pointer(editComposite.handle))
        User32.INSTANCE.SetParent(edit, editCompositeHandle)
        OS.SendMessage(editPointer, OS.WM_SETFONT, win32Font, 0)
    }

    fun captureStatusBar() {
        // Takes a hold of the StatusBar widget and puts it in the grid
        val statusBarComposite = Composite(shell, SWT.EMBEDDED).apply {
            layoutData = GridData(GridData.FILL, GridData.END, true, false, 2, 0).apply {
                heightHint = 22
            }
        }
        val statusBarCompositeHandle = WinDef.HWND(Pointer(statusBarComposite.handle))
        User32.INSTANCE.SetParent(statusBar, statusBarCompositeHandle)
    }

    override fun checkSubclass() {
    }
}