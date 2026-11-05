package com.example.taskflow.util;

import android.view.View;
import android.widget.PopupMenu;

import java.util.List;

/**
 * Shows a short dropdown under a view and reports which entry was picked.
 * Used by the Status, Priority, Date and Sort pills in the Tasks tab.
 */
public final class Menus {

    private Menus() {
        // Utility class, never instantiated.
    }

    /** Java has no function type, so the callback is a small interface. */
    public interface OnPicked {
        void onPicked(int index);
    }

    public static void show(View anchor, List<String> labels, final OnPicked callback) {
        PopupMenu popup = new PopupMenu(anchor.getContext(), anchor);
        for (int i = 0; i < labels.size(); i++) {
            popup.getMenu().add(0, i, i, labels.get(i));
        }
        popup.setOnMenuItemClickListener(item -> {
            callback.onPicked(item.getItemId());
            return true;
        });
        popup.show();
    }
}
