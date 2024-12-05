import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class FrontEnd implements Auction{

    private static int primaryReplicaID = -1;
    private static List<Integer> replicaIDs = new ArrayList<>();
    
    //Each method in the Auction interface is implemented by connecting to the primary replica and remotely calling the method there
    @Override
    public int register(String email) throws RemoteException {
        return connectToPrimaryReplica().register(email);
        
    }

    @Override
    public AuctionItem getSpec(int itemID) throws RemoteException {
        return connectToPrimaryReplica().getSpec(itemID);
        
    }

    @Override
    public int newAuction(int userID, AuctionSaleItem item) throws RemoteException {
        return connectToPrimaryReplica().newAuction(userID, item);
        
    }

    @Override
    public AuctionItem[] listItems() throws RemoteException {
        return connectToPrimaryReplica().listItems();
        
    }

    @Override
    public AuctionResult closeAuction(int userID, int itemID) throws RemoteException {
        return connectToPrimaryReplica().closeAuction(userID, itemID);
        
    }

    @Override
    public boolean bid(int userID, int itemID, int price) throws RemoteException {
        return connectToPrimaryReplica().bid(userID, itemID, price);
        
    }

    @Override
    public int getPrimaryReplicaID() throws RemoteException {
        return primaryReplicaID;
        
    }

    //Helper function to connect to the primary replica using the getPrimaryReplicaID method
    public ReplicaInterface connectToPrimaryReplica(){
        while(primaryReplicaID != 1){
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
                updateLiveReplicas();
                choosePrimaryReplica();
            }
        }
        System.err.println("No replicas available");
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

    //function which checks the registry for available replicas and updates the list of replica IDs
    private static void updateLiveReplicas() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            String[] names = registry.list();
            
            replicaIDs.clear();
            for (String name : names) {
                if (name.startsWith("Replica")) { 
                    int replicaID = Integer.parseInt(name.replace("Replica", ""));
                    replicaIDs.add(replicaID);
                }
            }
            if (primaryReplicaID == -1 && !replicaIDs.isEmpty()) {
                primaryReplicaID = replicaIDs.get(0);
            }
        } catch (Exception e) {
            System.err.println("Error updating replica list: " + e.getMessage());
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
            updateLiveReplicas(); //check for replicas when the server starts
        }
        catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }
    
}
