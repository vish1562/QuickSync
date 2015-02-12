import java.io.*;
import java.util.*;

class ListOfPeers{
{
	public SortedSet<PeerNode> peerList = new TreeSet<PeerNode>(new Comp());
    
	PeerNode mySelf;
     
	ListOfPeers(PeerNode mySelf)
	{
          //Insert to Peer List cloud domain id
	    this.mySelf = mySelf;
	}

        PeerNode getSelf(){
            return mySelf;
        }
       
   class Comp implements Comparator<PeerNode>
   {

		@Override
		public int  compare(PeerNode pn1,PeerNode pn2) 
		{
			if( pn1.getWeight() > pn2.getWeight())
				return 1;
			return -1 ;
		}
   }	

   boolean addPeerNode(PeerNode newNode)
   {
	     if(present(newNode))
		   return false;
	  
	     peerList.add(newNode);	 
   }

   boolean removePeerNode(PeerNode removeNode)
   {
	  	if(!present(removeNode))
			return false;

		peerList.remove(removeNode);
   }

   PeerNode getMaster()
   {
	   return peerList.first();
   }

   boolean present(PeerNode node)
   {
      Iterator itr = peerList.iterator();

	  while(itr.hasNext())
	  {
            if(itr.next().getId()==node.getId())
				return true;

	  }

	  return false;
   }

   PeerNode getPeerNode(String peerId){
       Iterator itr = peerList.iterator();
       PeerNode node;

       while(itr.hasNext()){
           node = itr.next();
           if(node.getId().equals(peerId)){
               return node;
           }
       }

       return null;
   }

}