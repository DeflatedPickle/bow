package com.deflatedpickle.bow.notepad

import com.deflatedpickle.bow.EnhancedText
import com.deflatedpickle.bow.User32Extended
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.graphics.Font
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.internal.win32.NONCLIENTMETRICSW
import org.eclipse.swt.internal.win32.OS
import org.eclipse.swt.internal.win32.SCROLLINFO
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.*
import org.eclipse.swt.widgets.List
import java.util.concurrent.atomic.AtomicReference

// TODO: Add a built-in terminal and run buttons
// TODO: Add a history pane
// TODO: Add a birds-eye-view widget
class BetterNotepad(parent: WinDef.HWND, val edit: WinDef.HWND?, val statusBar: WinDef.HWND?) : Shell(Display.getDefault(), SWT.POP_UP or SWT.ON_TOP) {
    val editPointer: Long
    val statusBarPointer: Long
    val win32Font: Long
    val swtFont: Font

    val tabFolder: TabFolder
    // val closeIcon: Image

    lateinit var newTabButton: Button
    val currentTextWidget: AtomicReference<EnhancedText> = AtomicReference()

    init {
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                println("Returning Notepad widgets to parent")
                User32.INSTANCE.SetParent(edit, parent)
                User32.INSTANCE.SetParent(statusBar, parent)
            }
        })

        val shellHandle = WinDef.HWND(Pointer(shell.handle))
        // TODO: Get the small close icon from the theme
        // val theme = UxTheme.INSTANCE.OpenThemeData(shellHandle, "WINDOW")
        // val hbitmap = Memory(Native.getNativeSize(Pointer::class.java).toLong())
        // UxTheme.INSTANCE.GetThemeBitmap(theme, 19, 3, 0, UxTheme.GBF_DIRECT, hbitmap)
        // FIXME: Colour Depth error when accessing Image#imageData
        // closeIcon = Image.win32_new(shell.display, SWT.ICON, Pointer.nativeValue(hbitmap))

        layout = GridLayout().apply {
            marginWidth = 0
            marginHeight = 0
        }

        User32.INSTANCE.SetParent(shellHandle, parent)
        val sizeRect = WinDef.RECT()
        User32.INSTANCE.GetClientRect(parent, sizeRect)

        shell.setLocation(0, 0)
        shell.setSize(sizeRect.right, sizeRect.bottom)

        editPointer = Pointer.nativeValue(edit!!.pointer)
        statusBarPointer = Pointer.nativeValue(statusBar!!.pointer)

        val nonClientMetrics = NONCLIENTMETRICSW()
        OS.SystemParametersInfoW(OS.SPI_GETNONCLIENTMETRICS, nonClientMetrics.cbSize, nonClientMetrics, 0)

        win32Font = OS.CreateFontIndirectW(nonClientMetrics.lfCaptionFont)
        swtFont = Font.win32_new(Display.getDefault(), win32Font)

        var selectStart = -1L
        var selectEnd = -1L

        createCoolbar()

        tabFolder = TabFolder(shell, SWT.NONE).apply {
            layoutData = GridData(GridData.FILL, GridData.FILL, true, true)

            addListener(SWT.Selection) {
                if (this.selection[0].control != null) {
                    val currentTab = this.selection[0].control as Composite
                    currentTextWidget.set(currentTab.children[1] as EnhancedText)
                    currentTextWidget.get().setFocus()
                }
            }
        }
        tabFolder.pack()

        newTabButton = Button(shell, SWT.PUSH).apply {
            text = "+"
            layoutData = GridData().apply { exclude = true }

            addListener(SWT.Selection) {
                newTab()
            }
        }

        newTab()
        tabFolder.setSelection(0)

        val lastTab = tabFolder.getItem(tabFolder.itemCount - 1)
        newTabButton.setLocation(lastTab.bounds.x + lastTab.bounds.width + 1, lastTab.bounds.y + lastTab.bounds.height + 5)
        currentTextWidget.set((tabFolder.selection[0].control as NotepadTab).text as EnhancedText)
        currentTextWidget.get().setFocus()

        newTabButton.pack()
        newTabButton.moveAbove(tabFolder)

        shell.layout()
        shell.open()

        var selectedLine = -1

        while (!shell.isDisposed) {
            if (!display.readAndDispatch()) {
                display.sleep()
            }
        }
    }

    fun newTab(): TabItem {
        val tabItem = TabItem(tabFolder, SWT.NONE).apply {
            text = "New ${tabFolder.itemCount}"
            // image = Image(display, display.getSystemImage(SWT.ICON_ERROR).imageData.scaledTo(16, 16))

            // TODO: Add a paint listener to draw a close button
            // FIXME: This close button draws under the tab, figure out how to draw it on-top
            // addPaintListener {
            //     it.gc.drawImage(Image(display, display.getSystemImage(SWT.ICON_ERROR).imageData.scaledTo(32, 32)), 5, 5)
            // }

            control = NotepadTab(tabFolder)
        }

        tabFolder.setSelection(tabFolder.selectionIndex + 1)

        val lastTab = tabFolder.getItem(tabFolder.itemCount - 1)
        newTabButton.setLocation(lastTab.bounds.x + lastTab.bounds.width + 1, lastTab.bounds.y + lastTab.bounds.height + 5)
        currentTextWidget.set((tabFolder.selection[0].control as NotepadTab).text as EnhancedText)
        currentTextWidget.get().setFocus()

        return tabItem
    }

    fun createCoolbar() {
        // TODO: Add the items to menus (http://www.java2s.com/Tutorial/Java/0280__SWT/CoolBardropdownachevronmenucontaininghiddentoolitems.htm)
        CoolBar(shell, SWT.NONE).apply {
            val coolBar = this
            layoutData = GridData(GridData.FILL, GridData.BEGINNING, true, false, 2, 0)

            CoolItem(this, SWT.BORDER or SWT.DROP_DOWN).apply {
                ToolBar(coolBar, SWT.NONE).apply {
                    ToolItem(this, SWT.PUSH).apply {
                        text = "New"
                        addListener(SWT.Selection) {
                            newTab()
                        }
                    }
                }.also {
                    control = it
                    calcSize(this)
                }
            }

            CoolItem(this, SWT.BORDER or SWT.DROP_DOWN).apply {
                ToolBar(coolBar, SWT.NONE).apply {
                    ToolItem(this, SWT.PUSH).apply {
                        text = "Cut"
                        addListener(SWT.Selection) {
                            currentTextWidget.get().cut()
                        }
                    }
                    ToolItem(this, SWT.PUSH).apply {
                        text = "Copy"
                        addListener(SWT.Selection) {
                            currentTextWidget.get().copy()
                        }
                    }
                    ToolItem(this, SWT.PUSH).apply {
                        text = "Paste"
                        addListener(SWT.Selection) {
                            currentTextWidget.get().paste()
                        }
                    }
                }.also {
                    control = it
                    calcSize(this)
                }
            }

            CoolItem(this, SWT.BORDER or SWT.DROP_DOWN).apply {
                ToolBar(coolBar, SWT.NONE).apply {
                    ToolItem(this, SWT.PUSH).apply {
                        text = "Undo"
                        addListener(SWT.Selection) {
                            currentTextWidget.get().undo()
                        }
                    }
                    ToolItem(this, SWT.PUSH).apply {
                        text = "Redo"
                        addListener(SWT.Selection) {
                            currentTextWidget.get().redo()
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

    fun captureEdit(parent: Composite) {
        // Takes a hold of the Edit widget and puts it in the grid
        // val editComposite = Composite(parent, SWT.BORDER or SWT.EMBEDDED).apply {
        //     layoutData = GridData(GridData.FILL, GridData.FILL, true, true)
        // }
        // val editCompositeHandle = WinDef.HWND(Pointer(editComposite.handle))
        // User32.INSTANCE.SetParent(edit, editCompositeHandle)
        // OS.SendMessage(editPointer, OS.WM_SETFONT, win32Font, 0)
    }

    fun captureStatusBar() {
        // Takes a hold of the StatusBar widget and puts it in the grid
        val statusBarComposite = Composite(shell, SWT.EMBEDDED).apply {
            layoutData = GridData(GridData.FILL, GridData.FILL, true, false).apply {
                heightHint = 22
            }
        }
        val statusBarCompositeHandle = WinDef.HWND(Pointer(statusBarComposite.handle))
        User32.INSTANCE.SetParent(statusBar, statusBarCompositeHandle)
    }

    fun lowByte(long: Long): Long {
        return long and 0xFF
    }

    fun highByte(long: Long): Long {
        return (long shr 8) and 0xFF
    }

    override fun checkSubclass() {
    }
}