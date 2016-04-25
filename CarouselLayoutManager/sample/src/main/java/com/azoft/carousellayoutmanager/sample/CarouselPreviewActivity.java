package com.azoft.carousellayoutmanager.sample;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.azoft.carousellayoutmanager.CarouselLayoutManager;
import com.azoft.carousellayoutmanager.CarouselZoomPostLayoutListener;
import com.azoft.carousellayoutmanager.CenterScrollListener;
import com.azoft.carousellayoutmanager.sample.databinding.ActivityCarouselPreviewBinding;
import com.azoft.carousellayoutmanager.sample.databinding.ItemViewBinding;

import java.util.Random;

public class CarouselPreviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActivityCarouselPreviewBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_carousel_preview);

        setSupportActionBar(binding.toolbar);

        // create layout manager with needed params: vertical, cycle
        final CarouselLayoutManager layoutManager = new CarouselLayoutManager(CarouselLayoutManager.VERTICAL, true);
        // enable zoom effect. this line can be customized
        layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());

        binding.list.setLayoutManager(layoutManager);
        // we expect only fixed sized item for now
        binding.list.setHasFixedSize(true);
        // sample adapter with random data
        final TestAdapter adapter = new TestAdapter();
        binding.list.setAdapter(adapter);
        // enable center post scrolling
        binding.list.addOnScrollListener(new CenterScrollListener());

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
        binding.fabScroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                binding.list.smoothScrollToPosition(5);
            }
        });

        //noinspection ConstantConditions
        binding.fabChangeData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                adapter.mColors[5] = Color.BLACK;
                adapter.notifyItemChanged(5);
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
            return new TestViewHolder(ItemViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(final TestViewHolder holder, final int position) {
            holder.mItemViewBinding.cItem1.setText(String.valueOf(position));
            holder.mItemViewBinding.cItem2.setText(String.valueOf(position));
            holder.itemView.setBackgroundColor(mColors[position]);
        }

        @Override
        public int getItemCount() {
            return 10;
        }
    }

    private static class TestViewHolder extends RecyclerView.ViewHolder {

        private final ItemViewBinding mItemViewBinding;

        TestViewHolder(final ItemViewBinding itemViewBinding) {
            super(itemViewBinding.getRoot());

            mItemViewBinding = itemViewBinding;
        }
    }
}