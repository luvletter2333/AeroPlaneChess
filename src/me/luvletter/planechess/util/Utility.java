package me.luvletter.planechess.util;

import javax.swing.*;

public class Utility {
    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void setJPanelTitle(JPanel target, String title) {
        target.setBorder(BorderFactory.createTitledBorder(title));
    }
}
