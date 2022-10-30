/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import model.ServerModel;

/**
 *
 * @author Hoang Pham
 */
public class RoomController extends Thread {
    
    private static ExecutorService pool = Executors.newFixedThreadPool(10);
    public static ArrayList<RoomController> list_room = new ArrayList<>();
    public ServerModel room_model;
    public int room_id;
    public String room_pass;
    
    public RoomController() {
    }
    
    public RoomController(int room_id, String room_pass) {
        this.room_id = room_id;
        this.room_pass = room_pass;
        
        room_model = new ServerModel();
        try {
            InetAddress inet_address = InetAddress.getLocalHost();
            int port_server = room_id;
            InetSocketAddress inet_socket_address = 
                    new InetSocketAddress(inet_address, port_server);
            
            room_model.bind_server(inet_socket_address);
        }
        catch(Exception e) {
            System.out.println("Can not create server!");
        }       
    }
    
    @Override
    public void run() {
        ServerController.setMsg_area("Room " + this.room_id + " is created");
        
        while (!this.room_model.server.isClosed()) {
            try {
                // Accept new client
                Socket member = room_model.server.accept();
                System.out.println(member);
                
                // Create new thread controller for new client
                MemberHandler mh = new MemberHandler(member, room_id);
                MemberHandler.list_members.add(mh);
                pool.execute(mh);
            }
            catch(Exception e) {
                break;
            }
        }
    }
}
