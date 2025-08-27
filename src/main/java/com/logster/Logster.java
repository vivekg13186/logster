package com.logster;

import com.formdev.flatlaf.FlatIntelliJLaf;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Logster extends JFrame implements SearchProgressListener {

    private static final Logger LOGGER = LogManager.getLogger();
    private final DateDetection dateDetection;
    private final JTextField searchBox = new JTextField(30);

    private final SearchResultTM tableModel = new SearchResultTM(new ArrayList<>());
    private final JTable resultTable = new JTable(tableModel);

    private final DateField fromDateField = new DateField();
    private final DateField toDateField = new DateField();

    private final JTabbedPane viewerTabs = new JTabbedPane();
    private final JTextField locationTextBox = new JTextField();


    final JButton searchBtn = new JButton("Search");
    final JCheckBox useDate = new JCheckBox();

    private final JLabel statusLabel = new JLabel();
    SearchController controller = new SearchController();

    private final SettingsDialog settingsDialog = new SettingsDialog();
    private final JProgressBar progressBar = new JProgressBar();

    public void setupMenu() {
        JMenu fileMenu = new JMenu("File");
        JMenuItem openFolder = new JMenuItem("Index folder");
        JMenuItem settingsMenu = new JMenuItem("Settings");
        fileMenu.add(openFolder);
        fileMenu.add(settingsMenu);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
        openFolder.addActionListener(_ -> chooseFolder());
        settingsMenu.addActionListener(_ -> showSettingsDialog());
    }

    private void showSettingsDialog() {
        settingsDialog.setVisible(true);
    }

    public void loadIcon() {

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


        JButton openFolderBtn = new JButton("Select folder");
        openFolderBtn.addActionListener((_) -> {
            chooseFolder();
        });
        JPanel searchRow = Util.rows(searchBox, searchBtn, new JLabel("Use range"), useDate, fromDateField, toDateField);
        JPanel folderPanel = Util.rows(locationTextBox, openFolderBtn);
        JPanel topPanel = Util.columns(folderPanel, searchRow);

        topPanel.add(searchRow);

        resultTable.setAutoCreateRowSorter(true);
        resultTable.getColumn(resultTable.getColumnName(1)).setCellRenderer(new SearchResultRenderer());

        Util.setLineColWidth(resultTable);

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        searchPanel.add(new JScrollPane(resultTable), BorderLayout.CENTER);
        searchPanel.add(topPanel, BorderLayout.NORTH);
        viewerTabs.add(searchPanel, "Search");

        add(viewerTabs, BorderLayout.CENTER);


        JButton stopSearchBtn =new JButton("stop");
        stopSearchBtn.addActionListener((_)->
        {
            controller.cancel();
        });
        JPanel progressPanel = Util.rows(statusLabel,progressBar,stopSearchBtn);
        progressPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        add(progressPanel, BorderLayout.SOUTH);


        searchBtn.addActionListener(_ -> performSearch());
        resultTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                int row = resultTable.getSelectedRow();
                if (row != -1) {
                    int modelRow = resultTable.convertRowIndexToModel(row);
                    SearchResult searchResult = tableModel.getSearchResultAt(modelRow);
                    if (evt.getClickCount() == 2) openViewer(searchResult);
                }
            }
        });
        setupMenu();
        dateDetection = new DateDetection();
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
            locationTextBox.setText(selectedFolder.getAbsolutePath());

        }
    }


    private void performSearch() {
        controller.reset();

        if (searchBox.getText() == null || locationTextBox.getText() == null || searchBox.getText().isBlank() || locationTextBox.getText().isBlank()) {
            return;
        }
        tableModel.clear();
        SearchProgressListener listener = this;
        new SwingWorker<>() {
            protected String doInBackground() throws Exception {

                /*if(useDate.isSelected()){


                }*/

                SimpleFileSearch search = new SimpleFileSearch();

                search.search(locationTextBox.getText(), searchBox.getText(), listener,controller);
                return "OK";
            }

        }.execute();


    }

    private void openViewer(SearchResult r) {

        new FileContentViewer(viewerTabs, new File(r.filePath), r.lineNumber);
    }

    public static void loadFont() {
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

    public static void main(String[] args) {

        Configurator.setLevel("com.logster", Level.ERROR);
        System.setProperty("flatlaf.useWindowDecorations", "false");
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


    @Override
    public void onResultFound(SearchResult result,int noOfFiles,int processedFiles) {
        SwingUtilities.invokeLater(() -> {
            tableModel.addSearchResult(result);
            float progress = ((float) processedFiles /(float)noOfFiles)*100;
            System.out.println(Math.floor(progress));
            progressBar.setValue((int) Math.floor(progress));
            statusLabel.setText(String.format("%d results ,%d of %d files ,searching....", tableModel.getRowCount(),processedFiles,noOfFiles));
        });

    }

    @Override
    public void onSearchStarted() {
        resultTable.clearSelection();
        statusLabel.setText("collecting file information..");
        progressBar.setValue(0);
    }

    @Override
    public void onSearchCompleted(long timeTakenInSeconds) {
        SwingUtilities.invokeLater(() -> {
            if(!controller.isCancelled()) {
                progressBar.setValue(100);
                statusLabel.setText(String.format("%d results in %d seconds", tableModel.getRowCount(), timeTakenInSeconds));
            }
        });

    }

    @Override
    public void onCancelled() {
        statusLabel.setText("search cancelled");
    }
}