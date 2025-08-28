package com.logster.ui;


import com.logster.search.SearchResult;

public interface SearchPanelListener {

    void onSearchBtnClick();
    void onRowClick(SearchResult result);

}
