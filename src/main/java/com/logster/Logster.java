package com.logster;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.logster.config.ConfigPanel;
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
import java.util.ArrayList;
import java.util.Objects;

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

    public Logster() throws IOException {
        super("Logster");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        loadIcon();
        searchPanel.setListener(this);
        viewerTabs.add(new ConfigPanel(), "Config");
        add(viewerTabs, BorderLayout.CENTER);
        statusBar.setOnSearchCancel(new Runnable() {
            @Override
            public void run() {
                controller.cancel();
                statusBar.setProgress(0);
                statusBar.setState(StatusBar.State.SEARCH_CANCELLED);

            }
        });
        add(statusBar, BorderLayout.SOUTH);
        setMenuBar();

    }


    public void setMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("File");
        JMenuItem testMenuItem = new JMenuItem("Text Regex", new FlatSVGIcon("icons/lab.svg", 16, 16));
        testMenuItem.addActionListener((_) -> {
            new TestPanel(viewerTabs);
        });
        fileMenu.add(testMenuItem);
        menuBar.add(fileMenu);
    }


    private void openViewer(SearchResult r) {

        new FileContentViewer(viewerTabs, new File(r.filePath()), r.lineNumber());
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
    public void onResultFound(SearchResult result, int noOfFiles, int processedFiles) {
        SwingUtilities.invokeLater(() -> {
            searchPanel.addSearchResult(result);
            int y = processedFiles + 1;
            float progress = ((float) y / (float) noOfFiles) * 100;
            statusBar.setProgress((int) Math.floor(progress));
            String message = String.format("%d results ,%d of %d files ,searching....", searchPanel.getRowCount(), y, noOfFiles);
            statusBar.setStatus(message);

        });

    }

    @Override
    public void onSearchStarted() {
        searchPanel.clearSearchResult();
        statusBar.setStatus("collecting file information..");
        statusBar.setProgress(0);
        statusBar.setState(StatusBar.State.IN_PROGRESS);

    }

    @Override
    public void onSearchCompleted(long timeTakenInSeconds) {
        SwingUtilities.invokeLater(() -> {
            statusBar.setProgress(100);
            statusBar.setStatus(String.format("%d results in %d seconds", searchPanel.getRowCount(), timeTakenInSeconds));
            statusBar.setState(StatusBar.State.SEARCH_COMPLETED);
        });
    }


    @Override
    public void onMaxLimit(int limit) {
        statusBar.setState(StatusBar.State.MAX_SEARCH_RESULT);
        statusBar.setStatus("Search limit reached");
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
            protected String doInBackground() throws Exception {
                SimpleFileSearch search = new SimpleFileSearch(Objects.requireNonNullElse(ConfigPanel.getConfig().ignoreFileExtensions(), new ArrayList<>()));
                if (searchPanel.useDateForSearch()) {
                    long start = searchPanel.getStartTime();
                    long end = searchPanel.getEndTime();
                    search.search(searchLocation, searchQuery, listener, controller, DateDetection.dateDetection, start, end);
                } else {
                    search.search(searchLocation, searchQuery, listener, controller, null, -1, -1);
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