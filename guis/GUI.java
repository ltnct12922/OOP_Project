package com.thanggun99.tank90.guis;

import java.awt.CardLayout;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class GUI extends JFrame {
    public static final int WIDTH_FRAME = 970;
    public static final int HEIGHT_FRAME = 590;

    private MyContainer myContainer;

    public GUI() throws IOException {
        inittializeGUI();
        this.addComponets();
    }

    private void inittializeGUI() {
        setTitle("GunDamVsMonster");
        setLayout(new CardLayout());
        setSize(WIDTH_FRAME, HEIGHT_FRAME);
        setResizable(false);
        //setUndecorated(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void addComponets() throws IOException {
        myContainer = new MyContainer();
        add(myContainer);
    }


}
