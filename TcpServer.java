import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.util.*;
import java.sql.Timestamp;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Calendar;
import java.text.DateFormat;
public class TcpServer implements Runnable
{
    private ServerSocket ss;
    private Socket s;
    ListOfPeers peerList;
    static String homeDir = System.getProperty("user.home");
    static String folder = "QuickSync";
    static String path = homeDir + "/" + folder ;
    boolean isFileSocket =false;
    PeerNode peerNode;//Communicating with this node
    String peerId;
	public TcpServer(ServerSocket ss, Socket s, ListOfPeers peerList)
    {
        this.ss = ss;
        this.s = s;
        this.peerList = peerList;
        peerNode = peerList.getPeerNodeFromIP(s.getInetAddress().getHostAddress());
    	peerId=peerNode.getId();
	}

    @Override
    public void run()
    {
        int count =0;
        InputStream inFromServer = null;
        ObjectInputStream in = null;
        System.out.println("TcpServer:run: Server running "+s.toString());
        try {
            inFromServer = s.getInputStream();
            in = new ObjectInputStream(inFromServer);
            while(!s.isClosed()){
                JSONObject obj = getMessage(s, in);
			   
                //Check for NULL Object
               	if(obj==null)
                {
                    System.out.println("obj null!!!!!!!!!!!!!!!!");
                }
                else if(obj.get("type").equals("Control"))
                {
                    System.out.println("TcpServer:run: Got an Control Message from:"+s.getInetAddress().toString());
                    String str = (String)obj.get("value");
                    //Send the file from ...
                    File file= new File(path+"/"+str);
                    JSONObject obj2 = JSONManager.getJSON(file);
                    Thread client = new Thread(new TcpClient(s.getInetAddress().getHostAddress(), "60010", obj2, peerList));
                    client.start();
                    break;
                }
                else if(obj.get("type").toString().substring(0,4).equals("File"))
                {
                    isFileSocket=true;
                    String fileContent = (String)obj.get("value");
                    //Store this File...
                    String receivedPath = obj.get("type").toString().substring(4);
                    System.out.println("TcpServer:run: Got an File " + receivedPath + " from:"+s.getInetAddress().toString());
                    /* Remove the file from in-transit hashset */
                    if(!peerList.removeFileInTransit(receivedPath,peerNode.getId())){
                        System.out.println("Error!!! File not found in hash set. Something is wrong");
                    }

                    String[] splits = receivedPath.split("/");
                    int noOfSplits = splits.length;
                    String newPath = path;

                    while(noOfSplits > 1){
                        newPath = newPath + "/" + splits[splits.length - noOfSplits];
                        File theDir = new File(newPath);
                        if(!theDir.exists()){
                            theDir.mkdir();
                        }
                        noOfSplits--;
                    }
                    
                    File file = new File(path+"/"+ receivedPath);
                    file.createNewFile();
                    FileOutputStream fos = new FileOutputStream(file);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    bos.write(fileContent.getBytes());
                    bos.close();
                    //java.util.Date date= new java.util.Date();
                    //Timestamp t = new Timestamp(date.getTime());
			DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss z");
			    final TimeZone utc = TimeZone.getTimeZone("UTC");
			    dateFormat.setTimeZone(utc);
		 Calendar cal = Calendar.getInstance();
		 Date dat = cal.getTime();
		 cal.add(Calendar.SECOND, peerList.getOffset());
         	 String t = dateFormat.format(cal);
                    //SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSSZ");
         
         
                    //String t = dateFormatter.format(new java.util.Date());
                    System.out.println("_"+peerNode.getId()+ "_" + t + "_" + receivedPath);
                }
                else if(obj.get("type").equals("ArrayList"))
                {
                    System.out.println("TcpServer:run: Got an ArrayList from:"+s.getInetAddress().toString());
                    ArrayList list = (ArrayList)obj.get("value");
                    //Uodate the peerList peerNode list of files
                    
                    if(peerNode ==null)
                    {
                        //System.out.println("TcpServer:run: \nCouldn't find the PeerNode");
                    }
                    else
                    {
                        ListOfFiles lof= new ListOfFiles(list);
                        peerNode.setListOfFiles(lof);
                    }
                    break;
                }
                else if(obj.get("type").equals("HashMap"))
                {
                    System.out.println("TcpServer:run: Got an HashMap from:"+s.getInetAddress().toString());
                    HashMap map = (HashMap)obj.get("value");
                    peerList.getSelf().setHashMapFilePeer(map);
                    break;
                }
                else if(obj.get("type").equals("ArrayListFiles"))
                {
                    System.out.println("TcpServer:run: Got an ArrayListFile from:"+s.getInetAddress().toString());
                    ArrayList<String> fileArray = (ArrayList<String>)obj.get("value");
                    //Store this File...
                    Thread client = new Thread(new TcpClient(s.getInetAddress().getHostAddress(), "60010", fileArray, false, peerList));
                    client.start();
                    break;
                }
                else if(obj.get("type").equals("EOFFileList")){
                    System.out.println("File EOF received from: " + s.getInetAddress().toString());
                    break;
                }else
                {
                    break;
                    //System.out.println("TcpServer:run: Got an Invalid Message from:"+s.getInetAddress().toString());
                }
            }
            System.out.println("Outside while****************");
            //CLOSE SOCKET HERE
            s.close();
        }catch(StreamCorruptedException ee){
                System.out.println("TcpServer:run: !!!!!!!!!!*********************** "+s.toString());
                ee.printStackTrace();
        }catch (Exception e) {
            try{
                System.out.println("TcpServer:run: !!!!!!!!!!!!!!!!!!!!!!!!!!!!! "+s.toString());
                s.close();
                e.printStackTrace();
            }
            catch(Exception ee)
            {
            }
        }
        finally
        {
            if(isFileSocket)
              peerList.syncMap("",peerId,"clearForPeer");
        }

            //System.out.println();        
    }
    
    JSONObject getMessage(Socket s, ObjectInputStream in)
    {
        
        JSONObject obj = null;
        try
        {
            System.out.println("Server socket " + s + "**** "+ s.isClosed()+" ---- Available " + in.available());
            Message obj2 = (Message)in.readObject();
            obj = (JSONObject)(obj2.obj);
        }
        catch(Exception e)
        {
            System.out.println("Dude Dude****************");
            e.printStackTrace();
            try{
                s.close();
            }catch(Exception ee){
            }
        }
        return obj;
    }
    
    
    void find(int x)
    {
        System.out.println("========Inside find" + x + "===========");
        Iterator<PeerNode> it = peerList.getList().iterator();
        while (it.hasNext())
        {
            PeerNode peerNode = it.next();
            ArrayList<String> lof = peerNode.getListOfFiles().getList();
            System.out.println("For peer node:"+peerNode.getId()+" list of files is:"+lof.toString());
        }
        System.out.println("========Leaving find()===========");
    }
}
