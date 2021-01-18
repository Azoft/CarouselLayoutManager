package com.azoft.carousellayoutmanager;

import android.graphics.PointF;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * Custom implementation of {@link RecyclerView.SmoothScroller} that can work only with {@link CarouselLayoutManager}.
 *
 * @see CarouselLayoutManager
 */
public class CarouselSmoothScroller {

    public CarouselSmoothScroller(@NonNull final RecyclerView.State state, final int position) {
        if (0 > position) {
            throw new IllegalArgumentException("position can't be less then 0. position is : " + position);
        }
        if (position >= state.getItemCount()) {
            throw new IllegalArgumentException("position can't be great then adapter items count. position is : " + position);
        }
    }

    @SuppressWarnings("unused")
    public PointF computeScrollVectorForPosition(final int targetPosition, @NonNull final CarouselLayoutManager carouselLayoutManager) {
        return carouselLayoutManager.computeScrollVectorForPosition(targetPosition);
    }

    @SuppressWarnings("unused")
    public int calculateDyToMakeVisible(final View view, @NonNull final CarouselLayoutManager carouselLayoutManager) {
        if (!carouselLayoutManager.canScrollVertically()) {
            return 0;
        }

        return carouselLayoutManager.getOffsetForCurrentView(view);
    }

    @SuppressWarnings("unused")
    public int calculateDxToMakeVisible(final View view, @NonNull final CarouselLayoutManager carouselLayoutManager) {
        if (!carouselLayoutManager.canScrollHorizontally()) {
            return 0;
        }
        return carouselLayoutManager.getOffsetForCurrentView(view);
    }
}