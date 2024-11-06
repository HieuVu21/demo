//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package controller;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Base64;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.IIOImage;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;


import run.Client1Run;
import run.ClientRun;
import run.ClientRun.SceneName;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import com.github.sarxos.webcam.Webcam;

public class SocketHandler {
    private Webcam webcam;
    private AtomicBoolean isVideoCallActive = new AtomicBoolean(false);
    private Thread videoThread;
    Socket s;
    DataInputStream dis;
    DataOutputStream dos;
    String loginUser ;
    private String host;
    String roomIdPresent = null;
    Thread listener = null;
    private static final int VIDEO_FRAME_INTERVAL = 50; // 20fps - more reasonable
    private static final int MAX_FRAME_SIZE = 1024 * 1024; // 1MB max frame size
    private static final int VIDEO_WIDTH = 640;
    private static final int VIDEO_HEIGHT = 480;
    private volatile boolean isWebcamInitialized = false;

    public SocketHandler() {
    }

    public String connect(String addr, int port) {
        try {
            InetAddress ip = InetAddress.getByName(addr);
            this.s = new Socket();
            this.s.connect(new InetSocketAddress(ip, port), 4000);
            System.out.println("Connected to " + String.valueOf(ip) + ":" + port + ", localport:" + this.s.getLocalPort());
            this.dis = new DataInputStream(this.s.getInputStream());
            this.dos = new DataOutputStream(this.s.getOutputStream());
            if (this.listener != null && this.listener.isAlive()) {
                this.listener.interrupt();
            }

            this.listener = new Thread(this::listen);
            this.listener.start();
            return "success";
        } catch (IOException var4) {
            IOException e = var4;
            return "failed;" + e.getMessage();
        }
    }
    private void initializeWebcam() {
        try {
            if (!isWebcamInitialized) {
                webcam = Webcam.getDefault();
                if (webcam != null) {
                    Dimension[] dimensions = webcam.getViewSizes();
                    Dimension dimension = new Dimension(VIDEO_WIDTH, VIDEO_HEIGHT);
                    // Find the closest supported resolution
                    for (Dimension d : dimensions) {
                        if (d.getWidth() <= VIDEO_WIDTH && d.getHeight() <= VIDEO_HEIGHT) {
                            dimension = d;
                        }
                    }
                    webcam.setViewSize(dimension);
                    webcam.open();
                    isWebcamInitialized = true;
                } else {
                    throw new RuntimeException("No webcam detected");
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize webcam: " + e.getMessage());
            throw new RuntimeException("Failed to initialize webcam", e);
        }
    }

    public void initiateVideoCall(String userInvited) {
        sendData("INVITE_TO_VIDEO;" + loginUser + ";" + userInvited);
    }

    public void acceptVideoCall(String userHost, String userInvited) {
        sendData("ACCEPT_VIDEO;" + userHost + ";" + userInvited);
//        startVideoStream(userInvited);
//        startVideoStream2(userHost);
    }

    public void declineVideoCall(String userHost, String userInvited) {
        sendData("DECLINE_VIDEO;" + userHost + ";" + userInvited);
    }

    public void endVideoCall(String userInvited) {
        sendData("END_VIDEO;" + loginUser + ";" + userInvited);
        stopVideoStream();
    }
    private boolean hasFrameChanged(BufferedImage prev, BufferedImage curr) {
        if (prev.getWidth() != curr.getWidth() || prev.getHeight() != curr.getHeight()) {
            return true;
        }

        for (int y = 0; y < curr.getHeight(); y += 10) {
            for (int x = 0; x < curr.getWidth(); x += 10) {
                if (prev.getRGB(x, y) != curr.getRGB(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }
    private byte[] compressImage(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            ImageWriter writer = writers.next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.5f);

            ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
            writer.dispose();
            ios.close();

            return baos.toByteArray();
        } catch (IOException e) {
            System.err.println("Error compressing image: " + e.getMessage());
            return null;
        }
    }

    private void

    startVideoStream(String recipient) {
        try {
            initializeWebcam();
            isVideoCallActive.set(true);

            videoThread = new Thread(() -> {
                BufferedImage prevFrame = null;
                long lastFrameTime = 0;

                while (isVideoCallActive.get()) {
                    try {
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastFrameTime < VIDEO_FRAME_INTERVAL) {
                            Thread.sleep(5); // Small sleep to prevent CPU hogging
                            continue;
                        }

                        if (!webcam.isOpen()) {
                            System.err.println("Webcam is not open");
                            break;
                        }

                        BufferedImage currentFrame = webcam.getImage();
                        if (currentFrame == null) {
                            continue;
                        }

                        // Only send frame if it's different from previous frame
                        if (prevFrame == null || hasFrameChanged(prevFrame, currentFrame)) {
                            // Compress and send frame
                            byte[] imageBytes = compressImage(currentFrame);
                            if (imageBytes != null && imageBytes.length > 0 && imageBytes.length < MAX_FRAME_SIZE) {
                                sendData("VIDEO_FRAME;" + loginUser + ";" + recipient + ";"
                                        + Base64.getEncoder().encodeToString(imageBytes));
                                prevFrame = currentFrame;
                            }
                        }

                        lastFrameTime = currentTime;

                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        System.err.println("Error in video stream: " + e.getMessage());
                        if (!isVideoCallActive.get()) break;
                    }
                }
            });
            videoThread.setDaemon(true);
            videoThread.start();

        } catch (Exception e) {
            System.err.println("Failed to start video stream: " + e.getMessage());
            stopVideoStream();
            throw new RuntimeException("Failed to start video stream", e);
        }
    }

    private void startVideoStream2(String recipient) {
        try {
            initializeWebcam();
            isVideoCallActive.set(true);

            videoThread = new Thread(() -> {
                BufferedImage prevFrame = null;
                long lastFrameTime = 0;

                while (isVideoCallActive.get()) {
                    try {
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastFrameTime < VIDEO_FRAME_INTERVAL) {
                            Thread.sleep(5); // Small sleep to prevent CPU hogging
                            continue;
                        }

                        if (!webcam.isOpen()) {
                            System.err.println("Webcam is not open");
                            break;
                        }

                        BufferedImage currentFrame = webcam.getImage();
                        if (currentFrame == null) {
                            continue;
                        }

                        // Only send frame if it's different from previous frame
                        if (prevFrame == null || hasFrameChanged(prevFrame, currentFrame)) {
                            // Compress and send frame
                            byte[] imageBytes = compressImage(currentFrame);

                            if (imageBytes != null && imageBytes.length > 0 && imageBytes.length < MAX_FRAME_SIZE) {
                                sendData("VIDEO_FRAME;" + loginUser + ";" + recipient + ";"
                                        + Base64.getEncoder().encodeToString(imageBytes));
                                prevFrame = currentFrame;
                            }
                        }

                        lastFrameTime = currentTime;

                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        System.err.println("Error in video stream: " + e.getMessage());
                        if (!isVideoCallActive.get()) break;
                    }
                }
            });
            videoThread.setDaemon(true);
            videoThread.start();

        } catch (Exception e) {
            System.err.println("Failed to start video stream: " + e.getMessage());
            stopVideoStream();
            throw new RuntimeException("Failed to start video stream", e);
        }
    }














    private void stopVideoStream() {
        isVideoCallActive.set(false);
        if (videoThread != null) {
            videoThread.interrupt();
            videoThread = null;
        }
        if (webcam != null && webcam.isOpen()) {
            try {
                webcam.close();
                isWebcamInitialized = false;
            } catch (Exception e) {
                System.err.println("Error closing webcam: " + e.getMessage());
            }
        }
    }
    private void listen() {
        boolean running = true;

        IOException ex;
        while(running) {
            try {
                String received = this.dis.readUTF();
                System.out.println("RECEIVED: " + received);
                switch (received.split(";")[0]) {
                    case "LOGIN":
                        this.onReceiveLogin(received);
                        break;
                    case "REGISTER":
                        this.onReceiveRegister(received);
                        break;
                    case "GET_LIST_ONLINE":
                        this.onReceiveGetListOnline(received);
                        break;
                    case "LOGOUT":
                        this.onReceiveLogout(received);
                        break;
                    case "INVITE_TO_CHAT":
                        this.onReceiveInviteToChat(received);
                        break;
                    case "ACCEPT_MESSAGE":
                        this.onReceiveAcceptMessage(received);
                        break;
                    case "NOT_ACCEPT_MESSAGE":
                        this.onReceiveNotAcceptMessage(received);
                        break;
                    case "LEAVE_TO_CHAT":
                        this.onReceiveLeaveChat(received);
                        break;
                    case "CHAT_MESSAGE":
                        this.onReceiveChatMessage(received);
                        break;
                    case "EXIT":
                        running = false;
                    case "INVITE_TO_VIDEO":
                        onReceiveVideoInvite(received);
                        break;
                    case "ACCEPT_VIDEO":
                        onReceiveVideoAccept(received);
                        break;
                    case "DECLINE_VIDEO":
                        onReceiveVideoDecline(received);
                        break;
                    case "END_VIDEO":
                        onReceiveVideoEnd(received);
                        break;
                    case "VIDEO_FRAME":
                        onReceiveVideoFrame(received);
                        break;
                }
            } catch (IOException var7) {
                ex = var7;
                Logger.getLogger(SocketHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
                running = false;
            }
        }

        try {
            this.s.close();
            this.dis.close();
            this.dos.close();
        } catch (IOException var6) {
            ex = var6;
            Logger.getLogger(SocketHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
        }

        JOptionPane.showMessageDialog((Component)null, "Mất kết nối tới server", "Lỗi", 0);
        ClientRun.closeAllScene();
        ClientRun.openScene(SceneName.CONNECTSERVER);
    }

    private void onReceiveVideoInvite(String received) {
        // Parse the received data
        String[] parts = received.split(";");
        String sender = parts[2];
        String receiver = parts[3];

        SwingUtilities.invokeLater(() -> {
            int response = JOptionPane.showConfirmDialog(
                    null,
                    "Incoming video call from " + sender + ". Accept?",
                    "Video Call Request",
                    JOptionPane.YES_NO_OPTION
            );

            if (response == JOptionPane.YES_OPTION) {
                acceptVideoCall(sender, receiver);
//                acceptVideoCall(received,sender);
                ClientRun.messageView.showVideoPanel();
            } else {
                declineVideoCall(sender, receiver);
            }
        });
    }

    private void onReceiveVideoAccept(String received) {
        String[] parts = received.split(";");
        String acceptingUser = parts[3];
        String callinguser = parts[2];

        SwingUtilities.invokeLater(() -> {
            ClientRun.messageView.showVideoPanel();
            startVideoStream(acceptingUser);
//            startVideoStream(callinguser);
        });
    }

    private void onReceiveVideoDecline(String received) {
        String[] parts = received.split(";");
        String decliningUser = parts[1];

        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    null,
                    decliningUser + " declined the video call",
                    "Call Declined",
                    JOptionPane.INFORMATION_MESSAGE
            );
            ClientRun.messageView.hideVideoPanel();
        });
    }

    private void onReceiveVideoEnd(String received) {
        String[] parts = received.split(";");
        String endingUser = parts[1];

        stopVideoStream();
        SwingUtilities.invokeLater(() -> {
            ClientRun.messageView.hideVideoPanel();
            JOptionPane.showMessageDialog(
                    null,
                    endingUser + " ended the video call",
                    "Call Ended",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });
    }
//    @Override
//    protected void finalize() throws Throwable {
//        stopVideoStream();
//        super.finalize();
//    }







private void onReceiveVideoFrame(String received) {
    try {
        System.out.println(this.getHost());
        // Validate received data integrity
        if (received == null || received.isEmpty()) {
            System.err.println("Received data is null or empty");
            return;
        }

        String[] splitted = received.split(";", -1);
        if (splitted.length < 5) {
            System.err.println("Invalid data format");
            return;
        }

        if (!"success".equals(splitted[1])) {
            System.err.println("Protocol error: " + splitted[1]);
            return;
        }

        String sender = splitted[2];

        String base64Data = processBase64Data(splitted[4]);

        if (base64Data != null) {
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));

            if (image != null) {
                ImageIcon frameIcon = new ImageIcon(image);

                // Update UI on EDT
                SwingUtilities.invokeLater(() -> {
                    if (sender.equals(loginUser)) {
                        ClientRun.messageView.updateOwnVideoPanel(frameIcon);
                        ClientRun.messageView.updateRemoteVideoPanel(frameIcon);
                    } else {
                        ClientRun.messageView.updateOwnVideoPanel(frameIcon);
                        ClientRun.messageView.updateRemoteVideoPanel(frameIcon);
                    }
                });
            }
        }
    } catch (Exception e) {
        System.err.println("Error processing video frame: " + e.getMessage());
        e.printStackTrace();
    }
}

    // Helper method to validate UTF-8 encoding
    private boolean isValidUTF8(String str) {
        try {
            byte[] bytes = str.getBytes("UTF-8");
            new String(bytes, "UTF-8");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Helper method to process Base64 data with multiple cleaning approaches
    private String processBase64Data(String rawData) {
        try {
            if (rawData == null || rawData.isEmpty()) {
                return null;
            }

            // 1. Basic cleaning
            String cleaned = rawData.trim()
                    .replace("\n", "")
                    .replace("\r", "")
                    .replace(" ", "");

            // 2. Remove any non-Base64 characters
            cleaned = cleaned.replaceAll("[^A-Za-z0-9+/=]", "");

            // 3. Fix padding
            while (cleaned.length() % 4 != 0) {
                cleaned += "=";
            }

            // 4. Validate minimum length
            if (cleaned.length() < 8) { // Minimum realistic length for an image
                System.err.println("Base64 string too short to be valid image data");
                return null;
            }

            return cleaned;

        } catch (Exception e) {
            System.err.println("Error processing Base64 data: " + e.getMessage());
            return null;
        }
    }

    // Helper method to create image with multiple fallback approaches
    private BufferedImage createImageWithFallbacks(String base64Data) {
        try {
            byte[] imageBytes;

            // 1. Try standard decoder
            try {
                imageBytes = Base64.getDecoder().decode(base64Data);
            } catch (IllegalArgumentException e) {
                // 2. Try MIME decoder
                try {
                    imageBytes = Base64.getMimeDecoder().decode(base64Data);
                } catch (IllegalArgumentException e2) {
                    // 3. Try URL-safe decoder
                    imageBytes = Base64.getUrlDecoder().decode(base64Data);
                }
            }

            // Validate image bytes
            if (!isValidImageBytes(imageBytes)) {
                System.err.println("Invalid image data detected");
                return null;
            }

            // Try multiple approaches to create the image
            BufferedImage image = null;

            // 1. Standard approach
            image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image != null) return image;

            // 2. Try with RGB color model
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            BufferedImage tempImage = ImageIO.read(bis);
            if (tempImage != null) {
                image = new BufferedImage(
                        tempImage.getWidth(),
                        tempImage.getHeight(),
                        BufferedImage.TYPE_INT_RGB
                );
                image.getGraphics().drawImage(tempImage, 0, 0, null);
                return image;
            }

            return null;

        } catch (Exception e) {
            System.err.println("Error creating image: " + e.getMessage());
            return null;
        }
    }

    // Helper method to validate image bytes
    private boolean isValidImageBytes(byte[] bytes) {
        if (bytes == null || bytes.length < 4) return false;

        // Check for common image format headers
        return (
                // JPEG header check
                (bytes[0] == (byte)0xFF && bytes[1] == (byte)0xD8) ||
                        // PNG header check
                        (bytes[0] == (byte)0x89 && bytes[1] == (byte)0x50 &&
                                bytes[2] == (byte)0x4E && bytes[3] == (byte)0x47) ||
                        // GIF header check
                        (bytes[0] == (byte)0x47 && bytes[1] == (byte)0x49 &&
                                bytes[2] == (byte)0x46) ||
                        // BMP header check
                        (bytes[0] == (byte)0x42 && bytes[1] == (byte)0x4D)
        );
    }
    public void login(String email, String password) {
        String data = "LOGIN;" + email + ";" + password;
        this.sendData(data);
    }

    public void register(String email, String password) {
        String data = "REGISTER;" + email + ";" + password;
        this.sendData(data);
    }

    public void logout() {
        this.loginUser = null;
        this.sendData("LOGOUT");
    }

    public void close() {
        this.sendData("CLOSE");
    }

    public void getListOnline() {
        this.sendData("GET_LIST_ONLINE");
    }



    private void sendData(String data) {
        try {
            dos.writeUTF(data);
            dos.flush();
        } catch (IOException e) {
            System.err.println("Error sending data: " + e.getMessage());
        }
    }









    public void inviteToChat(String userInvited) {
        this.sendData("INVITE_TO_CHAT;" + this.loginUser + ";" + userInvited);
    }

    public void leaveChat(String userInvited) {
        this.sendData("LEAVE_TO_CHAT;" + this.loginUser + ";" + userInvited);
    }

    public void sendMessage(String userInvited, String message) {
        String chat = "[" + this.loginUser + "] : " + message + "\n";
        ClientRun.messageView.setContentChat(chat);
        this.sendData("CHAT_MESSAGE;" + this.loginUser + ";" + userInvited + ";" + message);
    }

    private void onReceiveLogin(String received) {
        String[] splitted = received.split(";");
        String status = splitted[1];
        if (status.equals("failed")) {
            String failedMsg = splitted[2];
            JOptionPane.showMessageDialog(ClientRun.loginView, failedMsg, "Lỗi", 0);
        } else if (status.equals("success")) {
            this.loginUser = splitted[2];
            ClientRun.closeScene(SceneName.LOGIN);
            ClientRun.openScene(SceneName.HOMEVIEW);
//            ClientRun.homeView.setUsername(this.loginUser);
//            ClientRun.homeView.setUserScore(this.score);
        }

    }

    private void onReceiveRegister(String received) {
        String[] splitted = received.split(";");
        String status = splitted[1];
        if (status.equals("failed")) {
            String failedMsg = splitted[2];
            JOptionPane.showMessageDialog(ClientRun.registerView, failedMsg, "Lỗi", 0);
        } else if (status.equals("success")) {
            JOptionPane.showMessageDialog(ClientRun.registerView, "Register account successfully! Please login!");
            ClientRun.closeScene(SceneName.REGISTER);
            ClientRun.openScene(SceneName.LOGIN);
        }

    }

    private void onReceiveGetListOnline(String received) {
        String[] splitted = received.split(";");
        String status = splitted[1];
        if (status.equals("success")) {
            int userCount = Integer.parseInt(splitted[2]);
            Vector vheader = new Vector();
            vheader.add("User");
            Vector vdata = new Vector();
            if (userCount > 1) {
                for(int i = 3; i < userCount + 3; ++i) {
                    String user = splitted[i];
                    if (!user.equals(this.loginUser) && !user.equals("null")) {
                        Vector vrow = new Vector();
                        vrow.add(user);
                        vdata.add(vrow);
                    }
                }

                ClientRun.homeView.setListUser(vdata, vheader);
            } else {
                ClientRun.homeView.resetTblUser();
            }
        } else {
            JOptionPane.showMessageDialog(ClientRun.loginView, "Have some error!", "Lỗi", 0);
        }

    }

    private void onReceiveLogout(String received) {
        String[] splitted = received.split(";");
        String status = splitted[1];
        if (status.equals("success")) {
            ClientRun.closeScene(SceneName.HOMEVIEW);
            ClientRun.openScene(SceneName.LOGIN);
        }

    }

    private void onReceiveInviteToChat(String received) {
        String[] splitted = received.split(";");
        String status = splitted[1];
        if (status.equals("success")) {
            String userHost = splitted[2];
            String userInvited = splitted[3];
            if (JOptionPane.showConfirmDialog(ClientRun.homeView, userHost + " want to chat with you?", "Chat?", 0) == 0) {
                ClientRun.openScene(SceneName.MESSAGEVIEW);
                ClientRun.messageView.setInfoUserChat(userHost);
                this.sendData("ACCEPT_MESSAGE;" + userHost + ";" + userInvited);

            } else {
                this.sendData("NOT_ACCEPT_MESSAGE;" + userHost + ";" + userInvited);
            }
        }

    }

    private void onReceiveAcceptMessage(String received) {
        String[] splitted = received.split(";");
        String status = splitted[1];
        if (status.equals("success")) {
            String userHost = splitted[2];
            String userInvited = splitted[3];
            setHost(userHost);
            ClientRun.openScene(SceneName.MESSAGEVIEW);
            ClientRun.messageView.setInfoUserChat(userInvited);
        }

    }

    private void onReceiveNotAcceptMessage(String received) {
        String[] splitted = received.split(";");
        String status = splitted[1];
        if (status.equals("success")) {
            String userHost = splitted[2];
            String userInvited = splitted[3];
            JOptionPane.showMessageDialog(ClientRun.homeView, userInvited + " don't want to chat with you!");
        }

    }

    private void onReceiveLeaveChat(String received) {
        String[] splitted = received.split(";");
        String status = splitted[1];
        if (status.equals("success")) {
            String userHost = splitted[2];
            String userInvited = splitted[3];
            ClientRun.closeScene(SceneName.MESSAGEVIEW);
            JOptionPane.showMessageDialog(ClientRun.homeView, userHost + " leave to chat!");
        }

    }

    private void onReceiveChatMessage(String received) {
        String[] splitted = received.split(";");
        String status = splitted[1];
        if (status.equals("success")) {
            String userHost = splitted[2];
            String userInvited = splitted[3];
            String message = splitted[4];
            String chat = "[" + userHost + "] : " + message + "\n";
            ClientRun.messageView.setContentChat(chat);
        }

    }



public void setHost (String host)
{
    this.host= host;
}
public String getHost() {
    return this.host;
}


    public String getLoginUser() {
        return loginUser;
    }
}
