package com.logster;
import com.formdev.flatlaf.FlatIntelliJLaf;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
public class Logster extends JFrame {

    private static final Logger LOGGER = LogManager.getLogger();
    private   final  DateDetection dateDetection;
    private final JTextField searchBox = new JTextField(30);

    private final SearchResultTM tableModel =new SearchResultTM(new ArrayList<>());
    private final  JTable resultTable = new JTable(tableModel);

    private final DateField fromDateField= new DateField();
    private final DateField toDateField= new DateField();

    private final JTabbedPane viewerTabs = new JTabbedPane();

    private FileIndexer indexer;
    private FileSearcher searcher;
    private final String indexDir = "logster_index";
    final JLabel searchLabel  = new JLabel("File->Open Folder");
    final JButton searchBtn=new JButton("Search");
    final JCheckBox useDate= new JCheckBox();
    private final JProgressBar progressBar =new JProgressBar();

    private final SettingsDialog settingsDialog = new SettingsDialog();
    public void setupMenu(){
        JMenu fileMenu = new JMenu("File");
        JMenuItem openFolder = new JMenuItem("Index folder");
        JMenuItem settingsMenu = new JMenuItem("Settings");
        fileMenu.add(openFolder);
        fileMenu.add(settingsMenu);

        JMenuBar menuBar =new JMenuBar();
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
        openFolder.addActionListener(_ -> chooseFolder());
        settingsMenu.addActionListener(_->showSettingsDialog());
    }

    private void  showSettingsDialog(){
        settingsDialog.setVisible(true);
    }
    public Logster() throws IOException {
        super("Logster");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        try {
            setIconImage(ImageIO.read(Objects.requireNonNull(Logster.class.getResource("../../logo.png"))));
        } catch (IOException e) {
             LOGGER.error(e);
        }

         JPanel searchRow = Util.rows(searchBox,searchBtn,new JLabel("Use range"),useDate,fromDateField,toDateField);
        JPanel topPanel = Util.columns(searchLabel,searchRow);

        topPanel.add(searchRow);
        searchBtn.setEnabled(false);

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
        dateDetection =new DateDetection();
        LocalDateTime date = LocalDateTime.now();
        fromDateField.setDate(date.plusMinutes(10));
        toDateField.setDate(date);
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

                    indexer = new FileIndexer(indexDir,dateDetection);
                    indexer.indexFolder(folder);
                    indexer.close();
                    searcher = new FileSearcher(indexDir);
                } catch (Exception e) {
                    LOGGER.error(e); }
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
                if(useDate.isSelected()){
                    LocalDateTime from = fromDateField.getDate();
                    LocalDateTime to = toDateField.getDate();
                    if(from!=null && to !=null){
                        return  searcher.search(searchBox.getText(), 10000, Util.toEpochMilli(from),Util.toEpochMilli(to));
                    }

                }
                return searcher.search(searchBox.getText(), 10000);
            }

            protected void done() {
                try {
                    tableModel.clear();
                    List<SearchResult> results = get();
                    for (SearchResult r : results) tableModel.addSearchResult(r);
                } catch (Exception e) {
                    LOGGER.error(e);
                }
            }
        };
        worker.execute();
    }

    private void openViewer(SearchResult r) {

       new FileContentViewer(viewerTabs,new File(r.filePath),r.lineNumber);
    }

    public static void main(String[] args) throws IOException, FontFormatException {

        Configurator.setLevel("com.logster",Level.ERROR);
        System.setProperty("flatlaf.useWindowDecorations","false");
        FlatIntelliJLaf.setup();
        File ttfFile = new File(Objects.requireNonNull(Logster.class.getResource("../../fonts/Inter-Regular.ttf")).getFile()); // path to your Inter TTF
        Font interFont = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(ttfFile)).deriveFont(12f);


        UIManager.getLookAndFeelDefaults().put("defaultFont", interFont);
        SwingUtilities.invokeLater(() -> {
            try {
                new Logster().setVisible(true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }


}