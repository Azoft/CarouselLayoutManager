CarouselLayoutManager for RecyclerView
======================

## Examples

![Example](resources/carousel_work.gif "working example")

## Description

This LayoutManager works only with fixedSized items in adapter.
To use this LayoutManager copy files that contains in com.azoft.carousellayoutmanager.carousel folder and use this code:

    final CarouselLayoutManager layoutManager = new CarouselLayoutManager(CarouselLayoutManager.VERTICAL);

    final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setHasFixedSize(true);

To enable items center scrolling add this CenterScrollListener:

    recyclerView.addOnScrollListener(new CenterScrollListener());

To enable zoom effects that is enabled in gif add this line:

    layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());

Full code from this sample:

    // vertical and cycle layout
    final CarouselLayoutManager layoutManager = new CarouselLayoutManager(CarouselLayoutManager.VERTICAL, true);
    layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());

    final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setHasFixedSize(true);
    recyclerView.setAdapter(new TestAdapter(this));
    recyclerView.addOnScrollListener(new CenterScrollListener());
