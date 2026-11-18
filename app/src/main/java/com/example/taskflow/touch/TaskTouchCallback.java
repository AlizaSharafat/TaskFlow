package com.example.taskflow.touch;

import android.content.Context;
import android.graphics.Canvas;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskflow.R;
import com.example.taskflow.adapter.TaskAdapter;
import com.example.taskflow.model.TaskItem;
import com.example.taskflow.model.TaskRow;

/**
 * The swipe and drag rules for the grouped task list.
 *
 * ItemTouchHelper asks this class three things: which gestures a row allows,
 * whether a drag may land where the finger is, and what to do when a gesture
 * finishes. Everything the rules depend on comes from the row under the finger,
 * which is why the adapter is asked for it rather than the raw position being
 * trusted.
 */
public class TaskTouchCallback extends ItemTouchHelper.Callback {

    /** What the fragment has to decide, because it owns the project. */
    public interface Listener {

        /** Swiped left: finish the task, or say it is already finished. */
        void onCompleteSwipe(int position, TaskItem task);

        /** Swiped right: ask before anything is removed. */
        void onDeleteSwipe(int position, TaskItem task);

        /** A row passed over another during a drag: commit the new order. */
        void onTaskMoved(TaskItem moving, TaskItem target);

        /** The finger lifted after a drag, so the list can be written back. */
        void onDragFinished();
    }

    private final TaskAdapter adapter;
    private final Listener listener;
    private final SwipePainter painter;

    /** True once a drag has actually moved something, so a tap does nothing. */
    private boolean moved = false;

    public TaskTouchCallback(Context context, TaskAdapter adapter, Listener listener) {
        this.adapter = adapter;
        this.listener = listener;
        this.painter = new SwipePainter(context,
                R.drawable.ic_check_circle, R.color.success,
                R.drawable.ic_trash, R.color.danger);
    }

    /**
     * Headers are labels, not content, so they get no gestures at all.
     * Returning zero flags is what makes a row immovable: there is no separate
     * "disable" call, the row simply allows nothing.
     */
    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder) {
        TaskRow row = adapter.rowAt(viewHolder.getBindingAdapterPosition());
        if (row == null || row.isHeader()) {
            return makeMovementFlags(0, 0);
        }
        int drag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipe = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        return makeMovementFlags(drag, swipe);
    }

    /**
     * Called every time the dragged row passes another one. Returning false
     * leaves the list untouched, which is how a task is kept inside its own
     * group: it may change places with a task under the same heading and with
     * nothing else.
     */
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        int from = viewHolder.getBindingAdapterPosition();
        int to = target.getBindingAdapterPosition();
        TaskRow fromRow = adapter.rowAt(from);
        TaskRow toRow = adapter.rowAt(to);

        if (fromRow == null || toRow == null) {
            return false;
        }
        if (fromRow.isHeader() || toRow.isHeader()) {
            return false;
        }
        if (fromRow.getGroup() != toRow.getGroup()) {
            return false;
        }

        listener.onTaskMoved(fromRow.getTask(), toRow.getTask());
        adapter.moveRow(from, to);
        moved = true;
        return true;
    }

    /**
     * Stops the dragged row from even hovering over a header or over another
     * group, so the gesture looks blocked rather than simply doing nothing.
     */
    @Override
    public boolean canDropOver(@NonNull RecyclerView recyclerView,
                               @NonNull RecyclerView.ViewHolder current,
                               @NonNull RecyclerView.ViewHolder target) {
        TaskRow currentRow = adapter.rowAt(current.getBindingAdapterPosition());
        TaskRow targetRow = adapter.rowAt(target.getBindingAdapterPosition());
        if (currentRow == null || targetRow == null) {
            return false;
        }
        if (currentRow.isHeader() || targetRow.isHeader()) {
            return false;
        }
        return currentRow.getGroup() == targetRow.getGroup();
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getBindingAdapterPosition();
        TaskRow row = adapter.rowAt(position);
        if (row == null || row.isHeader()) {
            return;
        }
        if (direction == ItemTouchHelper.LEFT) {
            listener.onCompleteSwipe(position, row.getTask());
        } else {
            listener.onDeleteSwipe(position, row.getTask());
        }
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    /**
     * The last thing ItemTouchHelper calls once a gesture is over. The order
     * is written back here rather than on every single pass, so the list is
     * rebuilt once instead of on every frame of the drag.
     */
    @Override
    public void clearView(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        if (moved) {
            moved = false;
            listener.onDragFinished();
        }
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
