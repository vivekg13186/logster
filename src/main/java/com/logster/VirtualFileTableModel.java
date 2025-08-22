package com.logster;
import javax.swing.table.AbstractTableModel;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
public class  VirtualFileTableModel extends AbstractTableModel {

    private final String[] columnNames = {"lno", "content"};
    private final File file;
    public final int startLineNumber;
    private final int cacheSize = 1000; // number of lines to cache
    private final List<String> cache = new ArrayList<>();
    private long cacheStartLine = 0; // line number in file corresponding to cache.get(0)
    private long totalLines = -1;

    public VirtualFileTableModel(File file, int startLineNumber) {
        this.file = file;
        this.startLineNumber = startLineNumber;
    }

    @Override
    public int getRowCount() {
        try {
            if (totalLines < 0) {
                totalLines = countTotalLines();
            }
        } catch (IOException _) {

        }
        return (int) Math.max(totalLines - startLineNumber + 1, 0);
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        long lineNumber = rowIndex + startLineNumber;
        String lineContent = getLine(lineNumber);

        if (columnIndex == 0) {
            return lineNumber;
        } else {
            return lineContent;
        }
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }



    private String getLine(long lineNumber) {
        if (lineNumber < cacheStartLine || lineNumber >= cacheStartLine + cache.size()) {
            try {
                loadCache(lineNumber);
            } catch (IOException _) {

                return "";
            }
        }
        int index = (int) (lineNumber - cacheStartLine);
        return index >= 0 && index < cache.size() ? cache.get(index) : "";
    }

    private void loadCache(long lineNumber) throws IOException {
        cache.clear();
        cacheStartLine = lineNumber;

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            long skip = lineNumber - 1;
            for (long i = 0; i < skip; i++) br.readLine(); // skip lines
            String line;
            int count = 0;
            while (count < cacheSize && (line = br.readLine()) != null) {
                cache.add(line);
                count++;
            }
        }
    }

    private long countTotalLines() throws IOException {
        long lines = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            while (br.readLine() != null) lines++;
        }
        return lines;
    }
}