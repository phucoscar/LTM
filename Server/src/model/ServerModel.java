/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.net.InetSocketAddress;
import java.net.ServerSocket;

/**
 *
 * @author Hoang Pham
 */
public class ServerModel {
    
    public ServerSocket server;
    
    public ServerModel() {
    }
    
    public void bind_server(InetSocketAddress inet_socket_address) {
        try {
            this.server = new ServerSocket();
            this.server.bind(inet_socket_address);
        }
        catch(Exception e) {
            System.out.println("Can not bind server!");
        }
    }

    public ServerSocket getServer() {
        return server;
    }

    public void setServer(ServerSocket server) {
        this.server = server;
    }
}
