package org.example.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuPanel extends JPanel
{
    private GridBagLayout gridBagLayout;
    private JButton computerDisplayButton;
    private JButton computerAddButton;

    public MenuPanel(Runnable onAdd, Runnable onSearch)
    {
        initializeButtons();
        initializeLayout();
        this.setBackground(new Color(209, 209, 209));
        computerAddButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAdd.run();
            }
        });

        computerDisplayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSearch.run();
            }
        });


    }

    private void initializeButtons()
    {

        this.computerDisplayButton = new RoundButton("WYSZUKAJ KOMPUTER");
        this.computerAddButton = new RoundButton("DODAJ KOMPUTER");
        setButtonCorners();
    }


    private void initializeLayout()
    {
        this.gridBagLayout = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15,10,15,10);
        gbc.ipady = 5;

        this.setLayout(gridBagLayout);
        gbc.gridx =0;
        gbc.gridy =0;
        this.add(computerAddButton,gbc);
        gbc.gridy =1;
        this.add(computerDisplayButton,gbc);

    }
    private void setButtonCorners()
    {   computerAddButton.setOpaque(false);

    }




 }
