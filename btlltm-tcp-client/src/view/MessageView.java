//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

import static run.ClientRun.socketHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;

public class MessageView extends JFrame {
    String userChat = "";
    private JButton btnLeaveChat;
    private JButton btnSend;
    private JTextArea contentChat;
    private JLabel infoUserChat;
    private JScrollPane jScrollPane1;
    private JTextField tfMessage;
    private JPanel videoPanel;
    private JLabel remoteVideoLabel;
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JPanel videoControlPanel;
    private JPanel localVideoPanel;
    private JLabel localVideoLabel;
    private boolean isVideoActive = false;


//    public void showVideoPanel() {
//        if (videoPanel == null) {
//            videoPanel = new JPanel();
//            remoteVideoLabel = new JLabel();
//            videoPanel.setBackground(Color.BLACK);
//            videoPanel.setPreferredSize(new Dimension(320, 240));
//            remoteVideoLabel = new JLabel();
//            remoteVideoLabel.setPreferredSize(new Dimension(320, 240));
//            videoPanel.add(remoteVideoLabel);
//            // Add to your message view layout
//        }
//        videoPanel.setVisible(true);
//    }
//public void showVideoPanel() {
//    if (videoPanel == null) {
//        videoPanel = new JPanel();
//        remoteVideoLabel = new JLabel();
//        videoPanel.setBackground(Color.BLACK);
//        videoPanel.setPreferredSize(new Dimension(320, 240));
//
//        // Create a new label for the local video feed
//        JLabel ownVideoLabel = new JLabel();
//        ownVideoLabel.setPreferredSize(new Dimension(320, 240));
//
//        // Add the local video label first
//        videoPanel.add(ownVideoLabel, BorderLayout.WEST);
//
//        // Then add the remote video label
//        videoPanel.add(remoteVideoLabel, BorderLayout.EAST);
//    }
//
//    videoPanel.setVisible(true);
//}

//    public void hideVideoPanel() {
//        if (videoPanel != null) {
//            videoPanel.setVisible(false);
//            pack(); // Resize the frame after hiding the video panel
//        }
//    }
//
//    public void updateVideoPanel(ImageIcon frame) {
//        if (remoteVideoLabel != null) {
//            // Scale the image to fit the label while maintaining aspect ratio
//            Image img = frame.getImage();
//            Image scaledImg = img.getScaledInstance(320, 240, Image.SCALE_SMOOTH);
//            remoteVideoLabel.setIcon(new ImageIcon(scaledImg));
//            remoteVideoLabel.repaint();
//        }
//    }
    public MessageView() {
        this.initComponents();
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(MessageView.this, "Do you want to leave chat?", "Leave chat?", 0, 3) == 0) {
                    socketHandler.leaveChat(MessageView.this.userChat);
                    MessageView.this.dispose();
                }

            }
        });

    }

    public void setInfoUserChat(String username) {
        this.userChat = username;
        this.infoUserChat.setText("Chat with: " + username);
    }

    public void setContentChat(String chat) {
        this.contentChat.append(chat);
    }

    public void eventSendMessage() {
        String message = this.tfMessage.getText().trim();
        if (message.equals("")) {
            this.tfMessage.grabFocus();
        } else {
            socketHandler.sendMessage(this.userChat, message);
            this.tfMessage.setText("");
            this.tfMessage.grabFocus();
        }

    }


    private void initComponents() {
        this.tfMessage = new JTextField();
        this.btnSend = new JButton();
        this.jScrollPane1 = new JScrollPane();
        this.contentChat = new JTextArea();
        this.infoUserChat = new JLabel();
        this.btnLeaveChat = new JButton();
        this.setDefaultCloseOperation(0);
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        headerPanel = new JPanel(new BorderLayout());
        videoControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton videoCallButton = new JButton("Start Video Call");
        JButton endCallButton = new JButton("End Call");

        videoCallButton.addActionListener(e -> {
            socketHandler.initiateVideoCall(userChat);
            showVideoPanel();
        });

        endCallButton.addActionListener(e -> {
            socketHandler.endVideoCall(userChat);
            hideVideoPanel();
        });
        this.tfMessage.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                MessageView.this.tfMessageKeyPressed(evt);
            }
        });
        this.btnSend.setText("Send");
        this.btnSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                MessageView.this.btnSendActionPerformed(evt);
            }
        });
        this.contentChat.setEditable(false);
        this.contentChat.setColumns(20);
        this.contentChat.setRows(5);
        this.jScrollPane1.setViewportView(this.contentChat);
        this.infoUserChat.setFont(new Font("Tahoma", 0, 18));
        this.infoUserChat.setText("Chat with:");
        this.btnLeaveChat.setBackground(new Color(255, 102, 0));
        this.btnLeaveChat.setForeground(new Color(204, 255, 255));
        this.btnLeaveChat.setText("Leave chat");
        this.btnLeaveChat.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                MessageView.this.btnLeaveChatActionPerformed(evt);
            }
        });


//        Container contentPane = getContentPane();
//        GroupLayout layout = (GroupLayout) contentPane.getLayout();
//        GroupLayout layout = new GroupLayout(this.getContentPane());
//        this.getContentPane().setLayout(layout);




        videoPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        videoPanel.setBackground(Color.BLACK);
        videoPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

// Remote video
        JPanel remoteVideoContainer = new JPanel(new BorderLayout());
        remoteVideoContainer.setBackground(Color.BLACK);
        remoteVideoLabel = new JLabel("Remote Video");
        remoteVideoLabel.setForeground(Color.WHITE);
        remoteVideoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        remoteVideoLabel.setPreferredSize(new Dimension(320, 240));
        remoteVideoContainer.add(remoteVideoLabel, BorderLayout.CENTER);
        // Local video
        JPanel localVideoContainer = new JPanel(new BorderLayout());
        localVideoContainer.setBackground(Color.BLACK);
        localVideoLabel = new JLabel("Local Video");
        localVideoLabel.setForeground(Color.WHITE);
        localVideoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        localVideoLabel.setPreferredSize(new Dimension(320, 240));
        localVideoContainer.add(localVideoLabel, BorderLayout.CENTER);

        // Add video panels
        videoPanel.add(localVideoContainer);
        videoPanel.add(remoteVideoContainer);
        videoPanel.setVisible(false);

        // Add controls to header
        videoControlPanel.add(videoCallButton);
        videoControlPanel.add(endCallButton);
        videoControlPanel.add(btnLeaveChat);

        // Organize header panel
        headerPanel.add(infoUserChat, BorderLayout.WEST);
        headerPanel.add(videoControlPanel, BorderLayout.EAST);

        // Create chat panel
        JPanel chatPanel = new JPanel(new BorderLayout(0, 10));
        chatPanel.add(jScrollPane1, BorderLayout.CENTER);

        // Message input panel
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.add(tfMessage, BorderLayout.CENTER);
        inputPanel.add(btnSend, BorderLayout.EAST);

        // Add everything to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(videoPanel, BorderLayout.CENTER);
        mainPanel.add(chatPanel, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        // Set content pane
        setContentPane(mainPanel);

        // Update existing layout code
        GroupLayout layout = new GroupLayout(mainPanel);
        mainPanel.setLayout(layout);



        layout.setHorizontalGroup(
                layout.createParallelGroup(Alignment.LEADING)
                        .addComponent(headerPanel)
                        .addComponent(videoPanel)
                        .addComponent(jScrollPane1)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(tfMessage)
                                .addGap(10)
                                .addComponent(btnSend, 100, 100, 100))
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(headerPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(10)
                        .addComponent(videoPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(10)
                        .addComponent(jScrollPane1)
                        .addGap(10)
                        .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                                .addComponent(tfMessage, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnSend, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE))
        );


//        layout.setHorizontalGroup(
//                layout.createParallelGroup(Alignment.LEADING)
//                        .addGroup(layout.createSequentialGroup()
//                                .addContainerGap()
//                                .addGroup(layout.createParallelGroup(Alignment.LEADING, false)
//                                        .addGroup(layout.createSequentialGroup()
//                                                .addComponent(this.infoUserChat, -2, 252, -2)
//                                                .addPreferredGap(ComponentPlacement.RELATED, -1, Short.MAX_VALUE)
//                                                .addComponent(videoCallButton)
//                                                .addGap(10, 10, 10)
//                                                .addComponent(endCallButton)
//                                                .addGap(10, 10, 10)
//                                                .addComponent(this.btnLeaveChat, -2, 100, -2))
//                                        .addGroup(layout.createParallelGroup(Alignment.TRAILING, false)
//                                                .addComponent(this.jScrollPane1, -2, 441, -2)
//                                                .addGroup(layout.createSequentialGroup()
//                                                        .addComponent(this.tfMessage)
//                                                        .addGap(18, 18, 18)
//                                                        .addComponent(this.btnSend, -2, 112, -2))))
//                                .addContainerGap(21, Short.MAX_VALUE))
//        );
//        layout.setHorizontalGroup(
//                layout.createParallelGroup(Alignment.LEADING)
//                        .addGroup(layout.createSequentialGroup()
//                                .addContainerGap()
//                                .addGroup(layout.createParallelGroup(Alignment.LEADING)
//                                        .addComponent(videoPanel, Alignment.CENTER) // Add video panel
//                                        .addGroup(layout.createSequentialGroup()
//                                                .addComponent(this.infoUserChat, -2, 252, -2)
//                                                .addPreferredGap(ComponentPlacement.RELATED, -1, Short.MAX_VALUE)
//                                                .addComponent(videoCallButton)
//                                                .addGap(10, 10, 10)
//                                                .addComponent(endCallButton)
//                                                .addGap(10, 10, 10)
//                                                .addComponent(this.btnLeaveChat, -2, 100, -2))
//                                        .addGroup(layout.createParallelGroup(Alignment.TRAILING, false)
//                                                .addComponent(this.jScrollPane1, -2, 441, -2)
//                                                .addGroup(layout.createSequentialGroup()
//                                                        .addComponent(this.tfMessage)
//                                                        .addGap(18, 18, 18)
//                                                        .addComponent(this.btnSend, -2, 112, -2))))
//                                .addContainerGap(21, Short.MAX_VALUE))
//        );
////        layout.setVerticalGroup(
////                layout.createParallelGroup(Alignment.LEADING)
////                        .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
////                                .addContainerGap(26, Short.MAX_VALUE)
////                                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
////                                        .addComponent(this.infoUserChat, -1, 36, 32767)
////                                        .addComponent(videoCallButton, -1, -1, 32767)
////                                        .addComponent(endCallButton, -1, -1, 32767)
////                                        .addComponent(this.btnLeaveChat, -1, -1, 32767))
////                                .addGap(18, 18, 18)
////                                .addComponent(this.jScrollPane1, -2, 213, -2)
////                                .addGap(18, 18, 18)
////                                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
////                                        .addComponent(this.btnSend, -2, 38, -2)
////                                        .addComponent(this.tfMessage, -2, 38, -2))
////                                .addGap(28, 28, 28))
////        );
//        layout.setVerticalGroup(
//                layout.createParallelGroup(Alignment.LEADING)
//                        .addGroup(layout.createSequentialGroup()
//                                .addContainerGap()
//                                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
//                                        .addComponent(this.infoUserChat, -1, 36, 32767)
//                                        .addComponent(videoCallButton)
//                                        .addComponent(endCallButton)
//                                        .addComponent(this.btnLeaveChat))
//                                .addGap(10, 10, 10)
//                                .addComponent(videoPanel, -2, -2, -2) // Add video panel
//                                .addGap(10, 10, 10)
//                                .addComponent(this.jScrollPane1, -2, 213, -2)
//                                .addGap(18, 18, 18)
//                                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
//                                        .addComponent(this.btnSend, -2, 38, -2)
//                                        .addComponent(this.tfMessage, -2, 38, -2))
//                                .addContainerGap())
//        );



        pack();
        setLocationRelativeTo(null);



    }
    public void showVideoPanel() {
        if (videoPanel != null) {
            videoPanel.setVisible(true);
            isVideoActive = true;
            pack();
            revalidate();
            repaint();
        }
    }

    public void hideVideoPanel() {
        if (videoPanel != null) {
            videoPanel.setVisible(false);
            isVideoActive = false;
            pack();
            revalidate();
            repaint();
        }
    }

//    public void updateRemoteVideoPanel(ImageIcon frame) {
//        if (remoteVideoLabel != null && frame != null) {
//            Image img = frame.getImage();
//            Image scaledImg = img.getScaledInstance(320, 240, Image.SCALE_SMOOTH);
//            remoteVideoLabel.setIcon(new ImageIcon(scaledImg));
//            remoteVideoLabel.repaint();
//        }
//    }
//
//
//    public void updateOwnVideoPanel(ImageIcon frame) {
//        if (localVideoLabel != null && frame != null) {
//            Image img = frame.getImage();
//            Image scaledImg = img.getScaledInstance(320, 240, Image.SCALE_SMOOTH);
//            localVideoLabel.setIcon(new ImageIcon(scaledImg));
//            localVideoLabel.repaint();
//        }
//    }
public void updateOwnVideoPanel(ImageIcon frame) {
    if (localVideoLabel != null && frame != null) {
        Image img = frame.getImage();
        Image scaledImg = img.getScaledInstance(320, 240, Image.SCALE_SMOOTH);
        localVideoLabel.setIcon(new ImageIcon(scaledImg));
        localVideoLabel.repaint();
    }
}

    public void updateRemoteVideoPanel(ImageIcon frame) {
        if (remoteVideoLabel != null && frame != null) {
            Image img = frame.getImage();
            Image scaledImg = img.getScaledInstance(320, 240, Image.SCALE_SMOOTH);
            remoteVideoLabel.setIcon(new ImageIcon(scaledImg));
            remoteVideoLabel.repaint();
        }
    }



    private void btnLeaveChatActionPerformed(ActionEvent evt) {
        socketHandler.leaveChat(this.userChat);
        this.dispose();
    }

    private void btnSendActionPerformed(ActionEvent evt) {
        this.eventSendMessage();
    }

    private void tfMessageKeyPressed(KeyEvent evt) {
        if (evt.getKeyCode() == 10) {
            this.eventSendMessage();
        }

    }


    public static void main(String[] args) {
        try {
            UIManager.LookAndFeelInfo[] var12 = UIManager.getInstalledLookAndFeels();
            int var2 = var12.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                UIManager.LookAndFeelInfo info = var12[var3];
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException var5) {
            Logger.getLogger(MessageView.class.getName()).log(Level.SEVERE, (String)null, var5);
        } catch (InstantiationException var6) {
            Logger.getLogger(MessageView.class.getName()).log(Level.SEVERE, (String)null, var6);
        } catch (IllegalAccessException var7) {
            Logger.getLogger(MessageView.class.getName()).log(Level.SEVERE, (String)null, var7);
        } catch (UnsupportedLookAndFeelException var8) {
            UnsupportedLookAndFeelException ex = var8;
            Logger.getLogger(MessageView.class.getName()).log(Level.SEVERE, (String)null, ex);
        }

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                (new MessageView()).setVisible(true);
            }
        });
    }
}
