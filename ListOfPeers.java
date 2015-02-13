import java.io.*;
import java.util.*;

class ListOfPeers
{
	SortedSet<PeerNode> peerList = new TreeSet<PeerNode>(new Comp());
    
	PeerNode mySelf;
     
	ListOfPeers(PeerNode mySelf)
	{
          //Insert to Peer List cloud domain id
	    this.mySelf = mySelf;
	}

    PeerNode getSelf()
	{
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
       return true;
   }

   boolean removePeerNode(PeerNode removeNode)
   {
	  	if(!present(removeNode))
			return false;

		peerList.remove(removeNode);
    return true;
   }

   PeerNode getMaster()
   {
	   return peerList.size()==0? null :peerList.first();
   }

   boolean present(PeerNode node)
   {
      Iterator<PeerNode> itr = peerList.iterator();

	  while(itr.hasNext())
	  {
            if(itr.next().getId()==node.getId())
				return true;

	  }

	  return false;
   }

   PeerNode getPeerNode(String peerId){
       Iterator<PeerNode> itr = peerList.iterator();
       PeerNode node;

       while(itr.hasNext()){
           node = itr.next();
           if(node.getId().equals(peerId)){
               return node;
           }
       }

       return null;
   }

   SortedSet<PeerNode> getList()
   {
	    return peerList;
   }   
}
