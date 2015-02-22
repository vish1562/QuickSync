import java.net.*;
import java.io.*;
import java.lang.*;
import java.security.*;
import java.util.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class UdpClient implements Runnable
{
    private DatagramSocket clientSocket;
    private String broadcastAdd;
    private int port;
    private String selfIp;
    private ListOfPeers peerList;
    private ArrayList<String> client;
    
    UdpClient(int port, String broadcastAdd, ArrayList<String> client, ListOfPeers peerList){
        
        System.out.println("Starting UDP client on port" + port);
        try{
            this.clientSocket = new DatagramSocket();
            this.clientSocket.setBroadcast(true);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        this.broadcastAdd = broadcastAdd;
        this.port = port;
        this.client = client;
        this.peerList = peerList;
    }
    
    void sendUdpPacket(byte[] data, String remoteIp){
        try{
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(remoteIp), 61001);
            this.clientSocket.send(packet);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    //void broadcastUdpPacket(byte[] data, String ip){
    void broadcastUdpPacket(byte[] data){
        /*
        try{
        Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
        while(interfaces.hasMoreElements()){
        NetworkInterface networkInterface = (NetworkInterface)interfaces.nextElement();
        if (networkInterface.isLoopback() || !networkInterface.isUp()) {
        continue; // Don't want to broadcast to the loopback interface
        }
        for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
        InetAddress broadcast = interfaceAddress.getBroadcast();
        System.out.println(broadcast);
        if (broadcast == null) {
        continue;
        }
        try{
        DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(broadcastAdd), 61001);
        //DatagramPacket packet = new DatagramPacket(data, data.length, broadcast, 61001);
        this.clientSocket.send(packet);
        }catch(Exception e){
        e.printStackTrace();
        }
        //System.out.print("-----Broadcasting to "+ broadcastAdd);
        }
        }
        }catch(Exception e){
        }
        */
        try{
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(broadcastAdd), 61001);
            //DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(ip), 61001);
            //DatagramPacket packet = new DatagramPacket(data, data.length, broadcast, 61001);
            this.clientSocket.send(packet);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void run(){
        /* Start a udp server */
        int i ;
        byte[] buf = new byte[100];
        
        try{
            //String ipPort = this.clientSocket.getLocalAddress()+":"+this.port;
            /*
            StringBuilder a = new StringBuilder();
            a.append("blah blah");
            a.append(String.valueOf(port));
            String ipPort = "blah blah";
            byte[] bytes = ipPort.getBytes("UTF-8");
            System.out.println("Created stream");
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(bytes);
            */
            
            //PeerNode host = new PeerNode(peerList.getSelf().getId(), peerList.getSelf().getWeight());
            String data = peerList.getSelf().getId() + ":" + String.valueOf(peerList.getSelf().getWeight());
            
            JSONObject JSONobj = JSONManager.getJSON(data);
            data = JSONobj.toString();
            System.out.println("JSON++++++++++++ "+ data);
            
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            ObjectOutputStream o = new ObjectOutputStream(b);
            o.writeObject(data);
            buf = b.toByteArray();
            //buf = data.getBytes();
            System.out.println("Created data");
            System.out.println("----- "+ InetAddress.getByName(broadcastAdd) + broadcastAdd);
        }catch(Exception e){
            e.printStackTrace();
        }
        
        
        /* Send Broadcast info */
        while(true){
            //for(i = 1; i <= 255; i++){
            if(client.isEmpty() == true){
                broadcastUdpPacket(buf);
                System.out.println("Broadcasting .....");
            }else{
                Iterator itr = client.iterator();
                while(itr.hasNext()){
                    sendUdpPacket(buf, (String)itr.next());
                }
                System.out.println("Unicasting .....");
            }

            try {
                Thread.sleep(1000); //milliseconds
            } catch (Exception e){
                e.printStackTrace();
            }
            
            
        }
    }
}
