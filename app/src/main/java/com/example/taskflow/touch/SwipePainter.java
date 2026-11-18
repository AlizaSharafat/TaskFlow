package com.example.taskflow.touch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.example.taskflow.R;

/**
 * Paints the coloured strip a row slides away from during a swipe.
 *
 * ItemTouchHelper only moves the row: whatever is behind it is the plain
 * screen background unless something is drawn there. Without this the gesture
 * is invisible until it completes, and the user has no way to tell whether
 * they are about to finish a task or delete one. The icon only appears once
 * the row has moved far enough to make room for it.
 */
public class SwipePainter {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF area = new RectF();

    private final Drawable leftIcon;
    private final Drawable rightIcon;
    private final int leftColour;
    private final int rightColour;
    private final float radius;
    private final int inset;
    private final int iconSize;

    /**
     * @param leftIconRes    drawn when the row is swiped to the left
     * @param leftColourRes  colour revealed when the row is swiped to the left
     * @param rightIconRes   drawn when the row is swiped to the right
     * @param rightColourRes colour revealed when the row is swiped to the right
     */
    public SwipePainter(Context context, int leftIconRes, int leftColourRes,
                        int rightIconRes, int rightColourRes) {
        this.leftIcon = tinted(context, leftIconRes);
        this.rightIcon = tinted(context, rightIconRes);
        this.leftColour = ContextCompat.getColor(context, leftColourRes);
        this.rightColour = ContextCompat.getColor(context, rightColourRes);
        this.radius = context.getResources().getDimension(R.dimen.card_radius);
        this.inset = dp(context, 18);
        this.iconSize = dp(context, 22);
    }

    /** Tints a copy of the icon white, so it reads against the colour behind it. */
    private static Drawable tinted(Context context, int iconRes) {
        Drawable icon = ContextCompat.getDrawable(context, iconRes);
        if (icon == null) {
            return null;
        }
        Drawable copy = DrawableCompat.wrap(icon.mutate());
        DrawableCompat.setTint(copy, ContextCompat.getColor(context, R.color.white));
        return copy;
    }

    private static int dp(Context context, int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                context.getResources().getDisplayMetrics());
    }

    /**
     * @param dX how far the row has been dragged: negative is left, positive
     *           is right, zero means the row is back in place
     */
    public void paint(Canvas canvas, View row, float dX) {
        if (dX == 0f) {
            return;
        }
        boolean swipingLeft = dX < 0;
        paint.setColor(swipingLeft ? leftColour : rightColour);

        // The row's own bounds, so the strip lines up with the card exactly.
        if (swipingLeft) {
            area.set(row.getRight() + dX, row.getTop(), row.getRight(), row.getBottom());
        } else {
            area.set(row.getLeft(), row.getTop(), row.getLeft() + dX, row.getBottom());
        }
        canvas.drawRoundRect(area, radius, radius, paint);

        Drawable icon = swipingLeft ? leftIcon : rightIcon;
        if (icon == null || Math.abs(dX) < inset + iconSize) {
            return;
        }

        int top = row.getTop() + (row.getHeight() - iconSize) / 2;
        int left = swipingLeft
                ? row.getRight() - inset - iconSize
                : row.getLeft() + inset;
        icon.setBounds(left, top, left + iconSize, top + iconSize);
        icon.draw(canvas);
    }
}
