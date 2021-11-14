package com.simonemallei.gungeonrecognizer.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.simonemallei.gungeonrecognizer.MainActivity;
import com.simonemallei.gungeonrecognizer.R;
import com.simonemallei.gungeonrecognizer.adapter.GeneralAdapter;
import com.simonemallei.gungeonrecognizer.adapter.ItemAdapter;
import com.simonemallei.gungeonrecognizer.listener.OnItemSelectedListener;
import com.simonemallei.gungeonrecognizer.listener.OnQuerySearchListener;
import com.simonemallei.gungeonrecognizer.model.ApplicationModel;
import com.simonemallei.gungeonrecognizer.model.ItemModel;

import java.lang.ref.WeakReference;
import java.util.List;

/*
 * Class Name
 * com.simonemallei.gungeonrecognizer.ui.main.SearchFragment
 *
 * Version information
 * 1.1.5
 *
 * Date (m/d/y)
 * 11/05/2021 22:20
 */

/**
 * SearchFragment class containing the Search tab fragment.
 */
public class SearchFragment extends Fragment{

    /**
     * A WeakReference of OnItemSelectedListener called when an item is selected by the user.
     */
    private static WeakReference<OnItemSelectedListener> mOnItemSelectedListenerRef;
    /**
     * A WeakReference of OnQuerySearchListener called when the search form is modified.
     */
    private WeakReference<OnQuerySearchListener> mOnQuerySearchListenerRef;
    /**
     * A List of ItemModel objects containing the search tab model.
     */
    private final List<ItemModel> mModel = ApplicationModel.ITEMS;
    /**
     * A pageViewModel reference to the ViewModel used.
     */
    private PageViewModel pageViewModel;

    public SearchFragment(){
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
        if(context instanceof OnItemSelectedListener && context instanceof OnQueryTextListener){
            final OnItemSelectedListener listener = (OnItemSelectedListener) context;
            mOnItemSelectedListenerRef = new WeakReference<>(listener);
            final OnQuerySearchListener searchListener = (OnQuerySearchListener) context;
            mOnQuerySearchListenerRef = new WeakReference<>(searchListener);
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
        if (mOnQuerySearchListenerRef != null) {
            mOnQuerySearchListenerRef.clear();
            mOnQuerySearchListenerRef = null;
        }
    }

    /**
     * Method called when creating the view: sets the search tab layout, the right GeneralAdapter
     * for this fragment (ItemAdapter), the fragment's OnQuerySearchListener and
     * model items' OnItemSelectedListener reference.
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Setting Search Tab layou
        View root = inflater.inflate(R.layout.search_tab, container, false);
        ListView mListView = root.findViewById(R.id.item_list);

        // Setting ItemAdapter for the tab
        GeneralAdapter mAdapter = getCustomAdapter(container);
        mListView.setAdapter(mAdapter);

        OnItemSelectedListener listener = mOnItemSelectedListenerRef.get();
        // Setting the click listener
        mListView.setOnItemClickListener(listener.getItemListener(mAdapter));

        OnQuerySearchListener searchListener = mOnQuerySearchListenerRef.get();
        searchListener.setQueryAdapter(mAdapter);
        SearchView mSearchView = root.findViewById(R.id.search_input);
        MainActivity mActivity = (MainActivity) searchListener;
        mActivity.mSearchView = mSearchView;
        // Setting the query text listener
        mSearchView.setOnQueryTextListener(mOnQuerySearchListenerRef.get());

        return root;
    }

    /**
     * Creates an ItemAdapter instance for the fragment.
     *
     * @param container ViewGroup reference to the fragment.
     * @return The fragment's ItemIconAdapter.
     */
    private GeneralAdapter getCustomAdapter(ViewGroup container) {
        return new ItemAdapter(mModel, this, container);
    }
}