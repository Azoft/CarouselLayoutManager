package com.azoft.carousellayoutmanager;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.azoft.carousellayoutmanager.carousel.CarouselLayoutManager;
import com.azoft.carousellayoutmanager.carousel.CarouselZoomPostLayoutListener;
import com.azoft.carousellayoutmanager.carousel.CenterScrollListener;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final CarouselLayoutManager layoutManager = new CarouselLayoutManager(CarouselLayoutManager.VERTICAL, true);
        layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());

        final RecyclerView list = (RecyclerView) findViewById(R.id.list);
        //noinspection ConstantConditions
        list.setLayoutManager(layoutManager);
//        list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        list.setHasFixedSize(true);
        list.setAdapter(new TestAdapter(this));
        list.addOnScrollListener(new CenterScrollListener());

        //noinspection ConstantConditions
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                list.scrollToPosition(5);
            }
        });
    }

    private static final class TestAdapter extends RecyclerView.Adapter<TestViewHolder> {

        private final Context mContext;
        @SuppressWarnings("UnsecureRandomNumberGeneration")
        private final Random mRandom = new Random();
        private final int[] mColors;

        TestAdapter(final Context context) {
            mContext = context;
            mColors = new int[getItemCount()];
            for (int i = 0; i < getItemCount(); ++i) {
                //noinspection MagicNumber
                mColors[i] = Color.argb(255, mRandom.nextInt(256), mRandom.nextInt(256), mRandom.nextInt(256));
            }
        }

        @Override
        public TestViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            return new TestViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_view, parent, false));
        }

        @Override
        public void onBindViewHolder(final TestViewHolder holder, final int position) {
            holder.mItem1TextView.setText(String.valueOf(position));
            holder.mItem2TextView.setText(String.valueOf(position));
            holder.itemView.setBackgroundColor(mColors[position]);
            if (holder.mProgressBar instanceof ContentLoadingProgressBar) {
                ((ContentLoadingProgressBar) holder.mProgressBar).show();
            }
        }

        @Override
        public int getItemCount() {
            return 10;
        }
    }

    private static class TestViewHolder extends RecyclerView.ViewHolder {

        private final TextView mItem1TextView;
        private final TextView mItem2TextView;
        private final ProgressBar mProgressBar;

        @SuppressWarnings("ParameterHidesMemberVariable")
        TestViewHolder(final View itemView) {
            super(itemView);

            mItem1TextView = (TextView) itemView.findViewById(R.id.c_item_1);
            mItem2TextView = (TextView) itemView.findViewById(R.id.c_item_2);
            mProgressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
        }
    }
}