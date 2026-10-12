package org.example.gui;

import javax.swing.*;
import java.awt.*;

public class AppPanel extends JPanel
{
    private CardLayout cardLayout;
    private ComputerAddPanel computerAddPanel;
    private ComputerDisplayPanel computerDisplayPanel;
    private MenuPanel menuPanel;


    public AppPanel()
    {
        this.cardLayout = new CardLayout();
        this.setLayout(cardLayout);
        this.initializePanels();
        this.addComputerPanelsToAppPanel();

    }

    private void initializePanels() {
        this.computerAddPanel = new ComputerAddPanel(
                new Runnable() {
                    @Override
                    public void run() {
                        cardLayout.show(AppPanel.this, "MENU");
                    }
                }
        );

        this.computerDisplayPanel = new ComputerDisplayPanel(
                new Runnable() {
                    @Override
                    public void run() {
                        cardLayout.show(AppPanel.this, "MENU");
                    }
                }
        );

        this.menuPanel = new MenuPanel(
                new Runnable() {
                    @Override
                    public void run() {
                        cardLayout.show(AppPanel.this, "ADD");
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        cardLayout.show(AppPanel.this, "DISPLAY");
                    }
                }
        );
    }

    private void addComputerPanelsToAppPanel()
    {
        this.add(menuPanel,"MENU");
        this.add(computerAddPanel,"ADD");
        this.add(computerDisplayPanel,"DISPLAY");
    }

    private void setMainPanel()
    {
        cardLayout.first(menuPanel);
    }
}
