package com.simonemallei.gungeonrecognizer.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.fragment.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.simonemallei.gungeonrecognizer.R;
import com.simonemallei.gungeonrecognizer.model.ItemModel;

import java.util.List;

/*
 * Class Name
 * com.simonemallei.gungeonrecognizer.adapter.ItemIconAdapter
 *
 * Version information
 * 1.1.5
 *
 * Date (m/d/y)
 * 11/05/2021 22:20
 */

/**
 * ItemIconAdapter class containing ItemModel list's adapter to show
 * items icons in the recognition tab and in the items tab.
 */
public class ItemIconAdapter extends GeneralAdapter {

    /**
     * An integer containing the number of icons for each row.
     */
    public int N_ICONS;
    /**
     * An integer containing the width divider for each item's icon.
     */
    private final int WIDTH_DIV;


    static class Holder {
        ImageView itemImage;
    }

    public ItemIconAdapter(List<ItemModel> mModel, Fragment mFragment, int numIcons,
                           int widthDivider, ViewGroup container) {
        super(mModel, mFragment, container);
        this.N_ICONS = numIcons;
        this.WIDTH_DIV = widthDivider;
    }

    /**
     * Returns a View with item's icon.
     *
     * @return The View obtained by the method.
     */
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Holder holder;
        if (view == null) {
            holder = new Holder();
            view = LayoutInflater.from(mFragment.getContext())
                    .inflate(R.layout.itemicon_layout, null);
            // Binding
            holder.itemImage = view.findViewById(R.id.item_image);
            // Saving as Tag
            view.setTag(holder);
        }
        else
            holder = (Holder) view.getTag();
        // Current item
        final ItemModel item = getItem(i);

        // Scaling image of the icon
        Drawable imageToResize = item.image;
        Bitmap bitmapToResize = ((BitmapDrawable)imageToResize).getBitmap();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        Activity mActivity = (Activity) (mFragment.getContext());
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int dimensionScaled = displayMetrics.widthPixels / (WIDTH_DIV);

        Bitmap bitmapResult = Bitmap.createScaledBitmap(bitmapToResize,
                (int) (dimensionScaled),
                (int) (dimensionScaled),
                false);

        Drawable imageResized = new BitmapDrawable(mFragment.getResources(), bitmapResult);

        holder.itemImage.setImageDrawable(imageResized);

        return view;
    }

}
