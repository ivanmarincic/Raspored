package com.idiotnation.raspored.custom;

import android.graphics.Canvas;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HeaderItemDecoration extends RecyclerView.ItemDecoration {

    private StickyHeaderInterface listener;
    private int stickyHeaderHeight;
    private float stickyHeaderTextSize;
    private SparseArray<View> cachedHeaders = new SparseArray<>();

    public HeaderItemDecoration(RecyclerView recyclerView, @NonNull StickyHeaderInterface listener) {
        this.listener = listener;

        // On Sticky Header Click
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
                return motionEvent.getY() <= stickyHeaderHeight;
            }

            public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {

            }

            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        View previousHeader = null;
        View topChild = parent.getChildAt(0);
        int topHeader = 0;
        if (topChild != null) {
            topHeader = listener.getHeaderPositionForItem(parent.getChildAdapterPosition(topChild));
        }
        for (int i = parent.getChildCount() - 1; i >= 0; i--) {
            View view = parent.getChildAt(i);
            if (view == null) {
                continue;
            }
            int position = parent.getChildAdapterPosition(view);
            if (position == RecyclerView.NO_POSITION) {
                continue;
            }
            int headerPosition = listener.getHeaderPositionForItem(position);
            if (headerPosition >= 0) {
                View header = getHeaderViewForItem(headerPosition, parent);
                if (topHeader == headerPosition) {
                    if (previousHeader != null) {
                        if (previousHeader.getTop() < header.getBottom()) {
                            moveHeader(c, header, previousHeader);
                            continue;
                        }
                    }
                    drawHeader(c, header);
                } else {
                    View parentHeader = getParentHeader(parent, headerPosition);
                    drawHeaderAt(c, header, parentHeader);
                    previousHeader = parentHeader;
                }
            }
        }
    }

    private void drawHeader(Canvas c, View header) {
        c.save();
        c.translate(0, 0);
        header.draw(c);
        c.restore();
    }

    private void drawHeaderAt(Canvas c, View header, View parentHeader) {
        if (parentHeader != null) {
            c.save();
            c.translate(0, Math.max(0, parentHeader.getTop()));
            header.draw(c);
            c.restore();
        }
    }

    private void moveHeader(Canvas c, View currentHeader, View nextHeader) {
        c.save();
        c.translate(0, Math.max(nextHeader.getTop() - currentHeader.getHeight(), -currentHeader.getHeight()));
        currentHeader.draw(c);
        c.restore();
    }

    private View getHeaderViewForItem(int headerPosition, RecyclerView parent) {
        View convertView = cachedHeaders.get(headerPosition);
        if (convertView == null) {
            int layoutResId = listener.getHeaderLayout(headerPosition);
            convertView = LayoutInflater.from(parent.getContext()).inflate(layoutResId, parent, false);
            cachedHeaders.put(headerPosition, convertView);
            fixLayoutSize(parent, convertView);
            fixTextSize(parent, listener.getHeaderTextView(convertView));
        }
        listener.bindHeaderData(convertView, headerPosition);
        return convertView;
    }

    private View getChildInContact(RecyclerView parent, int contactPoint) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child.getBottom() > contactPoint) {
                if (child.getTop() <= contactPoint) {
                    // This child overlaps the contactPoint
                    return child;
                }
            }
        }
        return null;
    }

    private View getParentHeader(RecyclerView parent, int headerPosition) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (parent.getChildAdapterPosition(child) == headerPosition) {
                return child;
            }
        }
        return null;
    }

    /**
     * Properly measures and layouts the top sticky header.
     *
     * @param parent ViewGroup: RecyclerView in this case.
     */
    private void fixLayoutSize(ViewGroup parent, View view) {

        // Specs for parent (RecyclerView)
        int widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.UNSPECIFIED);

        // Specs for children (headers)
        int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec, parent.getPaddingLeft() + parent.getPaddingRight(), view.getLayoutParams().width);
        int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec, parent.getPaddingTop() + parent.getPaddingBottom(), view.getLayoutParams().height);

        view.measure(childWidthSpec, childHeightSpec);

        view.layout(0, 0, view.getMeasuredWidth(), stickyHeaderHeight = view.getMeasuredHeight());
    }

    private void fixTextSize(ViewGroup parent, View view) {
        ((TextView) view).setTextSize(stickyHeaderTextSize = ((view.getMeasuredHeight() - view.getPaddingTop() - view.getPaddingBottom()) / 2.5f / parent.getContext().getResources().getDisplayMetrics().scaledDensity));
    }

    public interface StickyHeaderInterface {

        int getHeaderPositionForItem(int itemPosition);

        int getHeaderLayout(int headerPosition);

        View getHeaderTextView(View header);

        void bindHeaderData(View header, int headerPosition);
    }
}