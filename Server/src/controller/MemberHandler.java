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
import view.ServerView;

/**
 *
 * @author Hoang Pham
 */
public class MemberHandler implements Runnable{
    public Socket member;
    public static ArrayList<MemberHandler> list_members = new ArrayList<>();
    public ObjectInputStream oin;
    public ObjectOutputStream oout;
    public String nickname = "";
    public int room_id;
    
    public MemberHandler() {
    }
    
    public MemberHandler(Socket member, int room_id) throws Exception {
        this.room_id = room_id;
        this.member = member;
        this.oin = new ObjectInputStream(member.getInputStream());
        this.oout = new ObjectOutputStream(member.getOutputStream());
    }
    
    @Override
    public void run() {
        String msg_client = "";
        String msg_server = "";
        Object o;
        
        this.nickname = (String) this.recv_member_msg();
        ServerController.setMsg_area("Member [" + this.nickname + "] "
                + "is connected to room " + this.room_id);
        System.out.println(this.nickname + ": " + this.member);
        
        // Send welcome text to client
        msg_server = "[SERVER]:&nbsp&nbsp Welcome " + this.nickname + 
                "! You are connected to room " + this.room_id + "!";
        this.send_room_msg(msg_server);
        msg_server = "[SERVER]:&nbsp&nbsp " + this.nickname + " joined the room!";
        this.broadcast_other_clients(msg_server);
        
        while (!this.member.isClosed()) {
            o = this.recv_member_msg();
            if (this.member.isClosed()) {
                list_members.remove(this);
                ServerController.setMsg_area("Member [" + this.nickname + "] "
                        + "was disconnected room " + this.room_id);
                int count = numberMemberInRoom(room_id);
                if (count == 0) {
                    for (RoomController rc : RoomController.list_room) {
                        if (rc.room_id == this.room_id) {
                            try {
                                rc.room_model.server.close();
                            }
                            catch(Exception e) {
                                System.out.println("Can not close room server!");
                            }
                            RoomController.list_room.remove(rc);
                            break;
                        }
                    }
                }
                break;
            }
            
            if (o instanceof String) {
                msg_client = (String) o;
                if (msg_client.equals("Show members!")) {
                    this.send_room_msg("List members!");
                    this.send_room_msg(""+this.room_id);
                    String str_members = this.get_all_members();
                    this.send_room_msg(str_members);
                }
                else if (msg_client.equals("Admin wants to kick a member!")) {
                    String kicked_nickname = (String) this.recv_member_msg();
                    msg_server = "You are kicked!";
                    this.send_msg_server_specified(kicked_nickname, msg_server);
                    msg_server = "[SERVER]: " + kicked_nickname + " was kicked by admin!";
                    this.broadcast_all_except(kicked_nickname, msg_server);
                }
                else if (msg_client.equals("Start camera!")) {
                    WebcamController wc = new WebcamController(this.room_id);
                    wc.start();
                    
                    this.send_room_msg("Accept to start camera!");
                    this.send_room_msg(""+this.room_id);
                }
                else {
                    String msg_send = "[" + this.nickname + "]:&nbsp&nbsp " + msg_client;
                    this.broadcast_other_clients(msg_send);
                }
            }
            else {
                String msg_send = "[" + this.nickname + "]:&nbsp&nbsp " + "<img src='" + o.toString() + "'></img>";
                this.broadcast_other_clients(msg_send);
            }
        }
        this.broadcast_all("[SERVER]:&nbsp&nbsp " + this.nickname + " left.");
    }
    
    // Recieve msg from member in room chat
    public Object recv_member_msg() {
        Object o = "No msg from client";
        try {
            o = this.oin.readObject();
        }
        catch(Exception e) {
            try {
                this.member.close();
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
                this.member.close();
            }
            catch(Exception ee) {
                System.out.println(ee);
            }
        }
    }
    
    // Send msg of server to all members in room chat except for the owner
    public void broadcast_all(Object o) {
        try {
            for (MemberHandler mh : list_members) {
                if (mh.room_id == this.room_id) {
                    mh.oout.writeObject(o);
                }
            }
        }
        catch(Exception e) {
            try {
                this.member.close();
            }
            catch(Exception ee) {
                System.out.println(ee);
            }
        }
    }
    
    // Send msg of server to all members in room chat except for the owner
    public void broadcast_other_clients(Object o) {
        try {
            for (MemberHandler mh : list_members) {
                if (mh != this && mh.room_id == this.room_id) {
                    mh.oout.writeObject(o);
                }
            }
        }
        catch(Exception e) {
            try {
                this.member.close();
            }
            catch(Exception ee) {
                System.out.println(ee);
            }
        }
    }
    
    // Broadcast a msg to all member in a room except for specified member
    public void broadcast_all_except(String nickname, Object o) {
        try {
            for (MemberHandler mh : list_members) {
                if ((!mh.nickname.equals(nickname)) && (mh.room_id == this.room_id)) {
                    mh.oout.writeObject(o);
                }
            }
        }
        catch(Exception e) {
            try {
                this.member.close();
            }
            catch(Exception ee) {
                System.out.println(ee);
            }
        }
    }
    
    // Send object to a specified member
    public void send_msg_server_specified(String nickname, Object o) {
        try {
            for (MemberHandler mh : list_members) {
                if (mh.nickname.equals(nickname)) {
                    mh.oout.writeObject(o);
                    break;
                }
            }
        }
        catch(Exception e) {
            try {
                this.member.close();
            }
            catch(Exception ee) {
                System.out.println(ee);
            }
        }
    }
    
    public int numberMemberInRoom(int room_id) {
        int count = 0;
        for (MemberHandler mh : list_members) {
            if (mh.room_id == room_id) {
                count++;
            }
        }
        return count;
    }
    
    // Get all members in a room
    public String get_all_members() {
        String str_members = "";
        for (MemberHandler mh : list_members) {
            if (mh.room_id == this.room_id) {
                str_members += mh.nickname + "-" + 
                    mh.member.getLocalAddress().toString().substring(1) + "-" + 
                    mh.member.getPort() + "-" + 
                    mh.member.getLocalPort() + ";";
            }
        }
        return str_members;
    }
}
