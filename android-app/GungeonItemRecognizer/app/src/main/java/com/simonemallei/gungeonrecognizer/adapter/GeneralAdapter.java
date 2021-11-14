package com.simonemallei.gungeonrecognizer.adapter;

import androidx.fragment.app.Fragment;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.simonemallei.gungeonrecognizer.model.ItemModel;

import java.util.List;

/*
 * Class Name
 * com.simonemallei.gungeonrecognizer.adapter.GeneralAdapter
 *
 * Version information
 * 1.1.5
 *
 * Date (m/d/y)
 * 11/05/2021 22:20
 */

/**
 * General Adapter class containing ItemModel list's adapter.
 */
public abstract class GeneralAdapter extends BaseAdapter {
    /**
     * A Fragment reference to the fragment considered by the adapter.
     */
    protected Fragment mFragment;
    /**
     * A List of ItemModel objects containing the items considered by the adapter.
     */
    protected List<ItemModel> mModel;
    /**
     * A ViewGroup reference to the adapter's container.
     */
    protected ViewGroup mContainer;

    public GeneralAdapter(List<ItemModel> mModel, Fragment mFragment, ViewGroup container) {
        super();
        this.mModel = mModel;
        this.mFragment = mFragment;
        this.mContainer = container;
    }

    public void setModel(List<ItemModel> newModel){
        this.mModel = newModel;
    }

    @Override
    public int getCount() {
        return mModel.size();
    }

    @Override
    public ItemModel getItem(int i) {
        return mModel.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

}
