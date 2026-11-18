package com.example.taskflow.touch;

import android.content.Context;
import android.graphics.Canvas;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskflow.R;
import com.example.taskflow.adapter.AttachmentAdapter;
import com.example.taskflow.model.AttachmentLink;

/**
 * The swipe rules for the attachment list.
 *
 * Only one gesture exists here: a swipe to the right asks to remove the
 * attachment. There is no drag, because attachments stay in the order they
 * were linked, and no left swipe, because there is nothing to complete.
 */
public class AttachmentTouchCallback extends ItemTouchHelper.Callback {

    public interface Listener {
        /** Swiped right: ask for the name before anything is unlinked. */
        void onRemoveSwipe(int position, AttachmentLink link);
    }

    private final AttachmentAdapter adapter;
    private final Listener listener;
    private final SwipePainter painter;

    public AttachmentTouchCallback(Context context, AttachmentAdapter adapter, Listener listener) {
        this.adapter = adapter;
        this.listener = listener;
        // The same red and the same bin on both sides: only one side is ever
        // reachable, because the movement flags below allow RIGHT alone.
        this.painter = new SwipePainter(context,
                R.drawable.ic_trash, R.color.danger,
                R.drawable.ic_trash, R.color.danger);
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, ItemTouchHelper.RIGHT);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getBindingAdapterPosition();
        AttachmentLink link = adapter.linkAt(position);
        if (link == null) {
            return;
        }
        listener.onRemoveSwipe(position, link);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public void onChildDraw(@NonNull Canvas canvas, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            painter.paint(canvas, viewHolder.itemView, dX);
        }
        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState,
                isCurrentlyActive);
    }
}
