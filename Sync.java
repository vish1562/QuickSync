import java.io.*;
import java.net.*;
import java.util.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.sql.Timestamp;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class Sync implements Runnable {
    ListOfFiles files;
    ListOfPeers listOfPeers;
    int count1 = 0, count2 = 0;
    boolean firstTime = true;

    Sync(ListOfPeers listOfPeers) {
        this.listOfPeers = listOfPeers;
    }

    public void run() {
        boolean ret = false;
        ListOfFiles lof = listOfPeers.getSelf().getListOfFiles();
        ArrayList < String > arrayOfFiles = new ArrayList < String > ();
        PeerNode self = listOfPeers.getSelf();


         
        while (true) {

            PeerNode masterNode = listOfPeers.getMaster();
            lof.getList();
             
            if (masterNode != null) {
                if (lof.getArrayListOfFiles().size() != 0) {
                    if (firstTime) {
                        firstTime = false;
                         
                         
                        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

                        final TimeZone utc = TimeZone.getTimeZone("UTC");
                        dateFormatter.setTimeZone(utc);

                        String t = dateFormatter.format(new java.util.Date());
                        System.out.println("Init " + t);
                    }
                    JSONObject obj = JSONManager.getJSON(lof.getList());  
                    if (obj == null) {
                         
                        try {
                            Thread.sleep(30000);
                        } catch (Exception e) {}
                        continue;
                    }
                    if (masterNode.isCloud()) {
                        self.sendMessage(obj);
                         
                    } else {
                        Thread client = new Thread(new TcpClient(masterNode.getIPAddress(), "60010", obj, listOfPeers));
                        client.start();
                         
                    }
                }
            }
            count2 = lof.getArrayListOfFiles().size();
            if (count2 != count1) {
                java.util.Date date = new java.util.Date();
                Timestamp t = new Timestamp(date.getTime());
                System.out.println("\n Number of Files Received till " + t + " is: " + count2);
                count1 = count2;
            }

             
             

             


             

            HashMap < String, ArrayList < String >> fileToPeersMap = getFilesToRequestPerPeer(listOfPeers.getSelf().getHashMapFilePeer(), listOfPeers.getSelf().getListOfFiles().getArrayListOfFiles());
            HashMap < String, ArrayList < String >> peerToFilesMap = new HashMap < String, ArrayList < String >> ();
            Random rand = new Random();

            Set mappingSet = fileToPeersMap.entrySet();
            Iterator itr = mappingSet.iterator();
             
             
            while (itr.hasNext()) {
                Map.Entry < String, ArrayList < String >> entry = (Map.Entry < String, ArrayList < String >> ) itr.next();
                ArrayList < String > listofPeerHavingTheFile = entry.getValue();
                String randomPeerId = "";
                 
                int min_weight = 10000;
                for (int k = 0; k < listofPeerHavingTheFile.size(); k++) {
                    PeerNode peer = listOfPeers.getPeerNode(listofPeerHavingTheFile.get(k));
                    if (peer != null && !peer.isCloud() && peer.getWeight() < min_weight) {
                        randomPeerId = listofPeerHavingTheFile.get(k);
                        min_weight = peer.getWeight();
                    }
                }
                System.out.println("Getting from: " + randomPeerId);
                if (peerToFilesMap.containsKey(randomPeerId)) {
                    ArrayList < String > listOfFileForPeer = peerToFilesMap.get(randomPeerId);
                    listOfFileForPeer.add(entry.getKey());
                } else {
                    ArrayList < String > listOfFileForPeer = new ArrayList < String > ();
                    listOfFileForPeer.add(entry.getKey());
                    peerToFilesMap.put(randomPeerId, listOfFileForPeer);
                }
            }

            mappingSet = peerToFilesMap.entrySet();
             
            itr = mappingSet.iterator();

            while (itr.hasNext()) {
                Map.Entry < String, ArrayList < String >> entry = (Map.Entry < String, ArrayList < String >> ) itr.next();
                Collections.shuffle(entry.getValue());
                ret = seekFromPeer(entry.getValue(), entry.getKey(), masterNode == null ? listOfPeers.getSelf().isCloud() : masterNode.isCloud());  
            }

             



             
            if (listOfPeers.getMaster() == null)  
            {
                 

                if (listOfPeers.getList().size() != 0) {}
                 
                else {
                    System.out.println("Sync:run:Looks like I am the only one here!");
                    try {
                        Thread.sleep(3000);
                    } catch (Exception e) {}

                    continue;
                }

                listOfPeers.getSelf().setHashMapFilePeer(getFilesToRequestPerPeerMaster(listOfPeers));
                 
                 

                SortedSet < PeerNode > peerList = listOfPeers.getList();
                Iterator < PeerNode > it = peerList.iterator();

                while (it.hasNext()) {
                    PeerNode peerNode = it.next();
                    HashMap < String, ArrayList < String >> hmFilesPeers = getFilesToRequestPerPeer(listOfPeers.getSelf().getHashMapFilePeer(), peerNode.getListOfFiles().getArrayListOfFiles());

                     
                     

                     
                     
                    if (!hmFilesPeers.isEmpty()) {
                        JSONObject obj = JSONManager.getJSON(hmFilesPeers);  
                        Thread client = new Thread(new TcpClient(peerNode.getIPAddress(), "60010", obj, listOfPeers));
                        client.start();

                    }
                }

                 
            }

            try {
                Thread.sleep(3000);
            } catch (Exception e) {}
        }
    }

    void print(HashMap < String, ArrayList < String >> hmap) {
        Set mappingSet = hmap.entrySet();

        Iterator itr = mappingSet.iterator();

        while (itr.hasNext()) {
            Map.Entry < String, ArrayList < String >> entry = (Map.Entry < String, ArrayList < String >> ) itr.next();
             
        }
        System.out.println("Size of HashMap Sending is:" + hmap.size());
        System.out.println();
    }

    boolean seekFromPeer(ArrayList < String > fileName, String peerId, boolean isCloud) {
         
        if (fileName == null || peerId == null) {
             
            return false;
        }

        PeerNode peer = listOfPeers.getPeerNode(peerId);
        JSONObject obj = JSONManager.getJSON(fileName, 1);
        if (peer == null) {
            return false;
        }
        if (peer.isCloud() == true) {
            Iterator < String > itr = fileName.iterator();

            while (itr.hasNext()) {
                String str = itr.next();
                JSONObject obj1 = JSONManager.getJSON(str);
                listOfPeers.getSelf().sendMessage(obj1);
            }
             
        } else {
            Thread client = new Thread(new TcpClient(peer.getIPAddress(), "60010", fileName, true, listOfPeers));
            client.start();
        }

        return true;
    }


    HashMap < String, ArrayList < String >> getFilesToRequestPerPeerMaster(ListOfPeers peers) {
         

        SortedSet < PeerNode > peerList = peers.getList();
        PeerNode mySelf = peers.getSelf();
        HashMap < String, ArrayList < String >> hmFilesPeers = new HashMap < String, ArrayList < String >> ();
        Iterator < PeerNode > it = peers.peerList.iterator();

        while (it.hasNext()) {
            PeerNode peerNode = it.next();
            addToHashMap(hmFilesPeers, peerNode);
        }

        addToHashMap(hmFilesPeers, mySelf);

        return hmFilesPeers;
    }


    void addToHashMap(HashMap < String, ArrayList < String >> hmFilesPeers, PeerNode peerNode) {
        ArrayList < String > lof = peerNode.getListOfFiles().getArrayListOfFiles();

        if (lof == null) return;
        int i;
        for (i = 0; i < lof.size(); i++) {
            if (hmFilesPeers.containsKey(lof.get(i))) {
                hmFilesPeers.get(lof.get(i)).add(peerNode.getId());
            } else {
                ArrayList < String > newListOfPeers = new ArrayList < String > ();
                newListOfPeers.add(peerNode.getId());
                hmFilesPeers.put(lof.get(i), newListOfPeers);
            }
        }
    }

    HashMap < String, ArrayList < String >> getFilesToRequestPerPeer(HashMap < String, ArrayList < String >> hmFilesPeers, ArrayList < String > filesWithPeer) {
        if (filesWithPeer == null) {
            return hmFilesPeers;
        }


        int i;
        HashMap < String, ArrayList < String >> incrementalHashMap = new HashMap < String, ArrayList < String >> (hmFilesPeers);
        for (i = 0; i < filesWithPeer.size(); i++) {
            if (incrementalHashMap.containsKey(filesWithPeer.get(i))) {
                incrementalHashMap.remove(filesWithPeer.get(i));
            }
        }

        return incrementalHashMap;
    }

    void find(int x) {
        System.out.println("Sync:run:========Inside find" + x + "===========");
        Iterator < PeerNode > it = listOfPeers.getList().iterator();
        while (it.hasNext()) {
            PeerNode peerNode = it.next();
            ArrayList < String > lof = peerNode.getListOfFiles().getList();
            System.out.println("For peer node:" + peerNode.getId() + " list of files is:" + lof.toString());
        }
        System.out.println("Sync:run:========Leaving find()===========");
    }
     

}