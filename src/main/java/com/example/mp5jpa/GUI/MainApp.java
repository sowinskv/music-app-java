package com.example.mp5jpa.GUI;

import com.example.mp5jpa.model.Artist;
import com.example.mp5jpa.model.Song;
import com.example.mp5jpa.service.MusicService;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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

    private JPanel songListContentPanel;
    private JList<Artist> artistList;
    private List<Artist> allArtistsMasterList;

    private JLabel playerEmptyStateLabel;
    private JPanel actualControlsPanel;


    public MainApp(MusicService musicService) {
        this.musicService = musicService;
        loadPlayerIcons();
        allArtistsMasterList = musicService.getAllArtistsWithSongs();
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
        frame.setSize(1000, 700);
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

    private void createLibraryPanel() {
        JPanel libraryPanel = new BackgroundPanel(loadImage("/images/library_bg.jpg"));
        libraryPanel.setLayout(new BorderLayout(0, 10));
        libraryPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        DefaultListModel<Artist> artistListModel = new DefaultListModel<>();
        if (allArtistsMasterList != null) {
            allArtistsMasterList.forEach(artistListModel::addElement);
        }
        artistList = new JList<>(artistListModel);
        artistList.setCellRenderer(new ArtistListCellRenderer());
        artistList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        artistList.setOpaque(false);

        artistList.addListSelectionListener(this::onArtistSelectionChanged);

        JScrollPane artistScrollPane = new JScrollPane(artistList);
        artistScrollPane.setOpaque(false);
        artistScrollPane.getViewport().setOpaque(false);
        artistScrollPane.setBorder(BorderFactory.createTitledBorder("Artists"));

        if (songListContentPanel == null) {
            songListContentPanel = new JPanel();
            songListContentPanel.setLayout(new BoxLayout(songListContentPanel, BoxLayout.Y_AXIS));
            songListContentPanel.setOpaque(false);
        }
        JScrollPane songScrollPane = new JScrollPane(songListContentPanel);
        songScrollPane.setOpaque(false);
        songScrollPane.getViewport().setOpaque(false);
        songScrollPane.setBorder(BorderFactory.createTitledBorder("Songs"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, artistScrollPane, songScrollPane);
        splitPane.setDividerLocation(300);
        splitPane.setOpaque(false);

        libraryPanel.add(splitPane, BorderLayout.CENTER);
        cards.add(libraryPanel, "LIBRARY");
    }

    private void onArtistSelectionChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            Artist selectedArtist = artistList.getSelectedValue();
            if (selectedArtist != null) {
                Set<Song> songs = selectedArtist.getSongs();
                updateSongListPanel(songs);
            } else {
                updateSongListPanel(new ArrayList<>());
            }
        }
    }

    private void updateSongListPanel(Collection<Song> songsToShow) {
        songListContentPanel.removeAll();
        if (songsToShow.isEmpty()) {
            JLabel emptyLabel = new JLabel("Select an artist to see their songs.");
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            songListContentPanel.add(emptyLabel);
        } else {
            for (Song song : songsToShow) {
                SongListItemPanel songPanel = new SongListItemPanel(song);
                songListContentPanel.add(songPanel);
                songListContentPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }
        songListContentPanel.revalidate();
        songListContentPanel.repaint();
    }


    class ArtistListCellRenderer extends DefaultListCellRenderer {
        @Override
        public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            java.awt.Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Artist) {
                Artist artist = (Artist) value;
                setText(artist.getName());
                setFont(new Font("Helvetica", Font.BOLD, 14));
                setBorder(new EmptyBorder(5, 10, 5, 10));
            }

            if (!isSelected) {
                c.setBackground(new Color(255, 255, 255, 20));
            } else {
                c.setBackground(new Color(100, 150, 255, 100));
            }
            return c;
        }
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

    private void createPlayerPanel() {
        JPanel playerPanel = new BackgroundPanel(loadImage("/images/library_bg.jpg"));
        playerPanel.setLayout(new BorderLayout());
        playerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        songCoverLabel = new JLabel();
        songCoverLabel.setHorizontalAlignment(SwingConstants.CENTER);
        playerPanel.add(songCoverLabel, BorderLayout.CENTER);


        songTitleLabel = new JLabel("", SwingConstants.CENTER);
        songTitleLabel.setForeground(Color.BLACK);
        songTitleLabel.setFont(new Font("Helvetica", Font.ITALIC, 20));
        songTitleLabel.setBorder(new EmptyBorder(20, 0, 10, 0));
        playerPanel.add(songTitleLabel, BorderLayout.NORTH);

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
        if (cards == null || playerEmptyStateLabel == null || actualControlsPanel == null) return;

        CardLayout cl = (CardLayout) ((JPanel)actualControlsPanel.getParent()).getLayout();
        JPanel southPanel = (JPanel) actualControlsPanel.getParent();

        if (audioClip == null || !audioClip.isOpen()) {
            songCoverLabel.setIcon(null);
            songCoverLabel.setText("");
            songTitleLabel.setText("");
            cl.show(southPanel, "EMPTY_STATE");
        } else {
            cl.show(southPanel, "CONTROLS");
        }
        updatePlayPauseButton();
    }

    private void stylePlayerButton(JButton button) {
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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

            String coverPath = song.getCoverImageUrl();
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
            long newPosition = audioClip.getMicrosecondPosition() - 5000000;
            audioClip.setMicrosecondPosition(Math.max(0, newPosition));
        }
    }

    private void forwardAudio() {
        if (audioClip != null) {
            long newPosition = audioClip.getMicrosecondPosition() + 5000000;
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

            backgroundColor = new Color(255, 255, 255, 0);
            hoverBackgroundColor = new Color(220, 220, 220, 100);
            pressedBackgroundColor = new Color(200, 200, 200, 150);

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
            g2.fillRect(0, 0, getWidth(), getHeight());
            super.paintComponent(g);
            g2.dispose();
        }
    }
}