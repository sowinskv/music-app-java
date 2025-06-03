package com.example.mp5jpa.GUI; // Upewnij się, że to jest poprawny pakiet

import com.example.mp5jpa.model.Song;
import com.example.mp5jpa.service.MusicService;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList; // Import dla ArrayList
import java.util.stream.Collectors; // Import dla Stream API

@Component
public class MainApp {

    private final MusicService musicService;

    private JFrame frame;
    private JPanel cards;
    private CardLayout cardLayout;
    private JPanel sidebarPanel;

    private Clip audioClip;
    private JLabel songCoverLabel;
    private JLabel songTitleLabel;

    private static final String DEFAULT_COVER_PATH = "/images/default_cover.jpg";

    private Timer sidebarAnimationTimer;
    private final int SIDEBAR_FULL_WIDTH = 180;
    private final int ANIMATION_STEP = 10;
    private final int ANIMATION_DELAY = 15;
    private boolean isSidebarVisible = true;
    private int currentSidebarWidth = SIDEBAR_FULL_WIDTH;

    private JButton playPauseButton;
    private JButton rewindButton;
    private JButton forwardButton;
    private ImageIcon playIcon;
    private ImageIcon pauseIcon;
    private ImageIcon rewindIcon;
    private ImageIcon forwardIcon;

    private java.util.List<Song> allSongsMasterList;
    private JPanel songListContentPanel;
    private JTextField searchField;

    private JLabel playerEmptyStateLabel;
    private JPanel actualControlsPanel;


    public MainApp(MusicService musicService) {
        this.musicService = musicService;
        loadPlayerIcons();
        allSongsMasterList = musicService.getAllSongs();
    }

    private void loadPlayerIcons() {
        playIcon = loadImageIcon("/images/resume.png", 60, 60);
        pauseIcon = loadImageIcon("/images/pause.png", 60, 60);
        rewindIcon = loadImageIcon("/images/rewind.png", 40, 40);
        forwardIcon = loadImageIcon("/images/forward.png", 40, 40);

        if (playIcon == null) System.err.println("Play icon not found!");
        if (pauseIcon == null) System.err.println("Pause icon not found!");
        if (rewindIcon == null) System.err.println("Rewind icon not found!");
        if (forwardIcon == null) System.err.println("Forward icon not found!");
    }

    private ImageIcon loadImageIcon(String path, int width, int height) {
        Image img = loadImage(path);
        if (img != null) {
            return new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_SMOOTH));
        }
        return null;
    }

    public void createAndShowGUI() {
        frame = new JFrame("Music Library");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        BackgroundPanel mainContentPane = new BackgroundPanel(loadImage("/images/library_bg.jpg"));
        mainContentPane.setLayout(new BorderLayout());

        createSidebarPanel();

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        createMainPanel();
        createLibraryPanel();
        createPlayerPanel();

        mainContentPane.add(sidebarPanel, BorderLayout.WEST);
        mainContentPane.add(cards, BorderLayout.CENTER);
        frame.setContentPane(mainContentPane);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        updatePlayPauseButton();
        updatePlayerControlsVisibility();
    }

    private void createSidebarPanel() {
        sidebarPanel = new TransparentSidebarPanel(new Color(255, 255, 255, 70));
        sidebarPanel.setPreferredSize(new Dimension(currentSidebarWidth, 0));

        JButton homeButton = createSidebarButton("Home / Menu");
        homeButton.addActionListener(e -> switchTo("MAIN"));

        JButton libraryButton = createSidebarButton("Music Library");
        libraryButton.addActionListener(e -> switchTo("LIBRARY"));

        JButton playerButton = createSidebarButton("Now Playing");
        playerButton.addActionListener(e -> switchTo("PLAYER"));

        sidebarPanel.add(homeButton);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        sidebarPanel.add(libraryButton);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        sidebarPanel.add(playerButton);
    }

    // ZMIANA: Używamy dedykowanej klasy SidebarButton zamiast GlassButton
    private JButton createSidebarButton(String text) {
        SidebarButton button = new SidebarButton(text);
        button.setFont(new Font("Helvetica", Font.PLAIN, 14));
        return button;
    }

    public void toggleSidebar() {
        if (sidebarAnimationTimer != null && sidebarAnimationTimer.isRunning()) {
            return;
        }
        isSidebarVisible = !isSidebarVisible;
        sidebarAnimationTimer = new Timer(ANIMATION_DELAY, e -> {
            if (isSidebarVisible) {
                currentSidebarWidth += ANIMATION_STEP;
                if (currentSidebarWidth >= SIDEBAR_FULL_WIDTH) {
                    currentSidebarWidth = SIDEBAR_FULL_WIDTH;
                    ((Timer) e.getSource()).stop();
                }
            } else {
                currentSidebarWidth -= ANIMATION_STEP;
                if (currentSidebarWidth <= 0) {
                    currentSidebarWidth = 0;
                    ((Timer) e.getSource()).stop();
                }
            }
            sidebarPanel.setPreferredSize(new Dimension(currentSidebarWidth, sidebarPanel.getHeight()));
            frame.getContentPane().revalidate();
            frame.getContentPane().repaint();
        });
        sidebarAnimationTimer.start();
    }

    private void createMainPanel() {
        JPanel mainPanel = new BackgroundPanel(loadImage("/images/library_bg.jpg"));
        mainPanel.setLayout(new GridBagLayout());

        JLabel welcomeLabel = new JLabel("Welcome to Music Library!");
        welcomeLabel.setFont(loadFont("/fonts/cutefont.ttf", 48f));
        welcomeLabel.setForeground(Color.BLACK);

        JButton toggleButton = new GlassButton("Toggle Sidebar", new Color(200,200,200,100), 18f);
        toggleButton.setFont(new Font("Helvetica", Font.PLAIN, 14));
        toggleButton.addActionListener(e -> toggleSidebar());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10,10,10,10);
        mainPanel.add(welcomeLabel, gbc);

        gbc.gridy = 1;
        mainPanel.add(toggleButton, gbc);

        cards.add(mainPanel, "MAIN");
    }

    private void createLibraryPanel() {
        JPanel libraryPanel = new BackgroundPanel(loadImage("/images/library_bg.jpg"));
        libraryPanel.setLayout(new BorderLayout(0, 10));
        libraryPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        searchField = new JTextField(25);
        searchField.setFont(new Font("Helvetica", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200, 150), 1),
                new EmptyBorder(5, 8, 5, 8)
        ));
        searchField.setOpaque(false);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { filterSongs(); }
            public void removeUpdate(DocumentEvent e) { filterSongs(); }
            public void insertUpdate(DocumentEvent e) { filterSongs(); }
        });
        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(searchField);
        libraryPanel.add(searchPanel, BorderLayout.NORTH);

        if (songListContentPanel == null) {
            songListContentPanel = new JPanel();
            songListContentPanel.setLayout(new BoxLayout(songListContentPanel, BoxLayout.Y_AXIS));
            songListContentPanel.setOpaque(false);
        }

        updateSongListPanel(allSongsMasterList);

        JScrollPane scrollPane = new JScrollPane(songListContentPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));

        libraryPanel.add(scrollPane, BorderLayout.CENTER);
        cards.add(libraryPanel, "LIBRARY");
    }

    private void filterSongs() {
        String searchText = searchField.getText().toLowerCase().trim();
        if (searchText.isEmpty()) {
            updateSongListPanel(allSongsMasterList);
        } else {
            java.util.List<Song> filteredSongs = allSongsMasterList.stream()
                    .filter(song -> song.getTitle().toLowerCase().contains(searchText) ||
                            (song.getArtist() != null && song.getArtist().getName().toLowerCase().contains(searchText)))
                    .collect(Collectors.toList());
            updateSongListPanel(filteredSongs);
        }
    }

    private void updateSongListPanel(java.util.List<Song> songsToShow) {
        songListContentPanel.removeAll();
        for (Song song : songsToShow) {
            SongListItemPanel songPanel = new SongListItemPanel(song);
            songListContentPanel.add(songPanel);
            songListContentPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        }
        songListContentPanel.revalidate();
        songListContentPanel.repaint();
    }


    private void createPlayerPanel() {
        JPanel playerPanel = new BackgroundPanel(loadImage("/images/library_bg.jpg"));
        playerPanel.setLayout(new BorderLayout());
        playerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        songCoverLabel = new JLabel();
        songCoverLabel.setHorizontalAlignment(SwingConstants.CENTER);
        // Initially, no cover or text, this will be shown when a song plays
        playerPanel.add(songCoverLabel, BorderLayout.CENTER);


        songTitleLabel = new JLabel("", SwingConstants.CENTER);
        songTitleLabel.setForeground(Color.BLACK);
        songTitleLabel.setFont(new Font("Helvetica", Font.ITALIC, 20));
        songTitleLabel.setBorder(new EmptyBorder(20, 0, 10, 0));
        playerPanel.add(songTitleLabel, BorderLayout.NORTH);

        // Panel for actual player controls (rewind, play/pause, forward)
        actualControlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        actualControlsPanel.setOpaque(false);
        actualControlsPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        rewindButton = new JButton(rewindIcon);
        stylePlayerButton(rewindButton);
        rewindButton.addActionListener(e -> rewindAudio());

        playPauseButton = new JButton();
        stylePlayerButton(playPauseButton);
        playPauseButton.addActionListener(e -> toggleAudioPlayback());

        forwardButton = new JButton(forwardIcon);
        stylePlayerButton(forwardButton);
        forwardButton.addActionListener(e -> forwardAudio());

        actualControlsPanel.add(rewindButton);
        actualControlsPanel.add(playPauseButton);
        actualControlsPanel.add(forwardButton);

        playerEmptyStateLabel = new JLabel("<html><div style='text-align: center;'>Oh oh... nothing to show here yet.<br>Go to your library & pick a track!</div></html>", SwingConstants.CENTER);
        playerEmptyStateLabel.setFont(new Font("Helvetica", Font.PLAIN, 16));
        playerEmptyStateLabel.setForeground(Color.BLACK);
        playerEmptyStateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        playerEmptyStateLabel.setVerticalAlignment(SwingConstants.CENTER);

        JPanel southPanelContainer = new JPanel(new CardLayout());
        southPanelContainer.setOpaque(false);
        southPanelContainer.add(actualControlsPanel, "CONTROLS");
        southPanelContainer.add(playerEmptyStateLabel, "EMPTY_STATE");

        playerPanel.add(southPanelContainer, BorderLayout.SOUTH);

        cards.add(playerPanel, "PLAYER");

        updatePlayerControlsVisibility();
    }

    private void updatePlayerControlsVisibility() {
        if (cards == null || playerEmptyStateLabel == null || actualControlsPanel == null) return; // Guard clause

        CardLayout cl = (CardLayout) ((JPanel)actualControlsPanel.getParent()).getLayout(); // Get CardLayout from southPanelContainer
        JPanel southPanel = (JPanel) actualControlsPanel.getParent();

        if (audioClip == null || !audioClip.isOpen()) { // No song loaded or clip is closed
            // Show empty state message, hide controls
            songCoverLabel.setIcon(null); // Clear cover art
            songCoverLabel.setText("");   // Clear any "No Cover Art" text
            songTitleLabel.setText("");   // Clear song title
            cl.show(southPanel, "EMPTY_STATE");
        } else {
            // Show controls, hide empty state message
            cl.show(southPanel, "CONTROLS");
        }
        // Ensure play/pause button icon is also updated
        updatePlayPauseButton();
    }

    private void stylePlayerButton(JButton button) {
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        //button.setPreferredSize(new Dimension(50, 50));
    }

    private void updatePlayPauseButton() {
        if (playPauseButton == null) return;

        if (audioClip != null && audioClip.isRunning()) {
            playPauseButton.setIcon(pauseIcon);
            playPauseButton.setToolTipText("Pause");
        } else {
            playPauseButton.setIcon(playIcon);
            playPauseButton.setToolTipText("Play");
        }
    }

    private void switchTo(String cardName) {
        cardLayout.show(cards, cardName);
    }

    private void playAudio(Song song) {
        try {
            if (audioClip != null) {
                audioClip.stop();
                audioClip.close();
            }

            String audioPath = song.getAudioUrl();
            if (audioPath == null || audioPath.isEmpty()) {
                throw new IOException("Audio path is missing for song: " + song.getTitle());
            }
            InputStream audioStream = MainApp.class.getResourceAsStream(audioPath);
            if (audioStream == null) {
                throw new IOException("Could not find audio file: " + audioPath);
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(audioStream));

            audioClip = AudioSystem.getClip();
            audioClip.open(ais);
            audioClip.start();
            updatePlayPauseButton();
            updatePlayerControlsVisibility();

            audioClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    if (audioClip != null && audioClip.getMicrosecondPosition() >= audioClip.getMicrosecondLength()) {
                        audioClip.setMicrosecondPosition(0);
                    }
                    SwingUtilities.invokeLater(this::updatePlayPauseButton);
                } else if (event.getType() == LineEvent.Type.START) {
                    SwingUtilities.invokeLater(() -> {
                        updatePlayPauseButton();
                        updatePlayerControlsVisibility();
                    });
                }
            });

            String coverPath = song.getCoverImageUrl(); // Zakładamy, że ścieżka jest już poprawna np. "/images/tutu.jpg"
            Image coverImage = loadImage(coverPath);
            if (coverImage == null) {
                coverImage = loadImage(DEFAULT_COVER_PATH);
            }

            if (coverImage != null) {
                songCoverLabel.setIcon(new ImageIcon(coverImage.getScaledInstance(250, 250, Image.SCALE_SMOOTH)));
            } else {
                songCoverLabel.setIcon(null);
                songCoverLabel.setText("No Cover Art");
                songCoverLabel.setForeground(Color.BLACK);
            }

            songTitleLabel.setText(song.getTitle() + " by " + (song.getArtist() != null ? song.getArtist().getName() : "Unknown Artist"));
            switchTo("PLAYER");

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Could not play audio for: " + song.getTitle() + "\nError: " + e.getMessage(), "Playback Error", JOptionPane.ERROR_MESSAGE);
            if (audioClip != null) {
                audioClip.close();
                audioClip = null;
            }
            updatePlayPauseButton();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "An unexpected error occurred with song: " + song.getTitle(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        finally {
            SwingUtilities.invokeLater(() -> {
                updatePlayPauseButton();
                updatePlayerControlsVisibility();
            });
        }
    }

    private void toggleAudioPlayback() {
        if (audioClip != null) {
            if (audioClip.isRunning()) {
                audioClip.stop();
            } else {
                if (audioClip.getMicrosecondPosition() >= audioClip.getMicrosecondLength()) {
                    audioClip.setMicrosecondPosition(0);
                }
                audioClip.start();
            }
            updatePlayPauseButton();
        }
        else {
            updatePlayerControlsVisibility();
        }
    }

    private void rewindAudio() {
        if (audioClip != null) {
            long newPosition = audioClip.getMicrosecondPosition() - 5000000; // Przewiń o 5 sekund
            audioClip.setMicrosecondPosition(Math.max(0, newPosition));
        }
    }

    private void forwardAudio() {
        if (audioClip != null) {
            long newPosition = audioClip.getMicrosecondPosition() + 5000000; // Przewiń o 5 sekund
            audioClip.setMicrosecondPosition(Math.min(audioClip.getMicrosecondLength(), newPosition));
        }
    }

    private Image loadImage(String path) {
        try {
            if (path == null || path.isEmpty()) return null;
            InputStream is = MainApp.class.getResourceAsStream(path);
            if (is == null) {
                System.err.println("Could not find image: " + path);
                return null;
            }
            return ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Font loadFont(String path, float size) {
        try {
            InputStream is = MainApp.class.getResourceAsStream(path);
            if (is == null) {
                System.err.println("Could not find font: " + path);
                return new Font("Helvetica", Font.PLAIN, (int) size);
            }
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            return font.deriveFont(size);
        } catch (Exception e) {
            e.printStackTrace();
            return new Font("Helvetica", Font.PLAIN, (int) size);
        }
    }

    // --- Klasy Wewnętrzne ---
    class BackgroundPanel extends JPanel {
        private final Image backgroundImage;
        public BackgroundPanel(Image image) { this.backgroundImage = image; }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    class SongListItemPanel extends JPanel {
        private Song song;
        private boolean isHovered = false;
        private final Color defaultBackgroundColor = new Color(255, 255, 255, 20);
        private final Color hoverBackgroundColor = new Color(255, 255, 255, 50);
        public SongListItemPanel(Song song) {
            this.song = song;
            setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            JLabel songTitleLabel = new JLabel(song.getTitle());
            songTitleLabel.setFont(new Font("Helvetica", Font.ITALIC, 16));
            songTitleLabel.setForeground(Color.BLACK);
            JLabel artistNameLabel = new JLabel("by " + (song.getArtist() != null ? song.getArtist().getName() : "Unknown Artist"));
            artistNameLabel.setFont(new Font("Helvetica", Font.PLAIN, 14));
            artistNameLabel.setForeground(Color.BLACK);
            add(songTitleLabel);
            add(artistNameLabel);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { playAudio(song); }
                @Override public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
            });
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (isHovered) { g2d.setColor(hoverBackgroundColor); } else { g2d.setColor(defaultBackgroundColor); }
            g2d.fillRoundRect(5, 2, getWidth() - 10, getHeight() - 4, 15, 15);
            g2d.dispose();
        }
        @Override public Dimension getPreferredSize() { return new Dimension(super.getPreferredSize().width, 50); }
        @Override public Dimension getMaximumSize() { return new Dimension(Short.MAX_VALUE, getPreferredSize().height); }
    }

    class GlassButton extends JButton {
        private final Color originalBgColor;
        private Color hoverBgColor;
        private Color pressedBgColor;
        private float fontSize;
        private boolean isHovered = false;
        private boolean isPressed = false;
        public GlassButton(String text, Color bgColor, float fontSize) {
            super(text);
            this.originalBgColor = bgColor;
            this.fontSize = fontSize;
            this.hoverBgColor = calculateHoverColor(bgColor);
            this.pressedBgColor = calculatePressedColor(bgColor);
            setOpaque(false); setContentAreaFilled(false); setBorderPainted(false);
            setForeground(Color.BLACK);
            setFont(loadFont("/fonts/cutefont.ttf", this.fontSize));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 20, 10, 20));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { isHovered = false; isPressed = false; repaint(); }
                @Override public void mousePressed(MouseEvent e) { isPressed = true; repaint(); }
                @Override public void mouseReleased(MouseEvent e) { isPressed = false; repaint(); }
            });
        }
        private Color calculateHoverColor(Color color) {
            int alpha = Math.min(255, color.getAlpha() + 40);
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        }
        private Color calculatePressedColor(Color color) {
            int alpha = Math.min(255, color.getAlpha() + 60);
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.min(255, originalBgColor.getAlpha() + 80));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color currentBgColor = originalBgColor;
            if (isPressed) { currentBgColor = pressedBgColor; } else if (isHovered) { currentBgColor = hoverBgColor; }
            g2.setColor(currentBgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            FontMetrics fm = g2.getFontMetrics(); String text = getText();
            Rectangle stringBounds = fm.getStringBounds(text, g2).getBounds();
            int textX = (getWidth() - stringBounds.width) / 2;
            int textY = (getHeight() - stringBounds.height) / 2 + fm.getAscent();
            if (isPressed) { textX += 1; textY += 1; }
            g2.setColor(getForeground()); g2.drawString(text, textX, textY); g2.dispose();
        }
    }

    class TransparentSidebarPanel extends JPanel {
        private Color sidebarBackgroundColor;
        public TransparentSidebarPanel(Color bgColor) {
            this.sidebarBackgroundColor = bgColor;
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            //setPreferredSize(new Dimension(180, 0)); // Szerokość zarządzana przez sidebarAnimationTimer
            setBorder(new EmptyBorder(15, 10, 15, 10));
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(sidebarBackgroundColor);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.dispose();
        }
    }

    // NOWA ZMIANA: Klasa SidebarButton (zamiast createSidebarButton)
    class SidebarButton extends JButton {
        private Color backgroundColor;
        private Color hoverBackgroundColor;
        private Color pressedBackgroundColor;
        private boolean isHovered = false;
        private boolean isPressed = false;

        public SidebarButton(String text) {
            super(text);
            setForeground(Color.BLACK);
            setFont(new Font("Helvetica", Font.BOLD, 14));

            // Definiujemy kolory - możesz je dostosować
            backgroundColor = new Color(255, 255, 255, 0); // Całkowicie przezroczyste tło domyślnie
            hoverBackgroundColor = new Color(220, 220, 220, 100); // Jasnoszare, półprzezroczyste na hover
            pressedBackgroundColor = new Color(200, 200, 200, 150); // Nieco ciemniejsze przy wciśnięciu

            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 15, 10, 15));
            setAlignmentX(CENTER_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height + 10));


            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    isPressed = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    isPressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isPressed) {
                g2.setColor(pressedBackgroundColor);
            } else if (isHovered) {
                g2.setColor(hoverBackgroundColor);
            } else {
                g2.setColor(backgroundColor);
            }
            // Możesz rysować prostokąt lub zaokrąglony prostokąt
            g2.fillRect(0, 0, getWidth(), getHeight());
            // g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); // Jeśli chcesz zaokrąglenia

            // Rysowanie tekstu (odziedziczone z super.paintComponent lub rysowane ręcznie)
            // Aby tekst był poprawnie wycentrowany, pozwalamy domyślnej implementacji go narysować
            // ale ponieważ setContentAreaFilled(false), musimy sami go narysować
            super.paintComponent(g); // To wywoła rysowanie tekstu przez JButton, jeśli inne flagi są ustawione poprawnie
            // Jeśli tekst się nie pojawia, użyj poniższego kodu do ręcznego rysowania:
            /*
            FontMetrics fm = g2.getFontMetrics();
            String currentText = getText();
            Rectangle stringBounds = fm.getStringBounds(currentText, g2).getBounds();
            int textX = (getWidth() - stringBounds.width) / 2;
            int textY = (getHeight() - stringBounds.height) / 2 + fm.getAscent();

            if (isPressed) {
                textX += 1;
                textY += 1;
            }

            g2.setColor(getForeground());
            g2.drawString(currentText, textX, textY);
            */
            g2.dispose();
        }
    }
}