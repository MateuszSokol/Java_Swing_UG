package org.example.gui;

import javax.swing.*;
import java.awt.*;

public class AppWindow extends JFrame
{

    public static AppWindow INSTANCE;
    private AppPanel MainPanel;
    private static final int APP_HEIGHT = 350;
    private static final int APP_WIDTH = 400;

    private AppWindow()
    {

        initialize();
    }

    public static AppWindow getInstance()
    {
        if(INSTANCE == null)
        {
            INSTANCE = new AppWindow();
        }
        return INSTANCE;
    }

    private void initialize()
    {
        MainPanel = new AppPanel();
        this.setSize(APP_WIDTH,APP_HEIGHT);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setContentPane(MainPanel);
        this.setBackground(Color.CYAN);


       // this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

}
