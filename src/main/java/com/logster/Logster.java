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


    private final String indexDir = "logster_index";
    final JLabel searchLabel  = new JLabel("File->Open Folder");
    final JButton searchBtn=new JButton("Search");
    final JCheckBox useDate= new JCheckBox();

    private final JLabel statusLabel  =new JLabel();

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
    public   void loadIcon(){

        try (InputStream is = Logster.class.getClassLoader().getResourceAsStream("icons/logo.png")) {
            if (is == null) {
                throw new IllegalStateException("Font not found!");
            }

            setIconImage(ImageIO.read(is));
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }
    public Logster() throws IOException {
        super("Logster");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        loadIcon();

         JPanel searchRow = Util.rows(searchBox,searchBtn,new JLabel("Use range"),useDate,fromDateField,toDateField);
        JPanel topPanel = Util.columns(searchLabel,searchRow);

        topPanel.add(searchRow);

        resultTable.setAutoCreateRowSorter(true);

        Util.setLineColWidth(resultTable);

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        searchPanel.add(new JScrollPane(resultTable),BorderLayout.CENTER);
        searchPanel.add(topPanel, BorderLayout.NORTH);
        viewerTabs.add(searchPanel,"Search");

        add(viewerTabs, BorderLayout.CENTER);


        JPanel progressPanel = Util.rows( statusLabel);
         progressPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


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

    private File selectedFolder;
    private void chooseFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
              selectedFolder = chooser.getSelectedFile();
            searchLabel.setText(selectedFolder.getAbsolutePath());

        }
    }





    private void performSearch() {
        statusLabel.setText("Searching ....");
        if(searchBox.getText()==null || selectedFolder==null || searchBox.getText().isBlank()){
            return;
        }
          tableModel.clear();
        SwingWorker< SearchStats , Void> worker = new SwingWorker<>() {
            protected  SearchStats  doInBackground() throws Exception {

                /*if(useDate.isSelected()){


                }*/

                SimpleFileSearch search =new SimpleFileSearch();

                List<SearchResult>searchResults =  search.search(selectedFolder.getAbsolutePath(),searchBox.getText(),1000);
                return  new SearchStats(search.getTimeTakenInSeconds(),searchResults);
            }

            protected void done() {
                try {

                    SearchStats searchStats = get();
                    statusLabel.setText(String.format("Time taken %d in seconds", searchStats.timeInSeconds));
                    List<SearchResult> results = searchStats.searchResults;
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

    public static void loadFont(){
        try (InputStream is = Logster.class.getClassLoader().getResourceAsStream("fonts/Inter-Regular.ttf")) {
            if (is == null) {
                throw new IllegalStateException("Font not found!");
            }
            Font inter = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12f);
            UIManager.put("defaultFont", inter);
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }
    public static void main(String[] args)  {

        Configurator.setLevel("com.logster",Level.ERROR);
        System.setProperty("flatlaf.useWindowDecorations","false");
        FlatIntelliJLaf.setup();

        loadFont();

        SwingUtilities.invokeLater(() -> {
            try {
                new Logster().setVisible(true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void updateStatus(String status){
        statusLabel.setText(status);
    }

}