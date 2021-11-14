package com.simonemallei.gungeonrecognizer.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.simonemallei.gungeonrecognizer.R;
import com.simonemallei.gungeonrecognizer.model.ItemModel;

import java.util.List;

/*
 * Class Name
 * com.simonemallei.gungeonrecognizer.adapter.ItemAdapter
 *
 * Version information
 * 1.1.5
 *
 * Date (m/d/y)
 * 11/05/2021 22:20
 */

/**
 * ItemAdapter class containing ItemModel list's adapter for
 * query text searching tab.
 */
public class ItemAdapter extends GeneralAdapter {

    /**
     * An integer containing the width divider for each item's image.
     */
    private static final int DIVIDE_BY = 8;

    static class Holder {
        ImageView itemImage;
        TextView itemTitle;
        TextView itemQuote;
    }

    public ItemAdapter(List<ItemModel> mModel, Fragment mFragment, ViewGroup mContainer) {
        super(mModel, mFragment, mContainer);
    }

    /**
     * Returns a View with these following items' infos: Title, Quote and Image.
     *
     * @return The View obtained by the method.
     */
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Holder holder = null;
        if (view == null) {
            holder = new Holder();
            // Setting item's layout
            view = LayoutInflater.from(mFragment.getContext())
                    .inflate(R.layout.item_layout, null);
            // Binding
            holder.itemTitle = view.findViewById(R.id.item_name);
            holder.itemQuote = view.findViewById(R.id.item_quote);
            holder.itemImage = view.findViewById(R.id.item_image);
            // Saving as Tag
            view.setTag(holder);
        }
        else
            holder = (Holder) view.getTag();
        // Current item
        final ItemModel item = getItem(i);
        // Setting the data
        holder.itemTitle.setText(item.title);
        holder.itemQuote.setText(item.quote);

        // Scaling image
        Drawable imageToResize = item.image;
        Bitmap bitmapToResize = ((BitmapDrawable)imageToResize).getBitmap();

        int dimensionScaled = mContainer.getMeasuredWidth() / DIVIDE_BY;

        Bitmap bitmapResult = Bitmap.createScaledBitmap(bitmapToResize,
                (int) (dimensionScaled),
                (int) (dimensionScaled),
                false);
        Drawable imageResized = new BitmapDrawable(mFragment.getResources(), bitmapResult);

        holder.itemImage.setImageDrawable(imageResized);

        return view;
    }
}
