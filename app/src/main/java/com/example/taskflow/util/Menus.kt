package com.example.taskflow.util

import android.view.View
import android.widget.PopupMenu

/**
 * Shows a short dropdown under a view and reports which entry was picked.
 * Used by the Status, Priority, Date and Sort pills.
 */
object Menus {

    fun show(anchor: View, labels: List<String>, onPick: (Int) -> Unit) {
        val popup = PopupMenu(anchor.context, anchor)
        labels.forEachIndexed { index, label ->
            popup.menu.add(0, index, index, label)
        }
        popup.setOnMenuItemClickListener { item ->
            onPick(item.itemId)
            true
        }
        popup.show()
    }
}
