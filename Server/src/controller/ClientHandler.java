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
public class ClientHandler implements Runnable{
    public Socket client;
    public static ArrayList<ClientHandler> list_clients = new ArrayList<>();
    public ObjectInputStream oin;
    public ObjectOutputStream oout;
    public String nickname = "";
    
    public ClientHandler() {
    }

    public ClientHandler(Socket client) throws Exception {
        this.client = client;
        this.oin = new ObjectInputStream(client.getInputStream());
        this.oout = new ObjectOutputStream(client.getOutputStream());
    }
    
    @Override
    public void run() {
        String msg_client = "";
        String msg_server = "";
        while (true) {
            msg_client = (String) this.recv_client_msg(); // Recieve client's nickname
            if (msg_client.equals("Client joined server!")) { // Don't check password
                break;
            }
            msg_server = this.checkNicknameExist(msg_client);
            this.send_server_msg(msg_server);
        }
        
        this.nickname = (String) this.recv_client_msg();
        ServerController.setMsg_area("Client [" + this.nickname + "] is connected to server");
        System.out.println(this.nickname + ": " + this.client);
        
        while (!this.client.isClosed()) {
            msg_client = (String) this.recv_client_msg();
            if (this.client.isClosed()) {
                list_clients.remove(this);
                ServerController.setMsg_area("Client [" + this.nickname + "] was disconnected server");
                break;
            }
            if (msg_client.equals("Client want to create room!")) {
                int room_id = 0;
                String room_pass = "";
                
                while (true) {
                    msg_client = (String) this.recv_client_msg();
                    
                    
                    if (msg_client.equals("Member created room!")) {
                        break;
                    }
                    room_id = Integer.parseInt((String) this.recv_client_msg()); // get room id
                    msg_server = this.checkRoomIdExist(room_id); // Check room id
                    this.send_server_msg(msg_server);
                    if (!msg_server.equals("Room id existed!") &&
                            !msg_server.equals("Room id is not in range!")) {
                        room_pass = (String) this.recv_client_msg(); // Get room pass
                    }
                }
                
                RoomController room = new RoomController(room_id, room_pass);
                RoomController.list_room.add(room);
                room.start();
                
                this.send_server_msg("Room is created!");
            }
            if (msg_client.equals("Client want to join room!")) {
                int room_id = 0;
                String room_pass = "";
                
                while (true) {
                    msg_client = (String) this.recv_client_msg();
                    if (msg_client.equals("Member joined room!")) {
                        break;
                    }
                    room_id = Integer.parseInt((String) this.recv_client_msg());
                    room_pass = (String) this.recv_client_msg();
                    
                    msg_server = this.checkJoinRoom(room_id, room_pass);
                    this.send_server_msg(msg_server);
                }
            }
        }
    }
    
    public Object recv_client_msg() {
        Object o = "No msg from client";
        try {
            o = this.oin.readObject();
        }
        catch(Exception e) {
            try {
                this.client.close();
            }
            catch(Exception ee) {
                System.out.println(ee);
            }
        }
        return o;
    }
    
    public void send_server_msg(Object o) {
        try {
            this.oout.writeObject(o);
        }
        catch(Exception e) {
            try {
                this.client.close();
            }
            catch(Exception ee) {
                System.out.println(ee);
            }
        }
    }
    
    // Check nickname existence
    public String checkNicknameExist(String nickname) {
        for (ClientHandler ch : list_clients) {
            if (ch.nickname.toLowerCase().equals(nickname.toLowerCase())) {
                return "Nickname existed!";
            }
        }
        return "Nickname unexisted!";
    }
    
    // Check room id existence
    public String checkRoomIdExist(int room_id) {
        if (room_id < 40000 || room_id > 50000) {
            return "Room id is not in range!";
        }
        if (RoomController.list_room.size() > 0) {
            for (RoomController rc : RoomController.list_room) {
                if (rc.room_id == room_id) {
                    return "Room id existed!";
                }
            }
        }
        return "Room id unexisted!";
    }
    
    // Check room id and password to join room
    public String checkJoinRoom(int room_id, String room_pass) {
        if (RoomController.list_room.size() > 0) {
            for (RoomController rc : RoomController.list_room) {
                if (rc.room_id == room_id) {
                    if (rc.room_pass.equals(room_pass)) {
                        return "Accepted to join!";
                    }
                    else {
                        return "Wrong password!";
                    }
                }
            }
        }
        return "Room id unexisted!";
    }
}
