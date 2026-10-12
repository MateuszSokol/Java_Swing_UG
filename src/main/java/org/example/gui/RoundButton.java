package org.example.gui;

import javax.swing.*;
import java.awt.*;

public class RoundButton extends JButton
{
    private final int arc = 30;
    private final static Dimension BUTTON_SIZE = new Dimension(170,25);


    public RoundButton(String text)
    {
        super(text);
        setButtonSize();
        setContentAreaFilled(false);
        setOpaque(false);
        setBorderPainted(false);
        setBackground(new Color(30, 100, 210));
        setForeground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        Color color = getBackground();

        if (getModel().isPressed()) {
            color = color.darker();
        }

        g2.setColor(color);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        g2.dispose();
        super.paintComponent(g);
    }

    private void setButtonSize()
    {
        this.setPreferredSize(BUTTON_SIZE);
    }


}
