package com.logster;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;

public class FileContentViewer extends ClosableTabPanel {




    public FileContentViewer(JTabbedPane tabbedPane, File file, int highlightLine) {
        super(tabbedPane, file.getName());

        VirtualFileTableModel model = new VirtualFileTableModel(file, 1);
        JTable table = new JTable(model);
        Util.setTableRenderer(table,new FileContentRenderer(highlightLine));

        table.setTableHeader(null);
        Util.setLineColWidth(table);

        table.setAutoCreateRowSorter(false);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setCellSelectionEnabled(true);

        // Copy functionality
        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK);
        table.getInputMap().put(copy, "copy");
        table.getActionMap().put("copy", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int[] rows = table.getSelectedRows();
                StringBuilder sb = new StringBuilder();
                for (int row : rows) {
                    int modelRow = table.convertRowIndexToModel(row);
                    sb.append(model.getValueAt(modelRow, 1)).append("\n");
                }
                Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new StringSelection(sb.toString()), null);
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        JLabel fileLabel = new JLabel(file.getAbsolutePath());
        fileLabel.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
        setLayout(new BorderLayout());
        add(fileLabel,BorderLayout.NORTH);
        add(scrollPane,BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(10,10,0,10));
        SwingUtilities.invokeLater(() -> {
            int rowIndex =  (highlightLine - model.startLineNumber);
            int viewRow = table.convertRowIndexToView(rowIndex);
            table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
            table.setRowSelectionInterval(viewRow, viewRow);
            table.requestFocusInWindow();
        });
    }


}
