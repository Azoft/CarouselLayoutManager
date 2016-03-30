package com.azoft.carousellayoutmanager.carousel;

import android.support.annotation.NonNull;
import android.view.View;

public class CarouselZoomPostLayoutListener implements CarouselLayoutManager.PostLayoutListener {

    @Override
    public void transformChild(@NonNull final View child, final float itemPositionToCenterDiff, final int orientation) {
        final float scale = (float) (4 * (2 * -StrictMath.atan(Math.abs(itemPositionToCenterDiff) + 1.0) / Math.PI + 1) / 2);

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