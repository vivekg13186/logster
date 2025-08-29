package com.logster;

import javax.swing.table.AbstractTableModel;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class VirtualFileTableModel extends AbstractTableModel {

    private final String[] columnNames = {"lno", "content"};
    private final File file;
    public final int startLineNumber; // 1-based inclusive
    public final int endLineNumber;   // 1-based inclusive
    private final List<String> lines = new ArrayList<>();

    public VirtualFileTableModel(File file, int requestedStartLineNumber, int requestedEndLineNumber) {
        this.file = file;
        // enforce 1-based inclusive start
        this.startLineNumber = Math.max(1, requestedStartLineNumber);
        // ensure end >= start
        this.endLineNumber = Math.max(this.startLineNumber, requestedEndLineNumber);
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
        int lineNumber = startLineNumber + rowIndex; // 1-based file line number
        String lineContent = lines.get(rowIndex);
        return columnIndex == 0 ? lineNumber : lineContent;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    private void loadLines() {
        lines.clear();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            int currentLine = 1;
            String line;
            while ((line = br.readLine()) != null) {
                if (currentLine >= startLineNumber) {
                    if (currentLine > endLineNumber) break;
                    if (line.length() > 500) {
                        lines.add(line.substring(0, 500));
                    } else {
                        lines.add(line);
                    }
                }
                currentLine++;
            }
        } catch (IOException e) {
            lines.add("[Error reading file: " + e.getMessage() + "]");
        }
    }
}
