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
public class WebcamController extends Thread {
    private static ExecutorService pool = Executors.newFixedThreadPool(10);
    public static ServerModel webcam_model;
    public int room_id;
    
    public WebcamController() {
    }
    
    public WebcamController(int room_id) {
        this.room_id = room_id;
        webcam_model = new ServerModel();
        try {
            InetAddress inet_address = InetAddress.getLocalHost();
            int port_server = room_id + 1;
            InetSocketAddress inet_socket_address = 
                    new InetSocketAddress(inet_address, port_server);
            
            webcam_model.bind_server(inet_socket_address);
        }
        catch(Exception e) {
            System.out.println("Can not create server!");
        }       
    }
    
    @Override
    public void run() {
        ServerController.setMsg_area("Video call at room " + this.room_id + " is created");
        
        while (!this.webcam_model.server.isClosed()) {
            try {
                // Accept new client
                Socket video = webcam_model.server.accept();
                System.out.println(video);
                
                // Create new thread controller for new client
                VideoHandler vh = new VideoHandler(video, room_id);
                VideoHandler.list_videos.add(vh);
                pool.execute(vh);
            }
            catch(Exception e) {
                break;
            }
        }
    }
}
