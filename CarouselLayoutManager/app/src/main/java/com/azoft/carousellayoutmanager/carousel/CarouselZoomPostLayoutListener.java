package com.azoft.carousellayoutmanager.carousel;

import android.support.annotation.NonNull;
import android.view.View;

/**
 * Implementation of {@link CarouselLayoutManager.PostLayoutListener} that makes interesting scaling of items. <br />
 * We are trying to make items scaling quicker for closer items for center and slower for when they are far away.<br />
 * Tis implementation uses atan function for this purpose.
 */
public class CarouselZoomPostLayoutListener implements CarouselLayoutManager.PostLayoutListener {

    @Override
    public void transformChild(@NonNull final View child, final float itemPositionToCenterDiff, final int orientation) {
        final float scale = (float) (4 * (2 * -StrictMath.atan(Math.abs(itemPositionToCenterDiff) + 1.0) / Math.PI + 1) / 2);

        // because scaling will make view smaller in its center, then we should move this item to the top or bottom to make it visible
        if (CarouselLayoutManager.VERTICAL == orientation) {
            final float translateY = child.getHeight() * (1 - scale) / 2f;
            child.setTranslationY(Math.signum(itemPositionToCenterDiff) * translateY);
        } else {
            final float translateX = child.getWidth() * (1 - scale) / 2f;
            child.setTranslationX(Math.signum(itemPositionToCenterDiff) * translateX);
        }

        child.setScaleX(scale);
        child.setScaleY(scale);
    }
}