package com.azoft.carousellayoutmanager.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.azoft.carousellayoutmanager.CarouselLayoutManager;
import com.azoft.carousellayoutmanager.CarouselZoomPostLayoutListener;
import com.azoft.carousellayoutmanager.CenterScrollListener;

import java.util.Random;

public class CarouselPreviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_carousel_preview);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // create layout manager with needed params: vertical, cycle
        final CarouselLayoutManager layoutManager = new CarouselLayoutManager(CarouselLayoutManager.VERTICAL, true);
        // enable zoom effect. this line can be customized
        layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());

        final RecyclerView list = (RecyclerView) findViewById(R.id.list);
        //noinspection ConstantConditions
        list.setLayoutManager(layoutManager);
        // we expect only fixed sized item for now
        list.setHasFixedSize(true);
        // sample adapter with random data
        list.setAdapter(new TestAdapter());
        // enable center post scrolling
        list.addOnScrollListener(new CenterScrollListener());

        layoutManager.addOnItemSelectionListener(new CarouselLayoutManager.OnCenterItemSelectionListener() {

            private Toast mToast;

            @Override
            public void onCenterItemChanged(final int adapterPosition) {
                if (null != mToast) {
                    mToast.cancel();
                }
                // uncomment lines bellow to show center item in toast.
                // but be aware that showing toast can be long operation, so scrolling may freeze
/*
                mToast = Toast.makeText(MainActivity.this, String.valueOf(adapterPosition), Toast.LENGTH_LONG);
                mToast.show();
*/
            }
        });

        // fab button will scroll to 5'th position
        //noinspection ConstantConditions
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                list.scrollToPosition(5);
            }
        });
    }

    private static final class TestAdapter extends RecyclerView.Adapter<TestViewHolder> {

        @SuppressWarnings("UnsecureRandomNumberGeneration")
        private final Random mRandom = new Random();
        private final int[] mColors;

        TestAdapter() {
            mColors = new int[getItemCount()];
            for (int i = 0; i < getItemCount(); ++i) {
                //noinspection MagicNumber
                mColors[i] = Color.argb(255, mRandom.nextInt(256), mRandom.nextInt(256), mRandom.nextInt(256));
            }
        }

        @Override
        public TestViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            return new TestViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(final TestViewHolder holder, final int position) {
            holder.mItem1TextView.setText(String.valueOf(position));
            holder.mItem2TextView.setText(String.valueOf(position));
            holder.itemView.setBackgroundColor(mColors[position]);
        }

        @Override
        public int getItemCount() {
            return 10;
        }
    }

    private static class TestViewHolder extends RecyclerView.ViewHolder {

        private final TextView mItem1TextView;
        private final TextView mItem2TextView;

        TestViewHolder(final ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent, false));

            mItem1TextView = (TextView) itemView.findViewById(R.id.c_item_1);
            mItem2TextView = (TextView) itemView.findViewById(R.id.c_item_2);
        }
    }
}