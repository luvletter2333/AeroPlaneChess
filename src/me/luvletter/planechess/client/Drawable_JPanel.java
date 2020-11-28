package me.luvletter.planechess.client;

import me.luvletter.planechess.util.ImageDrawer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;

public class Drawable_JPanel extends JPanel implements Drawable {

    private BufferedImage image;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(image != null) {
            // g.drawImage(image, 0, 0, this);
            ImageDrawer.drawScaledImage(image, g, getWidth(), getHeight(), this);
        }

    }

    public void Draw(BufferedImage i) {
        this.image = i;
        repaint();
    }

    public Drawable_JPanel(){
        super();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
            //    System.out.printf("resizing, width: %d, height: %d\n", getWidth(), getHeight());
                repaint();
            }
        });
    }



}