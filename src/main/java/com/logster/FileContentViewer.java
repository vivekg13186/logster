package com.logster;

import com.logster.ui.Icons;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.*;

import static com.logster.ui.Icons.previewIcon;

public class FileContentViewer extends ClosableTabPanel {

    public FileContentViewer(JTabbedPane tabbedPane, File file, int highlightLine,int startOffset,int endOffset) throws IOException, BadLocationException {
        super(tabbedPane, file.getName(), previewIcon);
        int start = Math.max(highlightLine - 2000, 1); // never 0
        int end = highlightLine + 2000;
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        int lineNumber = 0;

        while ((line = reader.readLine()) != null) {
            lineNumber++;
            if (lineNumber >= start && lineNumber <= end) {
                builder.append(line).append("\n");
            }
            if (lineNumber > end) break;
        }
        reader.close();
        RSyntaxTextArea textArea = new RSyntaxTextArea();
        textArea.setText(builder.toString());
                textArea.setCodeFoldingEnabled(true);
        textArea.setEditable(false);



            int offset = textArea.getLineStartOffset(highlightLine - start);
            textArea.setCaretPosition(offset);
            Rectangle viewRect = textArea.modelToView(offset);
            if (viewRect != null) {
                textArea.scrollRectToVisible(viewRect);
            }


        RTextScrollPane scrollPane = new RTextScrollPane(textArea);
        Gutter gutter = scrollPane.getGutter();
        gutter.setLineNumberingStartIndex(start);
        gutter.setBookmarkingEnabled(true);
        gutter.addLineTrackingIcon(highlightLine - start, Icons.searchingIconMarker);
        JLabel fileLabel = new JLabel(file.getAbsolutePath());
        fileLabel.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
        setLayout(new BorderLayout());
        add(fileLabel,BorderLayout.NORTH);
        add(scrollPane,BorderLayout.CENTER);

     
    }
}
