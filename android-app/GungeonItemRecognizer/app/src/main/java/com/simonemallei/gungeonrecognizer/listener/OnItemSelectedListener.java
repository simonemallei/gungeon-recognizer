package com.simonemallei.gungeonrecognizer.listener;

import android.widget.AdapterView;

import com.simonemallei.gungeonrecognizer.adapter.GeneralAdapter;
import com.simonemallei.gungeonrecognizer.model.ItemModel;

/*
 * Interface Name
 * com.simonemallei.gungeonrecognizer.listener.OnItemSelectedListener
 *
 * Version information
 * 1.1.5
 *
 * Date (m/d/y)
 * 11/05/2021 22:20
 */

/**
 * OnItemSelectedListener interface containing methods to define when
 * an ItemModel object is selected.
 */
public interface OnItemSelectedListener {
    // Listener on item selection
    void onItemSelected(ItemModel item);
    AdapterView.OnItemClickListener getItemListener(GeneralAdapter adapter);
}
