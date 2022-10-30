/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import model.ClientModel;
import view.ChatView;
import view.MembersView;
import view.VideoSendView;

/**
 *
 * @author Hoang Pham
 */
public class MemberController {
    public ClientModel member_model;
    public Socket member_socket;
    public ObjectOutputStream oout;
    public ObjectInputStream oin;
    public String nickname;
    public String role;
    public static MembersView mv;
    
    public MemberController() {
    }
    
    public MemberController(String nickname, int room_id, String role) {
        this.nickname = nickname;
        this.role = role;
        InetSocketAddress inet_socket_address = null;
        try {
            InetAddress inet_address = InetAddress.getLocalHost();
            int port_server = room_id;
            inet_socket_address = 
                    new InetSocketAddress(inet_address, port_server);
        }
        catch(Exception e) {
            System.out.println(e);
        }
        
        this.member_model = new ClientModel();
        member_model.connect_server(inet_socket_address);
        this.member_socket = member_model.getClient();
        this.oout = this.member_model.getOout();
        this.oin = this.member_model.getOin();
    }
    
    // Recieve object from server
    public Object recv_server_msg() {
        Object server_object = new Object();
        try {
            server_object = this.oin.readObject();
        }
        catch(Exception e) {
            try {
                this.member_socket.close();
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
                this.member_socket.close();
            }
            catch(Exception ee) {
                System.out.println(ee);
            }
        }
    }
    
    public static void setMsg_area(String msg) {
        HTMLDocument doc = (HTMLDocument) ChatView.msg_area.getDocument();
                        HTMLEditorKit editorKit = (HTMLEditorKit) ChatView.msg_area.getEditorKit();
        try {
            editorKit.insertHTML(doc, doc.getLength(), "<div>"+msg+"</div>", 0, 0, null);
            ChatView.msg_area.setCaretPosition(doc.getLength());
        }                
        
        catch(Exception e) {
                System.out.println(e);
        }
    }
    
    public class ChatThread extends Thread {
        public MemberController mc;
        
        public ChatThread(MemberController mc) {
            this.mc = mc;
        }
        
        @Override
        public void run() {
            if (!member_socket.isClosed()) {
                String msg_client = nickname;
                send_client_msg(msg_client);
                String msg_server = (String) recv_server_msg();
                MemberController.setMsg_area(msg_server);
            }
            
            while (!member_socket.isClosed()) {
                Object o =  recv_server_msg();
                if (o instanceof String) {
                    String msg_server = (String) o;
                    if (msg_server.equals("List members!")) {
                        int room_id = Integer.parseInt((String) recv_server_msg());
                        msg_server = (String) recv_server_msg();
                        mv = new MembersView(this.mc, nickname, room_id, msg_server);
                        mv.setVisible(true);
                    }
                    else if (msg_server.equals("You are kicked!")) {
                        BeKicked bk = new BeKicked();
                        bk.start();
                    }
                    else if (msg_server.equals("Accept to start camera!")) {
                        int room_id = Integer.parseInt((String) recv_server_msg());
                        VideoSendView vsv = new VideoSendView(nickname, room_id);
                        vsv.setVisible(true);
                    }
                    else {
                        setMsg_area(msg_server);
                    }
                }
            }
        }
    }
    
    public void close() {
        WindowEvent close_window = new WindowEvent(mv, WindowEvent.WINDOW_CLOSING);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(close_window);
    }
    
    class BeKicked extends Thread{
        @Override
        public void run() {
            MemberController.setMsg_area("[SERVER]: You are kicked by admin. Window will be closed.");
            try {
                Thread.sleep(2000);
                member_socket.close();
            }
            catch(Exception e) {
                System.out.println(e);
            }
            close();
            System.exit(0);
        }
    }
}
