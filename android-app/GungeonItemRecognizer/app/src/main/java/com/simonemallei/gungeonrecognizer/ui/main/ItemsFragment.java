package com.simonemallei.gungeonrecognizer.ui.main;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.simonemallei.gungeonrecognizer.R;
import com.simonemallei.gungeonrecognizer.adapter.GeneralAdapter;
import com.simonemallei.gungeonrecognizer.adapter.ItemIconAdapter;
import com.simonemallei.gungeonrecognizer.listener.OnItemSelectedListener;
import com.simonemallei.gungeonrecognizer.model.ApplicationModel;
import com.simonemallei.gungeonrecognizer.model.ItemModel;

import java.lang.ref.WeakReference;
import java.util.List;

/*
 * Class Name
 * com.simonemallei.gungeonrecognizer.ui.main.ItemsFragment
 *
 * Version information
 * 1.1.5
 *
 * Date (m/d/y)
 * 11/05/2021 22:20
 */

/**
 * ItemsFragment class containing the Items tab fragment.
 */
public class ItemsFragment extends Fragment {

    /**
     * An integer containing the number icons for each row (number of columns in the items tab).
     */
    private static final int NUM_ICONS = 8;
    /**
     * A List of ItemModel objects containing the items tab model.
     */
    private final List<ItemModel> mModel = ApplicationModel.ITEMS;
    /**
     * A GeneralAdapter reference to the adapter used by the items tab (ItemIconAdapter).
     */
    private GeneralAdapter mAdapter;
    /**
     * A pageViewModel reference to the ViewModel used.
     */
    private PageViewModel pageViewModel;
    /**
     * A WeakReference of OnItemSelectedListener called when an item is selected by the user.
     */
    private WeakReference<OnItemSelectedListener> mOnItemSelectedListenerRef;

    public ItemsFragment(){
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(this).get(PageViewModel.class);
        int index = 1;
        pageViewModel.setIndex(index);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof OnItemSelectedListener){
            final OnItemSelectedListener listener = (OnItemSelectedListener) context;
            mOnItemSelectedListenerRef = new WeakReference<>(listener);
        }
        else
            throw new IllegalStateException("Context must implement fragment's interface.");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mOnItemSelectedListenerRef != null) {
            mOnItemSelectedListenerRef.clear();
            mOnItemSelectedListenerRef = null;
        }
    }

    /**
     * Method called when creating the view: sets the items tab layout, the right GeneralAdapter
     * for this fragment (ItemIconAdapter) and their OnItemSelectedListener reference.
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Setting Items Tab layout
        View root = inflater.inflate(R.layout.items_tab, container, false);

        GridView mGridView = root.findViewById(R.id.item_grid);

        // Setting ItemIconAdapter for the tab
        mGridView.setNumColumns(NUM_ICONS);
        mAdapter = getCustomAdapter(NUM_ICONS, container);
        mGridView.setAdapter(mAdapter);
        Log.i("Icooons", NUM_ICONS+"");
        OnItemSelectedListener listener = mOnItemSelectedListenerRef.get();
        // Setting the click listener
        mGridView.setOnItemClickListener(listener.getItemListener(mAdapter));

        return root;
    }

    /**
     * Creates an ItemIconAdapter instance for the fragment.
     *
     * @param numIcons Number of icons for each row.
     * @param container ViewGroup reference to the fragment.
     * @return The fragment's ItemIconAdapter.
     */
    private GeneralAdapter getCustomAdapter(int numIcons, ViewGroup container) {
        return new ItemIconAdapter(mModel, this, numIcons, numIcons+1, container);
    }

}
