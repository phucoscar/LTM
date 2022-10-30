/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import com.github.sarxos.webcam.Webcam;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import javax.swing.ImageIcon;
import model.ClientModel;
import view.VideoRecieveView;
import view.VideoSendView;

/**
 *
 * @author Hoang Pham
 */
public class VideoController {
    public ClientModel video_model;
    public Socket video_socket;
    public ObjectOutputStream oout;
    public ObjectInputStream oin;
    public String nickname;

    public VideoController() {
    }
    
    public VideoController(String nickname, int room_id) {
        this.nickname = nickname;
        InetSocketAddress inet_socket_address = null;
        try {
            InetAddress inet_address = InetAddress.getLocalHost();
            int port_server = room_id + 1;
            inet_socket_address = 
                    new InetSocketAddress(inet_address, port_server);
        }
        catch(Exception e) {
            System.out.println(e);
        }
        
        this.video_model = new ClientModel();
        video_model.connect_server(inet_socket_address);
        this.video_socket = video_model.getClient();
        this.oout = this.video_model.getOout();
        this.oin = this.video_model.getOin();
    }
    
    // Recieve object from server
    public Object recv_server_msg() {
        Object server_object = new Object();
        try {
            server_object = this.oin.readObject();
        }
        catch(Exception e) {
            try {
                this.video_socket.close();
            }
            catch(Exception ee) {
                System.out.println(ee);
            }
        }
        return server_object;
    }
    
    // Send object from client
    public void send_client_msg(Object o) {
        try {
            this.oout.writeObject(o);
        }
        catch(Exception e) {
            try {
                this.video_socket.close();
            }
            catch(Exception ee) {
                System.out.println(ee);
            }
        }
    }
    
    // Open camera and send frame to server
    public class VideoSendThread extends Thread {
        public boolean isOpened;
        @Override
        public void run() {
            ImageIcon ic;
            BufferedImage br;
            Webcam cam = Webcam.getDefault();
            Dimension d = new Dimension(320, 240);
            cam.setViewSize(d);
            cam.open(false);
            isOpened = true;
            
            while (isOpened == true) {
                if (video_socket.isClosed()) {
                    break;
                }
                try {
                    br = cam.getImage();
                    ic = new ImageIcon(br);
                    VideoSendView.video_area.setIcon(ic);
                    oout.writeObject(ic);
                }
                catch(Exception e) {
                }
            }
            cam.close();
        }
    }
    
    // Recieve frame sent by server
    public class VideoRecieveThread extends Thread {
        public boolean isOpened;
        @Override
        public void run() {
            isOpened = true;
            while (isOpened == true) {
                if (video_socket.isClosed()) {
                    break;
                }
                try {
                    Object o = oin.readObject();
                    ImageIcon ic = (ImageIcon) o; 
                    VideoRecieveView.video_area.setIcon(ic);
                }
                catch(Exception e) {
                    break;
                }
            }
        }
    }
}
