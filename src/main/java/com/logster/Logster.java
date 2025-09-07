package com.logster;

import com.formdev.flatlaf.FlatIntelliJLaf;

import com.logster.config.AppConfiguration;

import com.logster.config.DateFormatPanel;
import com.logster.config.IgnoreFileExtensionPanel;
import com.logster.search.*;
import com.logster.test.TestPanel;
import com.logster.ui.SearchPanel;
import com.logster.ui.SearchPanelListener;

import com.logster.ui.StatusBar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;


import static com.logster.ui.Icons.*;

public class Logster extends JFrame implements SearchProgressListener, SearchPanelListener {

    private static final Logger logger = LoggerFactory.getLogger(Logster.class);

    private final JTabbedPane viewerTabs = new JTabbedPane();

    final SearchController controller = new SearchController();

    final StatusBar statusBar = new StatusBar();
    final SearchPanel searchPanel = new SearchPanel(viewerTabs);


    public void loadIcon() {
        try (InputStream is = Logster.class.getClassLoader().getResourceAsStream("icons/logo.png")) {
            if (is == null) {
                throw new IllegalStateException("Font not found!");
            }
            setIconImage(ImageIO.read(is));
        } catch (Exception e) {
            logger.error("error", e);
        }
    }

    public Logster() {
        super("Logster");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        loadIcon();
        searchPanel.setListener(this);

        add(viewerTabs, BorderLayout.CENTER);
        statusBar.setOnSearchCancel(() -> {
            controller.cancel();

            statusBar.setState(StatusBar.State.SEARCH_CANCELLED);

        });
        add(statusBar, BorderLayout.SOUTH);
        setMenuBar();

    }


    public void setMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("File");
        JMenuItem testMenuItem = new JMenuItem("Text Regex", labIcon);
        testMenuItem.addActionListener((_) -> new TestPanel(viewerTabs));
        fileMenu.add(testMenuItem);
        JMenuItem fileExtensionMenuItem = new JMenuItem("Exclude File Types",extensionIcon);
        fileExtensionMenuItem.addActionListener((_) -> new IgnoreFileExtensionPanel(viewerTabs));
        fileMenu.add(fileExtensionMenuItem);

        JMenuItem dateFormatMenuItem = new JMenuItem("Add date formats",dateIcon);
        dateFormatMenuItem.addActionListener((_) -> new DateFormatPanel(viewerTabs));
        fileMenu.add(dateFormatMenuItem);

        menuBar.add(fileMenu);
    }


    private void openViewer(SearchResult r) {
        try {

            new FileContentViewer(viewerTabs, new File(r.filePath()), r.lineNumber(),1,10);
        } catch (Exception e) {
            logger.error("error", e);
        }

    }

    public static void loadFont() {
        try (InputStream is = Logster.class.getClassLoader().getResourceAsStream("fonts/Inter-Regular.ttf")) {
            if (is == null) {
                throw new IllegalStateException("Font not found!");
            }
            Font inter = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12f);
            UIManager.put("defaultFont", inter);

        } catch (Exception e) {
            logger.error("error", e);
        }
    }

    public static void main(String[] args) {
        AppConfiguration.loadSystemVars();
        System.setProperty("flatlaf.useWindowDecorations", "false");
        FlatIntelliJLaf.setup();
        loadFont();
        SwingUtilities.invokeLater(() -> new Logster().setVisible(true));
    }


    @Override
    public void onResultFound(SearchResult result ) {
        SwingUtilities.invokeLater(() -> {
            searchPanel.addSearchResult(result);

            String message = String.format("%d results  searching....", searchPanel.getRowCount());
            statusBar.setStatus(message);

        });

    }

    @Override
    public void onSearchStarted() {
        searchPanel.clearSearchResult();
        statusBar.setState(StatusBar.State.IN_PROGRESS);
        statusBar.setStatus("searching...");
    }

    @Override
    public void onSearchCompleted(long timeTakenInSeconds) {
        SwingUtilities.invokeLater(() -> {

            statusBar.setStatus(String.format("%d results in %d seconds", searchPanel.getRowCount(), timeTakenInSeconds));
            statusBar.setState(StatusBar.State.SEARCH_COMPLETED);
        });
    }


    @Override
    public void onMaxLimit() {
        statusBar.setState(StatusBar.State.MAX_SEARCH_RESULT);
    }

    @Override
    public void onSearchBtnClick() {
        controller.reset();
        String searchQuery = searchPanel.getSearchQuery();
        String searchLocation = searchPanel.getSearchTLocation();
        if (searchQuery.isEmpty() || searchLocation.isEmpty()) {
            return;
        }


        searchPanel.clearSearchResult();
        SearchProgressListener listener = this;
        new SwingWorker<>() {
            protected String doInBackground()  {
                try {

                    SimpleFileSearch search = new SimpleFileSearch(AppConfiguration.ignoreFileExtension);
                    if (searchPanel.useDateForSearch()) {
                        long start = searchPanel.getStartTime();
                        long end = searchPanel.getEndTime();

                        search.search(searchLocation, searchQuery, listener, controller, DateDetection.dateDetection, start, end);
                    } else {

                        search.search(searchLocation, searchQuery, listener, controller, null, -1, -1);
                    }
                }catch (Exception e){
                    logger.error("error ",e);
                }

                return "OK";
            }

        }.execute();

    }

    @Override
    public void onRowClick(SearchResult searchResult) {
        openViewer(searchResult);
    }
}