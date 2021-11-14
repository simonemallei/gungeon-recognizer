package com.simonemallei.gungeonrecognizer.listener;

import android.widget.SearchView;

import com.simonemallei.gungeonrecognizer.adapter.GeneralAdapter;

/*
 * Interface Name
 * com.simonemallei.gungeonrecognizer.listener.OnQuerySearchListener
 *
 * Version information
 * 1.1.5
 *
 * Date (m/d/y)
 * 11/05/2021 22:20
 */

/**
 * OnQuerySearchListener interface containing method to define when
 * a query text search view is created.
 */
public interface OnQuerySearchListener extends SearchView.OnQueryTextListener {
    // Listener on searching text update
    void setQueryAdapter(GeneralAdapter adapter);
}
