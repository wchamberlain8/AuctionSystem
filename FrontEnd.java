import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class FrontEnd implements Auction{

    private static int MAX_NUMBER_OF_REPLICAS = 10; //change depending on how many possible replicas are needed
    private static int primaryReplicaID = -1;
    private static List<Integer> replicaIDs = new ArrayList<>();
    
    //Each method in the Auction interface is implemented by connecting to the primary replica and remotely calling the method there
    @Override
    public int register(String email) throws RemoteException {
        lookForReplicas();
        return connectToPrimaryReplica().register(email);
        
    }

    @Override
    public AuctionItem getSpec(int itemID) throws RemoteException {
        lookForReplicas();
        return connectToPrimaryReplica().getSpec(itemID);
        
    }

    @Override
    public int newAuction(int userID, AuctionSaleItem item) throws RemoteException {
        lookForReplicas();
        return connectToPrimaryReplica().newAuction(userID, item);
        
    }

    @Override
    public AuctionItem[] listItems() throws RemoteException {
        lookForReplicas();
        return connectToPrimaryReplica().listItems();
        
    }

    @Override
    public AuctionResult closeAuction(int userID, int itemID) throws RemoteException {
        lookForReplicas();
        return connectToPrimaryReplica().closeAuction(userID, itemID);
        
    }

    @Override
    public boolean bid(int userID, int itemID, int price) throws RemoteException {
        lookForReplicas();
        return connectToPrimaryReplica().bid(userID, itemID, price);
        
    }

    @Override
    public int getPrimaryReplicaID() throws RemoteException {
        return primaryReplicaID;
        
    }

    //Helper function to connect to the primary replica using the getPrimaryReplicaID method
    public ReplicaInterface connectToPrimaryReplica(){
        
        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            
            int primaryReplicaID = getPrimaryReplicaID();
            ReplicaInterface primary = (ReplicaInterface) registry.lookup("Replica" + primaryReplicaID); //connect to primary replica
            primary.setPrimaryReplicaID(primaryReplicaID);
            primary.updateReplicaIDList(replicaIDs); //send an updated list of replica IDs to the primary replica

            return primary;
        } 
        catch (Exception e) {
            System.err.println("Problem connecting to the primary replica");
        }
        return null;
    }

    //Helper function to choose a new primary replica if the current one is killed
    private static void choosePrimaryReplica(){
        //if there are replicas available, choose the first one in the list
        if(!replicaIDs.isEmpty()){
            primaryReplicaID = replicaIDs.get(0);
            //System.out.println("NEW PRIMARY REPLICA CHOSEN = Replica" + primaryReplicaID);
        }
        else{
            primaryReplicaID = -1; //if there are no replicas available, set primaryReplicaID to -1
        }
    }
    
    //function to check which replicas are live and update the list of replica IDs
    private static void lookForReplicas(){
            
        for(int i =1; i <= MAX_NUMBER_OF_REPLICAS; i++){ //try to connect to MAX_NUMBER_OF_REPLICAS replica
                
            String name = "Replica" + i;
            //System.out.println("ATTEMPTING LOOKUP FOR " + name);
    
            try {
                Registry registry = LocateRegistry.getRegistry("localhost");
                ReplicaInterface replica = (ReplicaInterface) registry.lookup(name);
                
                if(!replicaIDs.contains(i)){ //if the replica is live and not already in the list, add it
                    replicaIDs.add(i);
                    //System.out.println(name + " is live and added to the list");
                }

                if(primaryReplicaID == -1){ //if there is no primary replica, make it the first connected 
                    primaryReplicaID = i;
                    //System.out.println(name + " has been set as primary replica");
                }
                
                if(replicaIDs.contains(i)){ //if the replica is live and already in the list, do nothing (debugging)
                    //System.out.println(name + " is live and already is in the list");
                }

            }
            catch (Exception e) {

                if(replicaIDs.contains(i)){ //if the replica is not live and is in the list, remove it as it has been killed
                    replicaIDs.remove(Integer.valueOf(i));
                    //System.out.println(name + " has been removed from the list of replicas");
                }

                if(primaryReplicaID == i){ //if the primary replica has been killed, choose a new one
                    //System.out.println("Primary replica has been killed, choosing a new one...");
                    choosePrimaryReplica();
                }
            }

        }

    }

    public static void main(String[] args) {
        try {
            FrontEnd frontEndServer = new FrontEnd(); // creating a server instance
            String name = "FrontEnd";

            Auction stub = (Auction) UnicastRemoteObject.exportObject(frontEndServer, 0);

            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);

            System.out.println("FrontEnd server ready");
            lookForReplicas(); //check for replicas when the server starts
        }
        catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }
    
}
