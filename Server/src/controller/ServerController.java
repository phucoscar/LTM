/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import model.ServerModel;
import view.ServerView;

/**
 *
 * @author Hoang Pham
 */
public class ServerController extends Thread{
    
    private static ExecutorService pool = Executors.newFixedThreadPool(10);
    public ServerModel sm;
    
    public ServerController() {
        sm = new ServerModel();
        try {
            InetAddress inet_address = InetAddress.getLocalHost();
            int port_server = 30030;
            InetSocketAddress inet_socket_address = 
                    new InetSocketAddress(inet_address, port_server);
            
            sm.bind_server(inet_socket_address);
        }
        catch(Exception e) {
            System.out.println("Can not create server!");
        }       
    }
    
    @Override
    public void run() {
        ServerController.setMsg_area("Server is listening ...");
        
        while (!this.sm.server.isClosed()) {
            try {
                // Accept new client
                Socket s = sm.server.accept();

                // Create new thread controller for new client
                ClientHandler ch = new ClientHandler(s);
                ClientHandler.list_clients.add(ch);
                pool.execute(ch);
            }
            catch(Exception e) {
                break;
            }
        }
    }
    
    // Set text area
    public static void setMsg_area(String msg) {
        if (ServerView.msg_area.getText().trim().equals("")) {
            ServerView.msg_area.setText(msg);
        }
        else {
            ServerView.msg_area.setText(ServerView.msg_area.getText().trim() + "\n" + msg);
        }
    }
}
