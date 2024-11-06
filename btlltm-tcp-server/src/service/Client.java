/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import controller.UserController;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import run.ServerRun;

import java.sql.SQLException;

/**
 *
 * @author admin
 */
public class Client implements Runnable {
    Socket s;
    DataInputStream dis;
    DataOutputStream dos;

    String loginUser;




    public Client(Socket s) throws IOException {
        this.s = s;

        // obtaining input and output streams
        this.dis = new DataInputStream(s.getInputStream());
        this.dos = new DataOutputStream(s.getOutputStream());
    }

    @Override
    public void run() {

        String received;
        boolean running = true;

        while (!ServerRun.isShutDown) {
            try {
                // receive the request from client
                received = dis.readUTF();

                System.out.println(received);
                String type = received.split(";")[0];

                switch (type) {
                    case "LOGIN":
                        onReceiveLogin(received);
                        break;
                    case "REGISTER":
                        onReceiveRegister(received);
                        break;
                    case "GET_LIST_ONLINE":
                        onReceiveGetListOnline();
                        break;
                    case "LOGOUT":
                        onReceiveLogout();
                        break;
                    case "CLOSE":
                        onReceiveClose();
                        break;
                    // chat
                    case "INVITE_TO_CHAT":
                        onReceiveInviteToChat(received);
                        break;
                    case "ACCEPT_MESSAGE":
                        onReceiveAcceptMessage(received);
                        break;
                    case "NOT_ACCEPT_MESSAGE":
                        onReceiveNotAcceptMessage(received);
                        break;
                    case "LEAVE_TO_CHAT":
                        onReceiveLeaveChat(received);
                        break;
                    case "CHAT_MESSAGE":
                        onReceiveChatMessage(received);
                        break;

                    case "EXIT":
                        running = false;
                    case "INVITE_TO_VIDEO":
                        onReceiveInviteToVideo(received);
                        break;
                    case "ACCEPT_VIDEO":
                        onReceiveAcceptVideo(received);
                        break;
                    case "DECLINE_VIDEO":
                        onReceiveDeclineVideo(received);
                        break;
                    case "END_VIDEO":
                        onReceiveEndVideo(received);
                        break;
                    case "VIDEO_FRAME":
                        onReceiveVideoFrame(received);
                        break;
                }

            } catch (IOException ex) {


                break;
            }
        }

        try {
            // closing resources
            this.s.close();
            this.dis.close();
            this.dos.close();
            System.out.println("- Client disconnected: " + s);

            // remove from clientManager
            ServerRun.clientManager.remove(this);

        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void onReceiveInviteToVideo(String received) {
        String[] splitted = received.split(";");
        String userHost = splitted[1];
        String userInvited = splitted[2];

        // Forward invitation to invited user
        String msg = "INVITE_TO_VIDEO;success;" + userHost + ";" + userInvited;
        ServerRun.clientManager.sendToAClient(userInvited, msg);
    }

    private void onReceiveAcceptVideo(String received) {
        String[] splitted = received.split(";");
        String userHost = splitted[1];
        String userInvited = splitted[2];

        // Notify host that invited user accepted
        String msg = "ACCEPT_VIDEO;success;" + userHost + ";" + userInvited;
        String msg1 = "ACCEPT_VIDEO;success;" + userInvited + ";" + userHost;
        ServerRun.clientManager.sendToAClient(userHost, msg);
        ServerRun.clientManager.sendToAClient(userInvited,msg1);
    }

    private void onReceiveDeclineVideo(String received) {
        String[] splitted = received.split(";");
        String userHost = splitted[1];
        String userInvited = splitted[2];

        // Notify host that invited user declined
        String msg = "DECLINE_VIDEO;success;" + userHost + ";" + userInvited;
        ServerRun.clientManager.sendToAClient(userHost, msg);
    }

    private void onReceiveEndVideo(String received) {
        String[] splitted = received.split(";");
        String userHost = splitted[1];
        String userInvited = splitted[2];

        // Notify other user that call ended
        String msg = "END_VIDEO;success;" + userHost + ";" + userInvited;
        ServerRun.clientManager.sendToAClient(userInvited, msg);
    }

    private void onReceiveVideoFrame(String received) {
        String[] splitted = received.split(";");
        String sender = splitted[1];     // Người gửi frame
        String receiver = splitted[2];   // Người nhận frame
        String frameData = splitted[3];  // Dữ liệu frame
        System.out.println("Received VIDEO_FRAME from " + sender + " to " + receiver);

        // Chuyển tiếp frame từ sender đến receiver
        String msg = "VIDEO_FRAME;success;" + sender + ";" + receiver + ";" + frameData;
        ServerRun.clientManager.sendToAClient(receiver, msg);

        // Đảm bảo chuyển tiếp dữ liệu video theo cả hai chiều
//        if (!sender.equals(receiver)) {
//            String reverseMsg = "VIDEO_FRAME;success;" + receiver + ";" + sender + ";" + frameData;
//            ServerRun.clientManager.sendToAClient(sender, reverseMsg);
//        }
    }



    // send data fucntions
    public String sendData(String data) {
        try {
            this.dos.writeUTF(data);
            return "success";
        } catch (IOException e) {
            System.err.println("Send data failed!");
            return "failed;" + e.getMessage();
        }
    }

    private void onReceiveLogin(String received) {
        // Kiểm tra xem dữ liệu có hợp lệ không
        if (received == null || !received.contains(";")) {
            sendData("LOGIN;failed;Invalid input data!");
            return;
        }

        // Tách email và password từ dữ liệu nhận được
        String[] splitted = received.split(";");

        if (splitted.length < 3) {
            sendData("LOGIN;failed;Missing username or password!");
            return;
        }

        String username = splitted[1].trim(); // Trim để loại bỏ khoảng trắng
        String password = splitted[2].trim();

        // Kiểm tra đăng nhập
        String result = new UserController().login(username, password);

        // Xử lý kết quả đăng nhập
        if (result.split(";")[0].equals("success")) {
            // Đặt người dùng đăng nhập
            this.loginUser = username;
        }

        // Gửi kết quả
        sendData("LOGIN;" + result);
        onReceiveGetListOnline();
    }

    private void onReceiveRegister(String received) {
        // get email / password from data
        String[] splitted = received.split(";");
        String username = splitted[1];
        System.out.println(username);
        String password = splitted[2];
        System.out.println(password);
        // reigster
        String result = new UserController().register(username, password);

        // send result
        sendData("REGISTER" + ";" + result);
    }

    private void onReceiveGetListOnline() {
        String result = ServerRun.clientManager.getListUseOnline();

        // send result
        String msg = "GET_LIST_ONLINE" + ";" + result;
        ServerRun.clientManager.broadcast(msg);
    }



    private void onReceiveLogout() {
        this.loginUser = null;
        // send result
        sendData("LOGOUT" + ";" + "success");
        onReceiveGetListOnline();
    }

    private void onReceiveInviteToChat(String received) {
        String[] splitted = received.split(";");
        String userHost = splitted[1];
        String userInvited = splitted[2];

        // send result
        String msg = "INVITE_TO_CHAT;" + "success;" + userHost + ";" + userInvited;
        ServerRun.clientManager.sendToAClient(userInvited, msg);
    }

    private void onReceiveAcceptMessage(String received) {
        String[] splitted = received.split(";");
        String userHost = splitted[1];
        String userInvited = splitted[2];

        // send result
        String msg = "ACCEPT_MESSAGE;" + "success;" + userHost + ";" + userInvited;
        ServerRun.clientManager.sendToAClient(userHost, msg);
    }

    private void onReceiveNotAcceptMessage(String received) {
        String[] splitted = received.split(";");
        String userHost = splitted[1];
        String userInvited = splitted[2];

        // send result
        String msg = "NOT_ACCEPT_MESSAGE;" + "success;" + userHost + ";" + userInvited;
        ServerRun.clientManager.sendToAClient(userHost, msg);
    }

    private void onReceiveLeaveChat(String received) {
        String[] splitted = received.split(";");
        String userHost = splitted[1];
        String userInvited = splitted[2];

        // send result
        String msg = "LEAVE_TO_CHAT;" + "success;" + userHost + ";" + userInvited;
        ServerRun.clientManager.sendToAClient(userInvited, msg);
    }

    private void onReceiveChatMessage(String received) {
        String[] splitted = received.split(";");
        String userHost = splitted[1];
        String userInvited = splitted[2];
        String message = splitted[3];

        // send result
        String msg = "CHAT_MESSAGE;" + "success;" + userHost + ";" + userInvited + ";" + message;
        ServerRun.clientManager.sendToAClient(userInvited, msg);
    }
















    // Close app
    private void onReceiveClose() {
        this.loginUser = null;
        ServerRun.clientManager.remove(this);
        onReceiveGetListOnline();
    }

    // Get set
    public String getLoginUser() {
        return loginUser;
    }



}
