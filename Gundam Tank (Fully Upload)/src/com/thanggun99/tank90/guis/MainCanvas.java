package com.thanggun99.tank90.guis;
import com.thanggun99.tank90.managers.AudioPlayer;
import com.thanggun99.tank90.models.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainCanvas extends JPanel implements Runnable, KeyListener {


    private static final long serialVersionUID = 1270488992849705712L;
    private static boolean gameOver;

    public static int currentLevel = 1;
    public static int animationClock = 0;
    public static Tools t;
    public static TileManager tm;
    public static ArrayList<Explosion> explosionArray;
    public static ArrayList<Explosion> explosionArray2;
    public static ArrayList<Tank> tankArray;
    public static ArrayList<Tank> tankArray2;
    public static ArrayList<Item> itemArray;
    public static ArrayList<Item> itemArray2;
    public static AudioPlayer audioPlayer;
    public static AudioPlayer audioPlayer2;

    private long sleepTime = 20;
    private PlayerTank playerTank;
    private PlayerTank2 playerTank2;
    public int totalAITank = 8;
    public final int MAX_AITANK_ONSCREEN = 3;
    public int currentTotalAITank = 0;
    public int tankKill = 0;
    private boolean isRunning;
    private boolean changeLevel;
    private Timer aitankTimer;
    Random rnd;
    Thread thread;
    private boolean endGame;
    public IBackToMenu onBackToMenuListener;

    public MainCanvas() {
        this.setEndGame(false);
        rnd = new Random(serialVersionUID);
        this.setSize(GUI.WIDTH_FRAME, GUI.HEIGHT_FRAME);
        t = new Tools();
        try {
            tm = new TileManager();
            // init tank, item and explosion array
            audioPlayer = new AudioPlayer();
            audioPlayer2 = new AudioPlayer();
            explosionArray = new ArrayList<Explosion>(20);
            explosionArray.clear();
            tankArray = new ArrayList<Tank>(20);
            tankArray.clear();
            itemArray = new ArrayList<Item>(10);
            itemArray.clear();
            explosionArray2 = new ArrayList<Explosion>(20);
            explosionArray2.clear();
            tankArray2 = new ArrayList<Tank>(20);
            tankArray2.clear();
            itemArray2 = new ArrayList<Item>(10);
            itemArray2.clear();
            // create tanks and add to array
            playerTank = new PlayerTank(t.getPlayerTankImage(), 32, 32);
            playerTank.spawnd();
            // player tank is alway at first place of tank array
            playerTank.setCurrentHealth(playerTank.totalHealth);
            tankArray.add(0, playerTank);
            playerTank2 = new PlayerTank2(t.getPlayerTank2Image(), 32, 32);
            playerTank2.spawnd();
            // player tank is alway at first place of tank array
            playerTank2.setCurrentHealth(playerTank2.totalHealth);
            tankArray2.add(0, playerTank2);

            initMap(rnd.nextInt(16) + 1);
            MainCanvas.setGameOver(false);

            this.setRunning(false);
            this.setFocusable(true);
            this.addKeyListener(this);
            thread = new Thread(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        thread.start();
        audioPlayer.playSound(AudioPlayer.GAME_START);
        //spawnItem(250, 300); //item appear
    }


    @Override
    public void run() {
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
        }
        while (!isEndGame()) {
            animationClock++;
            if (animationClock % 1000 == 0)
                spawnItem(new Random().nextInt(GUI.WIDTH_FRAME - 200), new Random().nextInt(GUI.HEIGHT_FRAME - 100));
            if (animationClock == 2147483647)
                animationClock = 0;

            if (!isGameOver()) {
                if (isAllAITankDestroyed()
                        && this.currentTotalAITank == this.totalAITank) {
                    currentLevel++;
                    audioPlayer.playSound(AudioPlayer.LEVEL_COMPLETED);
                    setChangeLevel(true);
                    changeLevel();
                }

                // Update tank state
                for (int i = 0; i < tankArray.size(); i++) {
                    Tank tmp = tankArray.get(i);
                    if (tmp instanceof AITank) {
                        ((AITank) tmp).think();
                    }

                        Tank tmp2 = tankArray2.get(i);
                        if (tmp2 instanceof AITank) {
                            ((AITank) tmp2).think();
                        }

                    tmp2.update();
                    tmp.update();
                    if (tmp instanceof AITank && tmp.isDestroyed) {
                        if (((AITank) tmp).getType() == AITank.RED_TANK) {
                            this.spawnItem(tmp.getX(), tmp.getY());
                        }
                        tankArray.remove(tmp);
                        tankArray2.remove(tmp);
                        tankKill++;
                        tmp = null;
                    }
                }

                // update item
                for (int i = 0; i < itemArray.size(); i++) {
                    if (itemArray.get(i).isDestroyed()) {
                        itemArray.remove(i);
                    }
                }

                // Update explosion array
                if (!explosionArray.isEmpty()) {
                    for (int i = 0; i < explosionArray.size(); i++) {
                        explosionArray.get(i).update();
                        if (explosionArray.get(i).isDestroyed())
                            explosionArray.remove(i);
                    }
                }
                // Update
                tm.update();
            } else {
                cleanUpEveryThing();
            }
            // Repaint
            repaint();
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void initMap(int mapNumber) {
        tm.loadMap(mapNumber);
        this.spawnAITanks();
    }

    public void cleanUpMap() {
        for (int i = tankArray.size() - 1; i > 0; i--) {
            tankArray.get(i).getBulletArray().clear();
            tankArray.remove(i);
        }
        explosionArray.clear();
        tm.cleanAllBricks();
        itemArray.clear();
        currentTotalAITank = 0;
        if (this.aitankTimer != null) {
            this.aitankTimer.cancel();
            this.aitankTimer.purge();
            this.aitankTimer = null;
        }
    }
    public void cleanUpMap2() {
        for (int i = tankArray2.size() - 1; i > 0; i--) {
            tankArray2.get(i).getBulletArray().clear();
            tankArray2.remove(i);
        }
        explosionArray2.clear();
        tm.cleanAllBricks();
        itemArray2.clear();
        currentTotalAITank = 0;
        if (this.aitankTimer != null) {
            this.aitankTimer.cancel();
            this.aitankTimer.purge();
            this.aitankTimer = null;
        }
    }

    public void changeLevel() {
        repaint();
        cleanUpMap();
        tankKill = 0;
        initMap(rnd.nextInt(16) + 1);
        tankArray.get(0).setPositionAndBound(10 * 32, 16 * 32);
        playerTank.setCurrentDirection(Sprite.UP);
        playerTank.delayTank();
        tankArray2.get(0).setPositionAndBound(15 * 32, 16 * 32);
        playerTank2.setCurrentDirection(Sprite.UP);
        playerTank2.delayTank();
        try {
            Thread.sleep(1500);
        } catch (Exception e) {
        }
        setChangeLevel(false);
    }

    public void spawnItem(int x, int y) {
        Item tmp;
        try {
            tmp = new Item(MainCanvas.t.getItemImage(), 32, 32);
            tmp.setType(new Random().nextInt(8));
            tmp.setPositionAndBound(x, y);
            itemArray.add(tmp);
            tmp = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void spawnAITanks() {
        aitankTimer = new Timer();
        aitankTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!isGameOver()) {
                    if (currentTotalAITank != totalAITank) {
                        if (MainCanvas.tankArray.size() - 1 < MAX_AITANK_ONSCREEN && MainCanvas.tankArray2.size() - 1 < MAX_AITANK_ONSCREEN) {
                            int pos = -1;

                            do {
                                pos = rnd.nextInt(3) * 12;
                                if (isPlaceSpawnable(pos)) {
                                } else pos = -1;
                            } while (pos == -1);

                            currentTotalAITank++;
                            try {
                                AITank tmp;
                                if (currentTotalAITank % 4 == 0) {
                                    tmp = new AITank(t.getRedTankImage(), 32,
                                            32, AITank.RED_TANK);
                                } else {
                                    tmp = new AITank(t.getAITankImage(), 32,
                                            32, AITank.BLUE_TANK);
                                }
                                tmp.setPositionAndBound(pos * 32, 0);
                                tankArray.add(tmp);
                                tankArray2.add(tmp);
                                tmp = null;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else
                        this.cancel();
                } else {
                    this.cancel();
                }
            }
        }, 0, 5000);
    }

    private boolean isPlaceSpawnable(int pos) {
        for (int i = 1; i < MainCanvas.tankArray.size(); i++) {
            if (MainCanvas.t.isInBound(tankArray.get(i), pos * 32, 0, 32, 32)) {
                return false;
            }
        }
        for (int i = 1; i < MainCanvas.tankArray2.size(); i++) {
            if (MainCanvas.t.isInBound(tankArray2.get(i), pos * 32, 0, 32, 32)) {
                return false;
            }
        }
        return true;
    }

    public void paintComponent(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, GUI.WIDTH_FRAME, GUI.HEIGHT_FRAME);

        if (!isGameOver()) {
            if (isChangeLevel()) {
                g.setColor(Color.WHITE);
                Font f = new Font("Courier New", Font.BOLD, 48);
                g.setFont(f);
                g.drawString("LEVEL COMPLED", 300, GUI.HEIGHT_FRAME / 2);
                return;
            }
            tm.render(g);
            // render items
            for (int i = 0; i < itemArray.size(); i++) {
                itemArray.get(i).render(g);
            }
            for (int i = 0; i < itemArray2.size(); i++) {
                itemArray2.get(i).render(g);
            }

            // render tanks
            for (int i = 0; i < tankArray.size(); i++) {
                tankArray.get(i).render(g);
            }
            for (int i = 0; i < tankArray2.size(); i++) {
                tankArray2.get(i).render(g);
            }

            // render explosion effects
            if (!explosionArray.isEmpty()) {
                for (int i = 0; i < explosionArray.size(); i++) {
                    explosionArray.get(i).render(g);
                }
            }
            if (!explosionArray2.isEmpty()) {
                for (int i = 0; i < explosionArray2.size(); i++) {
                    explosionArray2.get(i).render(g);
                }
            }


            // render right menu
            drawRightMenu(g);
        } else {
            g.setColor(Color.WHITE);
            Font f = new Font("Courier New", Font.BOLD, 48);
            g.setFont(f);
            g.drawString("GAME OVER", 350, GUI.HEIGHT_FRAME / 2);

            g.setColor(Color.green);
            Font f2 = new Font("Courier New", Font.ITALIC, 12);
            g.setFont(f2);
            g.drawString("press ENTER to back menu", 790, GUI.HEIGHT_FRAME - 40);
        }
    }

    private void drawRightMenu(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(800, 0, 200, 544);
        g.translate(800, 0);
        g.setColor(Color.WHITE);
        g.drawString("PLAYER1: "+PanelMenu.name,10,10+10);
        g.drawString("PLAYER2: "+PanelMenu.name1,10,10+10+20);
        g.drawString("LEVEL:      " + String.valueOf(currentLevel), 10, 20+20+30);
        g.drawString("SCORE:", 10, 40+20+30);
        g.drawString(
                String.valueOf(((PlayerTank) tankArray.get(0)).getScore()),
                100, 40+20+30);
        g.drawString("HEALTH:", 10, 80+30);
        g.drawString(String
                        .valueOf(((PlayerTank) tankArray.get(0)).currentHealth)
                        + "/"
                        + String.valueOf(((PlayerTank) tankArray.get(0)).totalHealth),
                100, 80+30);
        g.drawString("HEALTH2:", 10, 80+20+30);
        g.drawString(String
                        .valueOf(((PlayerTank2) tankArray2.get(0)).currentHealth)
                        + "/"
                        + String.valueOf(((PlayerTank2) tankArray2.get(0)).totalHealth),
                100, 80+20+30);


        g.drawString("LIVES:", 10, 120+30+30);
        try {
            BufferedImage lives = MainCanvas.t.getItemImage().getSubimage(64,
                    0, 32, 32);
            for (int i = 0; i < ((PlayerTank) tankArray.get(0)).getLives(); i++) {
                g.drawImage(lives, 20 * i, 120+30+30, 32, 32, null);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        g.drawString("LIVES2:", 10, 120+30+50+30);
        try {
            BufferedImage lives = MainCanvas.t.getItemImage().getSubimage(64,
                    0, 32, 32);
            for (int i = 0; i < ((PlayerTank2) tankArray2.get(0)).getLives(); i++) {
                g.drawImage(lives, 20 * i, 120+30+50+30, 32, 32, null);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        g.drawString("MONSTER: "
                + String.valueOf(totalAITank - tankKill), 10, 180+50+30+30);
        g.translate(0, 0);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (isGameOver() && e.getKeyCode() == KeyEvent.VK_ENTER) {
            this.onBackToMenuListener.backToMenu();
        }
        if (!isGameOver()) {
            playerTank.keyPressedReact(e);
            playerTank2.keyPressedReact2(e);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (!isGameOver()) {
            playerTank.keyReleasedReact(e);
            playerTank2.keyReleasedReact2(e);
        }

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    public static void addExplosion(int x, int y) {
        try {
            Explosion tmp = new Explosion(MainCanvas.t.getExplosionImage(), 32,
                    32);
            tmp.setPositionAndBound(x, y);
            explosionArray.add(tmp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void setRunning(boolean running) {
        this.isRunning = running;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isAllAITankDestroyed() {
        for (int i = 1; i < tankArray.size(); i++) {
            if (!tankArray.get(i).isDestroyed())
                return false;
        }
        for (int i = 1; i < tankArray2.size(); i++) {
            if (!tankArray2.get(i).isDestroyed())
                return false;
        }
        return true;
    }

    public void cleanUpEveryThing() {
        this.cleanUpMap();
        MainCanvas.tankArray.clear();
        playerTank = null;
        currentLevel = 1;
        animationClock = 0;
        setEndGame(true);
    }

    public boolean isEndGame() {
        return endGame;
    }

    public void setEndGame(boolean endGame) {
        this.endGame = endGame;
    }

    public static boolean isGameOver() {
        return gameOver;
    }

    public static void setGameOver(boolean gameOver) {
        MainCanvas.gameOver = gameOver;
    }

    public boolean isChangeLevel() {
        return changeLevel;
    }

    public void setChangeLevel(boolean changeLevel) {
        this.changeLevel = changeLevel;
    }

    public void setOnBackToMenuListener(IBackToMenu onBackToMenuListener) {
        this.onBackToMenuListener = onBackToMenuListener;
    }
}
