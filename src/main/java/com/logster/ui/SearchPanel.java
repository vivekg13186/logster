package com.logster.ui;

import com.logster.*;
import com.logster.search.SearchResult;
import com.logster.search.SearchResultRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

import static com.logster.ui.Icons.openFolderIcon;
import static com.logster.ui.Icons.searchIcon;

public class SearchPanel extends IconTabPanel {

    private final SearchResultTM tableModel = new SearchResultTM(new ArrayList<>());
    private final JTable resultTable = new JTable(tableModel);

    private final DateField fromDateField = new DateField();
    private final DateField toDateField = new DateField();

    private final JTextField locationTextBox = new JTextField(  30);
    final JButton searchBtn = new JButton(searchIcon);
    final JCheckBox useDate = new JCheckBox();
    final JButton openFolderBtn = new JButton(openFolderIcon);
    private final JTextField searchBox = new JTextField( 30);
    private SearchPanelListener listener;
    public SearchPanel(JTabbedPane tabbedPane) {
        super(tabbedPane, "Search", searchIcon);
        openFolderBtn.addActionListener((_) -> chooseFolder());
        JPanel searchRow = Util.rows(searchBox, searchBtn, new JLabel("Use range"), useDate, fromDateField, toDateField);
        JPanel folderPanel = Util.rows(locationTextBox, openFolderBtn);
        JPanel topPanel = Util.columns(1,folderPanel, searchRow);
        topPanel.add(searchRow);
        SearchResultRenderer renderer=new SearchResultRenderer();
        resultTable.setAutoCreateRowSorter(true);
        resultTable.getColumn(resultTable.getColumnName(0)).setCellRenderer(renderer);
        resultTable.getColumn(resultTable.getColumnName(1)).setCellRenderer(renderer);
        Util.setLineColWidth(resultTable);
        setLayout(new BorderLayout());
        Util.padding(this,2);
        add(new JScrollPane(resultTable), BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);
        LocalDateTime date = LocalDateTime.now();
        fromDateField.setDate(date.minusMinutes(10));
        toDateField.setDate(date);
        resultTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                int row = resultTable.getSelectedRow();
                if (row != -1) {
                    int modelRow = resultTable.convertRowIndexToModel(row);
                    SearchResult searchResult = tableModel.getSearchResultAt(modelRow);
                    if (evt.getClickCount() == 2) listener.onRowClick(searchResult);
                }
            }
        });
        searchBtn.addActionListener((_)-> listener.onSearchBtnClick());
    }

    private void chooseFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = chooser.getSelectedFile();
            locationTextBox.setText(selectedFolder.getAbsolutePath());

        }
    }

    public String getSearchQuery(){
        return Objects.requireNonNullElse(searchBox.getText(),"");
    }
    public String getSearchTLocation(){
        return Objects.requireNonNullElse(locationTextBox.getText(),"");
    }

    public void clearSearchResult(){
        tableModel.clear();
    }

    public boolean useDateForSearch(){
            return useDate.isSelected();
    }

    public long getStartTime(){
       return Util.toEpochMilli(fromDateField.getDate());
    }
    public long getEndTime(){
        return Util.toEpochMilli(toDateField.getDate());
    }

    public int getRowCount(){
        return tableModel.getRowCount();
    }
    public void  addSearchResult(SearchResult result){
        tableModel.addSearchResult(result);
    }

    public void setListener(SearchPanelListener listener) {
        this.listener = listener;
    }
}
