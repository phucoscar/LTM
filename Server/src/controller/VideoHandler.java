/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author Hoang Pham
 */
public class VideoHandler implements Runnable {
    public Socket video;
    public static ArrayList<VideoHandler> list_videos = new ArrayList<>();
    public ObjectInputStream oin;
    public ObjectOutputStream oout;
    public String nickname = "";
    public int room_id;
    
    public VideoHandler() {
    }
    
    public VideoHandler(Socket video, int room_id) throws Exception {
        this.room_id = room_id;
        this.video = video;
        this.oin = new ObjectInputStream(video.getInputStream());
        this.oout = new ObjectOutputStream(video.getOutputStream());
    }
    
    @Override
    public void run() {
        String msg_client = "";
        String msg_server = "";
        Object o;
        
        while (!this.video.isClosed()) {
            o = this.recv_member_msg();
            if (this.video.isClosed()) {
                list_videos.remove(this);
                if (list_videos.isEmpty()) {
                    try {
                        WebcamController.webcam_model.server.close();
                        ServerController.setMsg_area("Video call at room " + this.room_id + " ended");
                    }
                    catch(Exception e) {
                        System.out.println(e);
                    }
                    break;
                }
                break;
            }
            broadcast_other_members(o);
        }
    }
    
    // Recieve msg from member in room chat
    public Object recv_member_msg() {
        Object o = "No msg from client";
        try {
            o = this.oin.readObject();
        }
        catch(Exception e) {
            try {
                this.video.close();
            }
            catch(Exception ee) {
                System.out.println(ee);
            }
        }
        return o;
    }
    
    // Send msg of server to a member in room chat
    public void send_room_msg(Object o) {
        try {
            this.oout.writeObject(o);
        }
        catch(Exception e) {
            try {
                this.video.close();
            }
            catch(Exception ee) {
                System.out.println(ee);
            }
        }
    }
    
    // Send msg of server to all members in room chat except for the owner
    public void broadcast_all(Object o) {
        try {
            for (VideoHandler vh : list_videos) {
                if (vh.room_id == this.room_id) {
                    vh.oout.writeObject(o);
                }
            }
        }
        catch(Exception e) {
            try {
                this.video.close();
            }
            catch(Exception ee) {
                System.out.println(ee);
            }
        }
    }
    
    // Send msg of server to all members in room chat except for the owner
    public void broadcast_other_members(Object o) {
        try {
            for (VideoHandler vh : list_videos) {
                if (vh != this && vh.room_id == this.room_id) {
                    vh.oout.writeObject(o);
                }
            }
        }
        catch(Exception e) {
            try {
                this.video.close();
            }
            catch(Exception ee) {
                System.out.println(ee);
            }
        }
    }
}
