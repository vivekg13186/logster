package com.logster;

import javax.swing.table.AbstractTableModel;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class VirtualFileTableModel extends AbstractTableModel {

    private final String[] columnNames = {"lno", "content"};
    private final File file;
    public final int startLineNumber;
    public final int endLineNumber;
    private final List<String> lines = new ArrayList<>();

    public VirtualFileTableModel(File file, int startLineNumber, int endLineNumber) {
        this.file = file;
        this.startLineNumber = startLineNumber;
        this.endLineNumber = endLineNumber;
        loadLines();
    }

    @Override
    public int getRowCount() {
        return lines.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        int lineNumber = startLineNumber + rowIndex;
        String lineContent = lines.get(rowIndex);

        return columnIndex == 0 ? lineNumber : lineContent;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    private void loadLines() {
        lines.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            int currentLine = 1;
            String line;
            while ((line = br.readLine()) != null) {
                if (currentLine >= startLineNumber && currentLine <= endLineNumber) {
                    lines.add(line);
                }
                if (currentLine > endLineNumber) break;
                currentLine++;
            }
        } catch (IOException e) {
            lines.add("[Error reading file]");
        }
    }
}