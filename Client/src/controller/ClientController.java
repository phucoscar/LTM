/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import model.ClientModel;

/**
 *
 * @author Hoang Pham
 */
public class ClientController {
    public ClientModel client_model;
    public Socket client_socket;
    public ObjectOutputStream oout;
    public ObjectInputStream oin;
    public String nickname;
    
    public ClientController() {
        InetSocketAddress inet_socket_address = null;
        try {
            InetAddress inet_address = InetAddress.getLocalHost();
            int port_server = 30030;
            inet_socket_address = 
                    new InetSocketAddress(inet_address, port_server);
        }
        catch(Exception e) {
            System.out.println(e);
        }
        
        this.client_model = new ClientModel();
        client_model.connect_server(inet_socket_address);
        this.client_socket = client_model.getClient();
        this.oout = this.client_model.getOout();
        this.oin = this.client_model.getOin();
    }
    
    // Recieve object from server
    public Object recv_server_msg() {
        Object server_object = new Object();
        try {
            server_object = this.oin.readObject();
        }
        catch(Exception e) {
            try {
                this.client_socket.close();
            }
            catch(Exception ee) {
                System.out.println(ee);
            }
        }
        return server_object;
    }
    
    // Recieve object from server
    public void send_client_msg(Object o) {
        try {
            this.oout.writeObject(o);
        }
        catch(Exception e) {
            try {
                this.client_socket.close();
            }
            catch(Exception ee) {
                System.out.println(ee);
            }
        }
    }
}
