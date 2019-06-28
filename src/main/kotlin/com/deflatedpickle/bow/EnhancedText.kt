package com.deflatedpickle.bow

import org.eclipse.swt.SWT
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.events.KeyAdapter
import org.eclipse.swt.events.KeyEvent
import org.eclipse.swt.widgets.Composite
import java.util.*
import javax.swing.KeyStroke

// Credit: http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/SWTUndoRedo.htm
class EnhancedText(parent: Composite, style: Int) : StyledText(parent, style) {
    val stackSize = 60

    val undoStack = LinkedList<String>()
    val redoStack = LinkedList<String>()

    init {
        this.addExtendedModifyListener {
            val currentText = this.text
            val newText = currentText.substring(it.start, it.start + it.length)
            if (newText.isNotEmpty()) {
                if (undoStack.size == stackSize) {
                    undoStack.removeAt(undoStack.size - 1)
                }
                undoStack.add(0, newText)
            }
        }

        this.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.stateMask and SWT.CTRL != 0) {
                    when (e.keyCode.toChar()) {
                        'z' -> undo()
                        'y' -> redo()
                    }
                }
            }
        })
    }

    fun undo() {
        if (undoStack.size > 0) {
            val lastEdit = undoStack.removeAt(0)
            val editLength = lastEdit.length
            val currentText = this.text
            val startReplaceIndex = currentText.length - editLength
            this.replaceTextRange(startReplaceIndex, editLength, "")
            redoStack.add(0, lastEdit)
        }
    }

    fun redo() {
        if (redoStack.size > 0) {
            moveCursorToEnd()
            this.append(redoStack.removeAt(0))
            moveCursorToEnd()
        }
    }

    private fun moveCursorToEnd() {
        this.caretOffset = this.text.length
    }
}