package com.logster;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class SearchResultTM extends AbstractTableModel {
    private final String[] columnNames = {"lno", "line", "location"};
    private final List<SearchResult> searchResults;

    public SearchResultTM(List<SearchResult> searchResults) {
        this.searchResults = searchResults;
    }

    @Override
    public int getRowCount() {
        return searchResults.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        SearchResult p = searchResults.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> p.lineNumber;
            case 1 -> p;
            case 2 -> p.filePath;
            default -> null;
        };
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }
    public void addSearchResult(SearchResult p) {
        searchResults.add(p);
        int row = searchResults.size() - 1;
        fireTableRowsInserted(row, row);
    }

    public void clear() {
        searchResults.clear();
        fireTableDataChanged(); // refreshes whole table
    }

    public SearchResult getSearchResultAt(int modelRow) {
        return  searchResults.get(modelRow);
    }
}
