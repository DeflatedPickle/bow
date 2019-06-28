package com.deflatedpickle.bow.notepad

import com.deflatedpickle.bow.EnhancedText
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.List

class NotepadTab(parent: Composite) : Composite(parent, SWT.NONE) {
    val list: List
    lateinit var text: StyledText

    var cachedLines = -1

    init {
        this.layout = GridLayout(2, false).apply {
            marginWidth = 0
            marginHeight = 0
        }

        list = List(this, SWT.BORDER).apply {
            layoutData = GridData(GridData.FILL, GridData.FILL, false, true).apply {
                this.widthHint = 26
            }

            addListener(SWT.Selection) {
                val line = text.getOffsetAtLine(this.selectionIndex)
                val nextLine = text.getOffsetAtLine(this.selectionIndex + 1)
                text.setSelection(line, nextLine - 2)
                text.setFocus()
            }
        }

        text = EnhancedText(this, SWT.BORDER or SWT.MULTI or SWT.H_SCROLL or SWT.V_SCROLL).apply {
            layoutData = GridData(GridData.FILL, GridData.FILL, true, true).apply {
                this.verticalSpan = 2
            }
            font = list.font

            addListener(SWT.Modify) {
                val currentLines = text.lines().size
                if (currentLines != cachedLines) {
                    list.removeAll()
                    for (i in 1..currentLines) {
                        list.add("$i")
                    }

                    list.setSelection(this.getLineAtOffset(this.caretOffset))
                    cachedLines = currentLines
                }
            }

            addCaretListener {
                list.setSelection(this.getLineAtOffset(this.caretOffset))
            }

            this.verticalBar.addListener(SWT.Selection) {
                list.topIndex = this.topIndex
            }

            insert("")
        }

        Composite(this, SWT.NONE).apply {
            layoutData = GridData().apply {
                this.widthHint = 26
                this.heightHint = text.verticalBar.thumbTrackBounds.width - (this@NotepadTab.layout as GridLayout).verticalSpacing
            }
        }
    }
}