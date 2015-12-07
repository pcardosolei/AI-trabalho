package gui;

import Trabalho.Interface;
import jade.lang.acl.ACLMessage;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 *
 * @author a5776
 */
public class mainWindow extends javax.swing.JFrame {

    boolean warn = false;
    boolean fuelwarn = false;
    boolean tempwarn = false;
    BufferedImage rotpont = ImageIO.read(new File("images/rotpointer.png"));
    BufferedImage velpont = ImageIO.read(new File("images/velpointer.png"));
    BufferedImage dashboard = ImageIO.read(new File("images/dashboard.png"));
    BufferedImage bar = ImageIO.read(new File("images/barfull.png"));
    BufferedImage warning = ImageIO.read(new File("images/warning.png"));
    BufferedImage road = ImageIO.read(new File("images/road.png"));
    BufferedImage fuelwarning = ImageIO.read(new File("images/fuelwarn.png"));
    BufferedImage fuelnormal = ImageIO.read(new File("images/fuelnormal.png"));
    BufferedImage tempwarning = ImageIO.read(new File("images/tempwarn.png"));
    BufferedImage tempnormal = ImageIO.read(new File("images/tempnormal.png"));
    ArrayList<BufferedImage> cars = new ArrayList();
    int currentcar = 2;
    Interface agent;
    boolean ended = false;
    
    //data
    float combustivel;
    int distancia;
    float rotacoes;
    int velocidade;
    int temperatura;
    int gear;
    
    public mainWindow(Interface itf, float c, int d, float r, int v, int t, int g) throws IOException {
        combustivel = c;
        distancia = d;
        rotacoes = r;
        velocidade = v;
        temperatura = t;
        gear = g;
        this.agent = itf;
        cars.add(ImageIO.read(new File("images/veryclose.png")));
        cars.add(ImageIO.read(new File("images/close.png")));
        cars.add(ImageIO.read(new File("images/normal.png")));
        cars.add(ImageIO.read(new File("images/far.png")));
        cars.add(ImageIO.read(new File("images/veryfar.png")));
        cars.add(ImageIO.read(new File("images/ultrafar.png")));
        cars.add(ImageIO.read(new File("images/nothing.png")));

        setContentPane(new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                this.setSize(1120, 500);
                int windowx = this.getWidth();
                int windowy = this.getHeight();
                super.paintComponent(g);

                g.drawImage(road, 0, 0, windowx, windowy, this);
                g.drawImage(cars.get(currentcar), 0, 0, windowx, windowy, this);
                g.drawImage(dashboard, 0, 0, windowx, windowy, this);

                if (!fuelwarn) {
                    g.drawImage(fuelnormal, 0, 0, windowx, windowy, this);
                } else {
                    g.drawImage(fuelwarning, 0, 0, windowx, windowy, this);
                }

                if (!tempwarn) {
                    g.drawImage(tempnormal, 0, 0, windowx, windowy, this);
                } else {
                    g.drawImage(tempwarning, 0, 0, windowx, windowy, this);
                }
                
                g.drawImage(bar, (int) (0.092f * (float) windowx), 1 + (int) (0.6f * (float) windowy), 3, getFuelHeight(), this);
                g.drawImage(bar, (int) (-1 + 0.9f * (float) windowx), 2 + (int) (0.6f * (float) windowy), 3, getTempHeight(), this);

                AffineTransform txrot = AffineTransform.getRotateInstance(getAngleRot(rotacoes, 6.0f), 559, 399);
                AffineTransform txvel = AffineTransform.getRotateInstance(getAngleVel(velocidade, 240), 559, 399);
                AffineTransformOp oprot = new AffineTransformOp(txrot, AffineTransformOp.TYPE_BILINEAR);
                AffineTransformOp opvel = new AffineTransformOp(txvel, AffineTransformOp.TYPE_BILINEAR);
                
                g.drawImage(oprot.filter(rotpont, null), 0, 0, windowx, windowy, 0, 0, 1120, 500, this);
                g.drawImage(opvel.filter(velpont, null), 0, 0, windowx, windowy, 0, 0, 1120, 500, this);
                if (warn) {
                    g.drawImage(warning, 0, 0, windowx, windowy, this);
                }
                
                jLabelGear.setText(gear + "");
                jLabelDist.setText("" + distancia);
            }
        });
        WindowListener exitListener = new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                    agent.sendMessage("shutdown", ACLMessage.REQUEST);
                    agent.shutdown();
                    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
            }
        };
        addWindowListener(exitListener);
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelGear = new javax.swing.JLabel();
        jLabelDist = new javax.swing.JLabel();
        jButtonStart = new javax.swing.JButton();
        jButtonAccel = new javax.swing.JButton();
        jButtonNorm = new javax.swing.JButton();
        jButtonBreak = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMaximumSize(new java.awt.Dimension(1120, 500));
        setMinimumSize(new java.awt.Dimension(1120, 500));
        setResizable(false);
        setSize(new java.awt.Dimension(1120, 500));

        jLabelGear.setBackground(new java.awt.Color(204, 204, 204));
        jLabelGear.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabelGear.setForeground(new java.awt.Color(255, 255, 255));
        jLabelGear.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelGear.setText("0");
        jLabelGear.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        jLabelDist.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabelDist.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelDist.setText("0");
        jLabelDist.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        jButtonStart.setIcon(new javax.swing.ImageIcon("C:\\Users\\a5776\\Downloads\\Dropbox\\Aulas\\Perfil SI\\Agentes Inteligentes\\Trabalho\\SmartCar\\images\\start.png")); // NOI18N
        jButtonStart.setBorderPainted(false);
        jButtonStart.setContentAreaFilled(false);
        jButtonStart.setPressedIcon(new javax.swing.ImageIcon("C:\\Users\\a5776\\Downloads\\Dropbox\\Aulas\\Perfil SI\\Agentes Inteligentes\\Trabalho\\SmartCar\\images\\startpressed.png")); // NOI18N
        jButtonStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStartActionPerformed(evt);
            }
        });

        jButtonAccel.setBackground(new java.awt.Color(0, 204, 51));
        jButtonAccel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonAccelMouseEntered(evt);
            }
        });
        jButtonAccel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAccelActionPerformed(evt);
            }
        });

        jButtonNorm.setText(" ");
        jButtonNorm.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonNormMouseEntered(evt);
            }
        });

        jButtonBreak.setBackground(new java.awt.Color(255, 51, 51));
        jButtonBreak.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonBreakMouseEntered(evt);
            }
        });
        jButtonBreak.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBreakActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(163, 163, 163)
                .addComponent(jButtonStart)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(264, 264, 264)
                        .addComponent(jLabelGear)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 308, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButtonAccel, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelDist)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButtonNorm, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonBreak, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(164, 164, 164))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabelGear)
                .addGap(47, 47, 47))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(261, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jButtonStart, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(23, 23, 23))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabelDist)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonAccel, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonNorm, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonBreak, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(35, 35, 35))))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonAccelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAccelActionPerformed
        agent.increaseGear();
    }//GEN-LAST:event_jButtonAccelActionPerformed

    private void jButtonBreakActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBreakActionPerformed
        agent.lowerGear();
    }//GEN-LAST:event_jButtonBreakActionPerformed

    private void jButtonAccelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonAccelMouseEntered
        agent.setAccelerating();
    }//GEN-LAST:event_jButtonAccelMouseEntered

    private void jButtonNormMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonNormMouseEntered
        agent.setMaintain();
    }//GEN-LAST:event_jButtonNormMouseEntered

    private void jButtonBreakMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonBreakMouseEntered
        agent.setBreaking();
    }//GEN-LAST:event_jButtonBreakMouseEntered

    private void jButtonStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartActionPerformed
        agent.start();
        jButtonStart.setEnabled(false);
    }//GEN-LAST:event_jButtonStartActionPerformed

    int getFuelHeight() {
        if (combustivel <= 0) {
            return 121;
        }
        if (combustivel >= 1) {
            return 0;
        }
        return 121 - (int) ((float) combustivel * 121.0f);//barsize
    }

    int getTempHeight() {
        if (temperatura <= 40) {
            return 121;
        }
        if (temperatura >= 120) {
            return 0;
        }
        return 121 - (int) ((((float) temperatura - 40) / 80.0f) * 121);
    }
    
    public void updateCar(float c, int d, float r, int v, int t, int g) {
        combustivel = c;
        distancia = d;
        rotacoes = r;
        velocidade = v;
        temperatura = t;
        gear = g;
        //distancia
        if (distancia <= 10) {
            currentcar = 0;
            warn = true;
        } else if (distancia <= 20) {
            currentcar = 1;
            warn = true;
        } else if (distancia <= 50) {
            currentcar = 2;
            warn = false;
        } else if (distancia <= 70) {
            currentcar = 3;
            warn = false;
        } else if (distancia <= 90) {
            currentcar = 4;
            warn = false;
        } else if (distancia <= 150) {
            currentcar = 5;
            warn = false;
        } else {
            currentcar = 6;
            warn = false;
        }
        //temperatura
        tempwarn = temperatura >= 110;
        //fuel
        fuelwarn = temperatura <= 0.2;
        updateImage();
    }

    public void updateImage() {
        //revalidate();
        repaint();
    }

    double getAngleRot(float value, float max) {
        if (value >= max) {
            return Math.toRadians(180);
        } else if (value <= 0) {
            return Math.toRadians(0);
        }
        double degree = (value / max) * 180;
        return Math.toRadians(degree);
    }

    double getAngleVel(int value, int max) {
        if (value >= max) {
            return Math.toRadians(180);
        } else if (value <= 0) {
            return Math.toRadians(0);
        }
        double degree = ((double) value / (double) max) * 180;
        return Math.toRadians(degree);
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAccel;
    private javax.swing.JButton jButtonBreak;
    private javax.swing.JButton jButtonNorm;
    private javax.swing.JButton jButtonStart;
    private javax.swing.JLabel jLabelDist;
    private javax.swing.JLabel jLabelGear;
    // End of variables declaration//GEN-END:variables
}
