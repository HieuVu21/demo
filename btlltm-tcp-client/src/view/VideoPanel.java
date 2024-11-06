package view;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class VideoPanel extends JPanel {
    private ImageIcon currentFrame;
    private final int preferredWidth = 320;
    private final int preferredHeight = 240;
    private final String labelText;
    private boolean isConnected = false;

    public VideoPanel(String label) {
        this.labelText = label;
        setPreferredSize(new Dimension(preferredWidth, preferredHeight));
        setBackground(Color.BLACK);
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                label
        ));
    }

    public void setConnected(boolean connected) {
        this.isConnected = connected;
        repaint();
    }

    public void updateFrame(ImageIcon frame) {
        this.currentFrame = frame;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (currentFrame != null && isConnected) {
            Image img = currentFrame.getImage();
            int originalWidth = img.getWidth(this);
            int originalHeight = img.getHeight(this);

            if (originalWidth <= 0 || originalHeight <= 0) return;

            double scaleFactor = Math.min(
                    (double) getWidth() / originalWidth,
                    (double) getHeight() / originalHeight
            );

            int scaledWidth = (int) (originalWidth * scaleFactor);
            int scaledHeight = (int) (originalHeight * scaleFactor);

            int x = (getWidth() - scaledWidth) / 2;
            int y = (getHeight() - scaledHeight) / 2;

            g2d.drawImage(img, x, y, scaledWidth, scaledHeight, this);
        } else {
            // Draw placeholder when no video
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.setColor(Color.LIGHT_GRAY);
            String message = isConnected ? "No video signal" : "Disconnected";
            FontMetrics fm = g2d.getFontMetrics();
            int textX = (getWidth() - fm.stringWidth(message)) / 2;
            int textY = (getHeight() + fm.getAscent()) / 2;
            g2d.drawString(message, textX, textY);
        }
    }
}