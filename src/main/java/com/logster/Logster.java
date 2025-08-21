package com.logster;
import com.formdev.flatlaf.FlatIntelliJLaf;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Logster extends JFrame {
    private final JTextField searchBox = new JTextField(30);

    private final SearchResultTM tableModel =new SearchResultTM(new ArrayList<>());
    private final  JTable resultTable = new JTable(tableModel);



    private final JTabbedPane viewerTabs = new JTabbedPane();

    private FileIndexer indexer;
    private FileSearcher searcher;
    private final String indexDir = "lucene_index";
    JLabel searchLabel;
    JButton searchBtn;
    private final JProgressBar progressBar =new JProgressBar();

    public void setupMenu(){
        JMenu fileMenu = new JMenu("File");
        JMenuItem openFolder = new JMenuItem("Open folder");
        JMenuItem settingsMenu = new JMenuItem("Settings");
        fileMenu.add(openFolder);
        fileMenu.add(settingsMenu);

        JMenuBar menuBar =new JMenuBar();
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
        openFolder.addActionListener(_ -> chooseFolder());
    }
    public Logster() {
        super("Logster");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        try {
            setIconImage(ImageIO.read(Objects.requireNonNull(Logster.class.getResource("../../logo1.png"))));
        } catch (IOException _) {
             ;
        }
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        searchLabel = new JLabel("File->Open Folder");
        searchLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchLabel.setBorder(BorderFactory.createEmptyBorder(0,  2,0, 0));
        topPanel.add(searchLabel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT); // align left
        searchRow.add(searchBox);
        searchBtn = new JButton("Search");
        searchBtn.setEnabled(false);
        searchRow.add(searchBtn);
        topPanel.add(searchRow);


        resultTable.setAutoCreateRowSorter(true);

        Util.setLineColWidth(resultTable);

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        searchPanel.add(new JScrollPane(resultTable),BorderLayout.CENTER);
        searchPanel.add(topPanel, BorderLayout.NORTH);
        viewerTabs.add(searchPanel,"Search");

        add(viewerTabs, BorderLayout.CENTER);

        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        progressPanel.setPreferredSize(new Dimension(400, 30));
        progressPanel.add(progressBar, BorderLayout.CENTER);


        add(progressPanel,BorderLayout.SOUTH);


        searchBtn.addActionListener(_ -> performSearch());
        resultTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                int row = resultTable.getSelectedRow();
                if (row != -1) {
                    int modelRow = resultTable.convertRowIndexToModel(row);
                    SearchResult searchResult = tableModel.getSearchResultAt(modelRow);
                   if (evt.getClickCount() == 2) openViewer(searchResult);
            }
        }});
        setupMenu();
    }

    private void chooseFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = chooser.getSelectedFile();
            searchLabel.setText(selectedFolder.getAbsolutePath());
            indexFolder(selectedFolder);
        }
    }



    private void indexFolder(File folder) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            protected Void doInBackground() {
                try {

                    indexer = new FileIndexer(indexDir);
                    indexer.indexFolder(folder);
                    indexer.close();
                    searcher = new FileSearcher(indexDir);
                } catch (Exception _) {   }
                return null;
            }
            @Override
            protected void done() {
                searchBtn.setEnabled(true);
                progressBar.setIndeterminate(false);
                JOptionPane.showMessageDialog(Logster.this, "Indexing completed!");
            }
        };
        progressBar.setIndeterminate(true);
        searchBtn.setEnabled(false);
        worker.execute();
    }

    private void performSearch() {
        SwingWorker<List<SearchResult>, Void> worker = new SwingWorker<>() {
            protected List<SearchResult> doInBackground() throws Exception {
                if (searcher == null) return Collections.emptyList();
                return searcher.search(searchBox.getText(), 1000);
            }

            protected void done() {
                try {
                    tableModel.clear();

                    List<SearchResult> results = get();
                    for (SearchResult r : results) tableModel.addSearchResult(r);
                } catch (Exception _) {  }
            }
        };
        worker.execute();
    }

    private void openViewer(SearchResult r) {

       new FileContentViewer(viewerTabs,new File(r.filePath),r.lineNumber);
    }

    public static void main(String[] args) throws IOException, FontFormatException {
        System.setProperty("flatlaf.useWindowDecorations","false");
        FlatIntelliJLaf.setup();
        File ttfFile = new File(Objects.requireNonNull(Logster.class.getResource("../../fonts/Inter-Regular.ttf")).getFile()); // path to your Inter TTF
        Font interFont = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(ttfFile)).deriveFont(12f);


        UIManager.getLookAndFeelDefaults().put("defaultFont", interFont);
        SwingUtilities.invokeLater(() -> new Logster().setVisible(true));
    }


}