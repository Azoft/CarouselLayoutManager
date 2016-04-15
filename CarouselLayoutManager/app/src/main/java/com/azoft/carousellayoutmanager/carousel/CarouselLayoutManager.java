package com.azoft.carousellayoutmanager.carousel;

import android.graphics.PointF;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An implementation of {@link RecyclerView.LayoutManager} that layout items like carousel.
 * Generally there is one center item and bellow this item there are maximum {@link CarouselLayoutManager#getMaxVisibleItems()} items on each side of the center
 * item. By default {@link CarouselLayoutManager#getMaxVisibleItems()} is {@link CarouselLayoutManager#MAX_VISIBLE_ITEMS}.<br />
 * <br />
 * This LayoutManager supports only fixedSized adapter items.<br />
 * <br />
 * This LayoutManager supports {@link CarouselLayoutManager#HORIZONTAL} and {@link CarouselLayoutManager#VERTICAL} orientations. <br />
 * <br />
 * This LayoutManager supports circle layout. By default it if disabled. We don't recommend to use circle layout with adapter items count less then 3. <br />
 * <br />
 * Please be sure that layout_width of adapter item is a constant value and not {@link ViewGroup.LayoutParams#MATCH_PARENT}
 * for {@link #HORIZONTAL} orientation.
 * So like layout_height is not {@link ViewGroup.LayoutParams#MATCH_PARENT} for {@link CarouselLayoutManager#VERTICAL}<br />
 * <br />
 */
@SuppressWarnings("ClassWithTooManyMethods")
public class CarouselLayoutManager extends RecyclerView.LayoutManager {

    public static final int HORIZONTAL = OrientationHelper.HORIZONTAL;
    public static final int VERTICAL = OrientationHelper.VERTICAL;

    private static final int INVALID_POSITION = -1;

    public static final int MAX_VISIBLE_ITEMS = 2;
    private static final boolean CIRCLE_LAYOUT = false;

    private Integer mDecoratedChildWidth;
    private Integer mDecoratedChildHeight;

    private final int mOrientation;
    private final boolean mCircleLayout;

    private int mPendingScrollPosition;

    private final LayoutHelper mLayoutHelper = new LayoutHelper(MAX_VISIBLE_ITEMS);

    private PostLayoutListener mViewPostLayout;

    private final List<OnCenterItemSelectionListener> mOnCenterItemSelectionListeners = new ArrayList<>();
    private int mCenterItemPosition = INVALID_POSITION;

    /**
     * @param orientation should be {@link #VERTICAL} or {@link #HORIZONTAL}
     */
    @SuppressWarnings("unused")
    public CarouselLayoutManager(final int orientation) {
        this(orientation, CIRCLE_LAYOUT);
    }

    /**
     * If circleLayout is true then all items will be in cycle. Scroll will be infinite on both sides.
     *
     * @param orientation  should be {@link #VERTICAL} or {@link #HORIZONTAL}
     * @param circleLayout true for enabling circleLayout
     */
    @SuppressWarnings("unused")
    public CarouselLayoutManager(final int orientation, final boolean circleLayout) {
        if (HORIZONTAL != orientation && VERTICAL != orientation) {
            throw new IllegalArgumentException("orientation should be HORIZONTAL or VERTICAL");
        }
        mOrientation = orientation;
        mCircleLayout = circleLayout;
        mPendingScrollPosition = INVALID_POSITION;
    }

    /**
     * Setup {@link CarouselLayoutManager.PostLayoutListener} for this LayoutManager.
     * Its methods will be called for each visible view item after general LayoutManager layout finishes. <br />
     * <br />
     * Generally this method should be used for scaling and translating view item for better (different) view presentation of layouting.
     *
     * @param postLayoutListener listener for item layout changes. Can be null.
     */
    @SuppressWarnings("unused")
    public void setPostLayoutListener(@Nullable final PostLayoutListener postLayoutListener) {
        mViewPostLayout = postLayoutListener;
        requestLayout();
    }

    /**
     * Setup maximum visible (layout) items on each side of the center item.
     * Basically during scrolling there can be more visible items (+1 item on each side), but in idle state this is the only reached maximum.
     *
     * @param maxVisibleItems should be great then 0, if bot an {@link IllegalAccessException} will be thrown
     */
    @CallSuper
    @SuppressWarnings("unused")
    public void setMaxVisibleItems(final int maxVisibleItems) {
        if (0 >= maxVisibleItems) {
            throw new IllegalArgumentException("maxVisibleItems can't be less then 1");
        }
        mLayoutHelper.mMaxVisibleItems = maxVisibleItems;
        requestLayout();
    }

    /**
     * @return current setup for maximum visible items.
     * @see #setMaxVisibleItems(int)
     */
    @SuppressWarnings("unused")
    public int getMaxVisibleItems() {
        return mLayoutHelper.mMaxVisibleItems;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    /**
     * @return current layout orientation
     * @see #VERTICAL
     * @see #HORIZONTAL
     */
    public int getOrientation() {
        return mOrientation;
    }

    @Override
    public boolean canScrollHorizontally() {
        return HORIZONTAL == mOrientation;
    }

    @Override
    public boolean canScrollVertically() {
        return VERTICAL == mOrientation;
    }

    /**
     * @return current layout center item
     */
    public int getCenterItemPosition() {
        return mCenterItemPosition;
    }

    /**
     * @param onCenterItemSelectionListener listener that will trigger when ItemSelectionChanges. can't be null
     */
    public void addOnItemSelectionListener(@NonNull final OnCenterItemSelectionListener onCenterItemSelectionListener) {
        mOnCenterItemSelectionListeners.add(onCenterItemSelectionListener);
    }

    /**
     * @param onCenterItemSelectionListener listener that was previously added by {@link #addOnItemSelectionListener(OnCenterItemSelectionListener)}
     */
    public void removeOnItemSelectionListener(@NonNull final OnCenterItemSelectionListener onCenterItemSelectionListener) {
        mOnCenterItemSelectionListeners.remove(onCenterItemSelectionListener);
    }

    @SuppressWarnings("RefusedBequest")
    @Override
    public void scrollToPosition(final int position) {
        mPendingScrollPosition = position;
        requestLayout();
    }

    @SuppressWarnings("RefusedBequest")
    @Override
    public void smoothScrollToPosition(@NonNull final RecyclerView recyclerView, @NonNull final RecyclerView.State state, final int position) {
        final CarouselSmoothScroller mySmoothScroller =
                new CarouselSmoothScroller(recyclerView.getContext()) {
                    @Override
                    public PointF computeScrollVectorForPosition(final int targetPosition) {
                        return CarouselLayoutManager.this.computeScrollVectorForPosition(targetPosition);
                    }
                };
        mySmoothScroller.setTargetPosition(position);
        startSmoothScroll(mySmoothScroller);
    }

    protected PointF computeScrollVectorForPosition(final int targetPosition) {
        if (0 == getChildCount()) {
            return null;
        }
        final int direction = targetPosition < getCurrentScrollPosition() ? -1 : 1;
        if (HORIZONTAL == mOrientation) {
            return new PointF(direction, 0);
        } else {
            return new PointF(0, direction);
        }
    }

    @Override
    public int scrollVerticallyBy(final int dy, @NonNull final RecyclerView.Recycler recycler, @NonNull final RecyclerView.State state) {
        if (HORIZONTAL == mOrientation) {
            return 0;
        }
        return scrollBy(dy, recycler, state);
    }

    @Override
    public int scrollHorizontallyBy(final int dx, final RecyclerView.Recycler recycler, final RecyclerView.State state) {
        if (VERTICAL == mOrientation) {
            return 0;
        }
        return scrollBy(dx, recycler, state);
    }

    /**
     * This method is called from {@link #scrollHorizontallyBy(int, RecyclerView.Recycler, RecyclerView.State)} and
     * {@link #scrollVerticallyBy(int, RecyclerView.Recycler, RecyclerView.State)} to calculate needed scroll that is allowed. <br />
     * <br />
     * This method may do relayout work.
     *
     * @param diff     distance that we want to scroll by
     * @param recycler Recycler to use for fetching potentially cached views for a position
     * @param state    Transient state of RecyclerView
     * @return distance that we actually scrolled by
     */
    @CallSuper
    protected int scrollBy(final int diff, @NonNull final RecyclerView.Recycler recycler, @NonNull final RecyclerView.State state) {
        if (0 == getChildCount() || 0 == diff) {
            return 0;
        }
        final int resultScroll;
        if (mCircleLayout) {
            resultScroll = diff;
        } else {
            final int maxOffset = getMaxScrollOffset();

            if (0 > mLayoutHelper.mScrollOffset + diff) {
                resultScroll = -mLayoutHelper.mScrollOffset; //to make it 0
            } else if (mLayoutHelper.mScrollOffset + diff > maxOffset) {
                resultScroll = maxOffset - mLayoutHelper.mScrollOffset; //to make it maxOffset
            } else {
                resultScroll = diff;
            }
        }
        if (0 != resultScroll) {
            mLayoutHelper.mScrollOffset += resultScroll;
            fillData(recycler, state);
        }
        return resultScroll;
    }

    @Override
    public void onMeasure(final RecyclerView.Recycler recycler, final RecyclerView.State state, final int widthSpec, final int heightSpec) {
        mDecoratedChildHeight = null;
        mDecoratedChildWidth = null;

        super.onMeasure(recycler, state, widthSpec, heightSpec);
    }

    @SuppressWarnings("RefusedBequest")
    @Override
    @CallSuper
    public void onLayoutChildren(@NonNull final RecyclerView.Recycler recycler, @NonNull final RecyclerView.State state) {
        if (0 == state.getItemCount()) {
            removeAndRecycleAllViews(recycler);
            notifyOnItemSelectionListeners(INVALID_POSITION);
            return;
        }
        if (state.didStructureChange()) {
            // seance we don't know if views are actual now. so we should delete all cached views.
            removeAndRecycleAllViews(recycler);
        }

        if (null == mDecoratedChildWidth) {
            final View view = recycler.getViewForPosition(0);
            addView(view);
            measureChildWithMargins(view, 0, 0);

            mDecoratedChildWidth = getDecoratedMeasuredWidth(view);
            mDecoratedChildHeight = getDecoratedMeasuredHeight(view);
            removeAndRecycleView(view, recycler);
        }

        if (INVALID_POSITION != mPendingScrollPosition) {
            if (mPendingScrollPosition < state.getItemCount()) {
                mLayoutHelper.mScrollOffset = VERTICAL == mOrientation ? mPendingScrollPosition * mDecoratedChildHeight :
                        mPendingScrollPosition * mDecoratedChildWidth;
            } else {
                mPendingScrollPosition = INVALID_POSITION;
            }
        }

        fillData(recycler, state);
    }

    private void fillData(@NonNull final RecyclerView.Recycler recycler, @NonNull final RecyclerView.State state) {
        final float currentScrollPosition = INVALID_POSITION == mPendingScrollPosition ? getCurrentScrollPosition() : mPendingScrollPosition;
        mPendingScrollPosition = INVALID_POSITION;
        generateLayoutOrder(currentScrollPosition, state);
        removeAndRecycleUnusedViews(mLayoutHelper, recycler);

        final int width = getWidthNoPadding();
        final int height = getHeightNoPadding();
        if (VERTICAL == mOrientation) {
            fillDataVertical(recycler, width, height);
        } else {
            fillDataHorizontal(recycler, width, height);
        }

        recycler.clear();

        detectOnItemSelectionChanged(currentScrollPosition, state);
    }

    private void detectOnItemSelectionChanged(final float currentScrollPosition, final RecyclerView.State state) {
        final float absCurrentScrollPosition = makeScrollPositionInRange0ToCount(currentScrollPosition, state.getItemCount());
        final int centerItem = Math.round(absCurrentScrollPosition);

        if (mCenterItemPosition != centerItem) {
            notifyOnItemSelectionListeners(centerItem);
        }
    }

    private void notifyOnItemSelectionListeners(final int centerItem) {
        mCenterItemPosition = centerItem;
        for (final OnCenterItemSelectionListener onCenterItemSelectionListener : mOnCenterItemSelectionListeners) {
            onCenterItemSelectionListener.onCenterItemChanged(centerItem);
        }
    }

    private void fillDataVertical(final RecyclerView.Recycler recycler, final int width, final int height) {
        final int start = (width - mDecoratedChildWidth) / 2;
        final int end = start + mDecoratedChildWidth;

        final int centerViewTop = (height - mDecoratedChildHeight) / 2;

        for (int i = 0, count = mLayoutHelper.mLayoutOrder.length; i < count; ++i) {
            final LayoutOrder layoutOrder = mLayoutHelper.mLayoutOrder[i];
            final int offset = getCardOffsetByPositionDiff(layoutOrder.mItemPositionDiff);
            final int top = centerViewTop + offset;
            final int bottom = top + mDecoratedChildHeight;
            fillChildItem(start, top, end, bottom, layoutOrder, recycler, i);
        }
    }

    private void fillDataHorizontal(final RecyclerView.Recycler recycler, final int width, final int height) {
        final int top = (height - mDecoratedChildHeight) / 2;
        final int bottom = top + mDecoratedChildHeight;

        final int centerViewStart = (width - mDecoratedChildWidth) / 2;

        for (int i = 0, count = mLayoutHelper.mLayoutOrder.length; i < count; ++i) {
            final LayoutOrder layoutOrder = mLayoutHelper.mLayoutOrder[i];
            final int offset = getCardOffsetByPositionDiff(layoutOrder.mItemPositionDiff);
            final int start = centerViewStart + offset;
            final int end = start + mDecoratedChildWidth;
            fillChildItem(start, top, end, bottom, layoutOrder, recycler, i);
        }
    }

    private void removeAndRecycleUnusedViews(final LayoutHelper layoutHelper, final RecyclerView.Recycler recycler) {
        final List<View> unusedViews = new ArrayList<>();
        for (int i = 0, size = getChildCount(); i < size; ++i) {
            final View child = getChildAt(i);
            final ViewGroup.LayoutParams lp = child.getLayoutParams();
            if (!(lp instanceof RecyclerView.LayoutParams)) {
                unusedViews.add(child);
                continue;
            }
            final int adapterPosition = ((RecyclerView.LayoutParams) lp).getViewAdapterPosition();
            if (!layoutHelper.hasAdapterPosition(adapterPosition)) {
                unusedViews.add(child);
            }
        }

        for (final View view : unusedViews) {
            removeAndRecycleView(view, recycler);
        }
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    private void fillChildItem(final int start, final int top, final int end, final int bottom, @NonNull final LayoutOrder layoutOrder, @NonNull final RecyclerView.Recycler recycler, final int i) {
        final View view = layoutChild(start, top, end, bottom, layoutOrder.mItemAdapterPosition, recycler);
        ViewCompat.setElevation(view, i);
        if (null != mViewPostLayout) {
            performViewPostLayoutChanges(mViewPostLayout, view, layoutOrder.mItemPositionDiff, mOrientation);
        }
    }

    /**
     * @return current scroll position of center item. this value can be in any range if it is cycle layout.
     * if this is not, that then it is in [0, {@link #getItemCount() - 1}]
     */
    private float getCurrentScrollPosition() {
        final int fullScrollSize = getMaxScrollOffset();
        if (0 == fullScrollSize) {
            return 0;
        }
        return 1.0f * mLayoutHelper.mScrollOffset / getScrollItemSize();
    }

    /**
     * @return maximum scroll value to fill up all items in layout. Generally this is only needed for non cycle layouts.
     */
    private int getMaxScrollOffset() {
        return getScrollItemSize() * (getItemCount() - 1);
    }

    private static void performViewPostLayoutChanges(@NonNull final PostLayoutListener postLayout, @NonNull final View view, final float itemPositionToCenterDiff, final int orientation) {
        postLayout.transformChild(view, itemPositionToCenterDiff, orientation);
    }

    /**
     * Because we can support old Android versions, we should layout our children in specific order to make our center view in the top of layout
     * (this item should layout last). So this method will calculate layout order and fill up {@link #mLayoutHelper} object.
     * This object will be filled by only needed to layout items. Non visible items will not be there.
     *
     * @param currentScrollPosition current scroll position this is a value that indicates position of center item
     *                              (if this value is int, then center item is really in the center of the layout, else it is near state).
     *                              Be aware that this value can be in any range is it is cycle layout
     * @param state                 Transient state of RecyclerView
     * @see #getCurrentScrollPosition()
     */
    private void generateLayoutOrder(final float currentScrollPosition, @NonNull final RecyclerView.State state) {
        final int count = state.getItemCount();
        final float absCurrentScrollPosition = makeScrollPositionInRange0ToCount(currentScrollPosition, count);
        final int centerItem = Math.round(absCurrentScrollPosition);

        if (mCircleLayout && 1 < count) {
            final int layoutCount = Math.min(mLayoutHelper.mMaxVisibleItems * 2 + 3, count);// + 3 = 1 (center item) + 2 (addition bellow maxVisibleItems)

            mLayoutHelper.initLayoutOrder(layoutCount);

            final int countLayoutHalf = layoutCount / 2;
            // before center item
            for (int i = 1; i <= countLayoutHalf; ++i) {
                final int position = Math.round(absCurrentScrollPosition - i + count) % count;
                mLayoutHelper.setLayoutOrder(countLayoutHalf - i, position, centerItem - absCurrentScrollPosition - i);
            }
            // after center item
            for (int i = layoutCount - 1; i >= countLayoutHalf + 1; --i) {
                final int position = Math.round(absCurrentScrollPosition - i + layoutCount) % count;
                mLayoutHelper.setLayoutOrder(i - 1, position, centerItem - absCurrentScrollPosition + layoutCount - i);
            }
            mLayoutHelper.setLayoutOrder(layoutCount - 1, centerItem, centerItem - absCurrentScrollPosition);

        } else {
            final int firstVisible = Math.max(centerItem - mLayoutHelper.mMaxVisibleItems - 1, 0);
            final int lastVisible = Math.min(centerItem + mLayoutHelper.mMaxVisibleItems + 1, count - 1);
            final int layoutCount = lastVisible - firstVisible + 1;

            mLayoutHelper.initLayoutOrder(layoutCount);

            for (int i = firstVisible; i <= lastVisible; ++i) {
                if (i == centerItem) {
                    mLayoutHelper.setLayoutOrder(layoutCount - 1, i, i - absCurrentScrollPosition);
                } else if (i < centerItem) {
                    mLayoutHelper.setLayoutOrder(i - firstVisible, i, i - absCurrentScrollPosition);
                } else {
                    mLayoutHelper.setLayoutOrder(layoutCount - (i - centerItem) - 1, i, i - absCurrentScrollPosition);
                }
            }
        }
    }

    public int getWidthNoPadding() {
        return getWidth() - getPaddingStart() - getPaddingEnd();
    }

    public int getHeightNoPadding() {
        return getHeight() - getPaddingEnd() - getPaddingStart();
    }

    private View layoutChild(final int start, final int top, final int end, final int bottom, final int position, @NonNull final RecyclerView.Recycler recycler) {
        final View view = findViewForPosition(recycler, position);

        if (null == view.getParent()) {
            addView(view);
            measureChildWithMargins(view, 0, 0);
        } else {
            view.bringToFront();
        }

        view.layout(start, top, end, bottom);

        return view;
    }

    private View findViewForPosition(final RecyclerView.Recycler recycler, final int position) {
        for (int i = 0, size = getChildCount(); i < size; ++i) {
            final View child = getChildAt(i);
            final ViewGroup.LayoutParams lp = child.getLayoutParams();
            if (!(lp instanceof RecyclerView.LayoutParams)) {
                continue;
            }
            final int adapterPosition = ((RecyclerView.LayoutParams) lp).getViewAdapterPosition();
            if (adapterPosition == position) {
                return child;
            }
        }
        final View view = recycler.getViewForPosition(position);
        recycler.bindViewToPosition(view, position);
        return view;
    }

    /**
     * Called during {@link #fillData(RecyclerView.Recycler, RecyclerView.State)} to calculate item offset from layout center line. <br />
     * <br />
     * Returns {@link #convertItemPositionDiffToSmoothPositionDiff(float)} * (size off area above center item when it is on the center). <br />
     * Sign is: plus if this item is bellow center line, minus if not<br />
     * <br />
     * ----- - area above it<br />
     * ||||| - center item<br />
     * ----- - area bellow it (it has the same size as are above center item)<br />
     *
     * @param itemPositionDiff current item difference with layout center line. if this is 0, then this item center is in layout center line.
     *                         if this is 1 then this item is bellow the layout center line in the full item size distance.
     * @return offset in scroll px coordinates.
     */
    protected int getCardOffsetByPositionDiff(final float itemPositionDiff) {
        final double smoothPosition = convertItemPositionDiffToSmoothPositionDiff(itemPositionDiff);

        final int dimenDiff;
        if (VERTICAL == mOrientation) {
            dimenDiff = (getHeightNoPadding() - mDecoratedChildHeight) / 2;
        } else {
            dimenDiff = (getWidthNoPadding() - mDecoratedChildWidth) / 2;
        }
        //noinspection NumericCastThatLosesPrecision
        return (int) Math.round(Math.signum(itemPositionDiff) * dimenDiff * smoothPosition);
    }

    /**
     * Called during {@link #getCardOffsetByPositionDiff(float)} for better item movement. <br/>
     * Current implementation speed up items that are far from layout center line and slow down items that are close to this line.
     * This code is full of maths. If you want to make items move in a different way, probably you should override this method.<br />
     * Please see code comments for better explanations.
     *
     * @param itemPositionDiff current item difference with layout center line. if this is 0, then this item center is in layout center line.
     *                         if this is 1 then this item is bellow the layout center line in the full item size distance.
     * @return smooth position offset. needed for scroll calculation and better user experience.
     * @see #getCardOffsetByPositionDiff(float)
     */
    @SuppressWarnings({"MagicNumber", "InstanceMethodNamingConvention"})
    protected double convertItemPositionDiffToSmoothPositionDiff(final float itemPositionDiff) {
        // generally item moves the same way above center and bellow it. So we don't care about diff sign.
        final float absIemPositionDiff = Math.abs(itemPositionDiff);

        // we detect if this item is close for center or not. We use (1 / maxVisibleItem) ^ (1/3) as close definer.
        if (absIemPositionDiff > StrictMath.pow(1.0f / mLayoutHelper.mMaxVisibleItems, 1.0f / 3)) {
            // this item is far from center line, so we should make it move like square root function
            return StrictMath.pow(absIemPositionDiff / mLayoutHelper.mMaxVisibleItems, 1 / 2.0f);
        } else {
            // this item is close from center line. we should slow it down and don't make it speed up very quick.
            // so square function in range of [0, (1/maxVisible)^(1/3)] is quite good in it;
            return StrictMath.pow(absIemPositionDiff, 2.0f);
        }
    }

    /**
     * @return full item size
     */
    protected int getScrollItemSize() {
        if (VERTICAL == mOrientation) {
            return mDecoratedChildHeight;
        } else {
            return mDecoratedChildWidth;
        }
    }

    /**
     * @return Scroll offset from nearest item from center
     */
    int getOffsetCenterView() {
        return Math.round(getCurrentScrollPosition()) * getScrollItemSize() - mLayoutHelper.mScrollOffset;
    }

    int getOffsetForCurrentView(@NonNull final View view) {
        final int position = getPosition(view);
        return mLayoutHelper.mScrollOffset - position * getScrollItemSize();
    }

    /**
     * Helper method that make scroll in range of [0, count). Generally this method is needed only for cycle layout.
     *
     * @param currentScrollPosition any scroll position range.
     * @param count                 adapter items count
     * @return good scroll position in range of [0, count)
     */
    private static float makeScrollPositionInRange0ToCount(final float currentScrollPosition, final int count) {
        float absCurrentScrollPosition = currentScrollPosition;
        while (0 > absCurrentScrollPosition) {
            absCurrentScrollPosition += count;
        }
        while (Math.round(absCurrentScrollPosition) >= count) {
            absCurrentScrollPosition -= count;
        }
        return absCurrentScrollPosition;
    }

    /**
     * This interface methods will be called for each visible view item after general LayoutManager layout finishes. <br />
     * <br />
     * Generally this method should be used for scaling and translating view item for better (different) view presentation of layouting.
     */
    @SuppressWarnings("InterfaceNeverImplemented")
    public interface PostLayoutListener {

        /**
         * Called after child layout finished. Generally you can do any translation and scaling work here.
         *
         * @param child                    view that was layout
         * @param itemPositionToCenterDiff view center line difference to layout center. if > 0 then this item is bellow layout center line, else if not
         * @param orientation              layoutManager orientation {@link #getLayoutDirection()}
         */
        void transformChild(@NonNull final View child, final float itemPositionToCenterDiff, final int orientation);
    }

    public interface OnCenterItemSelectionListener {

        /**
         * Listener that will be called on every change of center item.
         * This listener will be triggered on <b>every</b> layout operation if item was changed.
         * Do not do any expensive operations in this method since this will effect scroll experience.
         *
         * @param adapterPosition current layout center item
         */
        void onCenterItemChanged(final int adapterPosition);
    }

    /**
     * Helper class that holds currently visible items.
     * Generally this class fills this list. <br />
     * <br />
     * This class holds all scroll and maxVisible items state.
     *
     * @see #getMaxVisibleItems()
     */
    private static class LayoutHelper {

        private int mMaxVisibleItems;

        private int mScrollOffset;

        private LayoutOrder[] mLayoutOrder;

        private final List<WeakReference<LayoutOrder>> mReusedItems = new ArrayList<>();

        LayoutHelper(final int maxVisibleItems) {
            mMaxVisibleItems = maxVisibleItems;
        }

        /**
         * Called before any fill calls. Needed to recycle old items and init new array list. Generally this list is an array an it is reused.
         *
         * @param layoutCount items count that will be layout
         */
        public void initLayoutOrder(final int layoutCount) {
            if (null == mLayoutOrder || mLayoutOrder.length != layoutCount) {
                if (null != mLayoutOrder) {
                    recycleItems(mLayoutOrder);
                }
                mLayoutOrder = new LayoutOrder[layoutCount];
                fillLayoutOrder();
            }
        }

        /**
         * Called during layout generation process of filling this list. Should be called only after {@link #initLayoutOrder(int)} method call.
         *
         * @param arrayPosition       position in layout order
         * @param itemAdapterPosition adapter position of item for future data filling logic
         * @param itemPositionDiff    difference of current item scroll position and center item position.
         *                            if this is a center item and it is in real center of layout, then this will be 0.
         *                            if current layout is not in the center, then this value will never be int.
         *                            if this item center is bellow layout center line then this value is greater then 0,
         *                            else less then 0.
         */
        public void setLayoutOrder(final int arrayPosition, final int itemAdapterPosition, final float itemPositionDiff) {
            final LayoutOrder item = mLayoutOrder[arrayPosition];
            item.mItemAdapterPosition = itemAdapterPosition;
            item.mItemPositionDiff = itemPositionDiff;
        }

        /**
         * Checks is this screen Layout has this adapterPosition view in layout
         *
         * @param adapterPosition adapter position of item for future data filling logic
         * @return true is adapterItem is in layout
         */
        public boolean hasAdapterPosition(final int adapterPosition) {
            if (null != mLayoutOrder) {
                for (final LayoutOrder layoutOrder : mLayoutOrder) {
                    if (layoutOrder.mItemAdapterPosition == adapterPosition) {
                        return true;
                    }
                }
            }
            return false;
        }

        @SuppressWarnings("VariableArgumentMethod")
        private void recycleItems(@NonNull final LayoutOrder... layoutOrders) {
            for (final LayoutOrder layoutOrder : layoutOrders) {
                //noinspection ObjectAllocationInLoop
                mReusedItems.add(new WeakReference<>(layoutOrder));
            }
        }

        private void fillLayoutOrder() {
            for (int i = 0; i < mLayoutOrder.length; ++i) {
                if (null == mLayoutOrder[i]) {
                    mLayoutOrder[i] = createLayoutOrder();
                }
            }
        }

        private LayoutOrder createLayoutOrder() {
            final Iterator<WeakReference<LayoutOrder>> iterator = mReusedItems.iterator();
            while (iterator.hasNext()) {
                final WeakReference<LayoutOrder> layoutOrderWeakReference = iterator.next();
                final LayoutOrder layoutOrder = layoutOrderWeakReference.get();
                iterator.remove();
                if (null != layoutOrder) {
                    return layoutOrder;
                }
            }
            return new LayoutOrder();
        }
    }

    /**
     * Class that holds item data.
     * This class is filled during {@link #generateLayoutOrder(float, RecyclerView.State)} and used during {@link #fillData(RecyclerView.Recycler, RecyclerView.State)}
     */
    private static class LayoutOrder {

        /**
         * Item adapter position
         */
        private int mItemAdapterPosition;
        /**
         * Item center difference to layout center. If center of item is bellow layout center, then this value is greater then 0, else it is less.
         */
        private float mItemPositionDiff;

    }
}