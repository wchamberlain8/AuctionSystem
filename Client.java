import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client{
     public static void main(String[] args) {
      if (args.length < 1) {
        System.out.println("Usage: java Client n");
        return;
       }

      int n = Integer.parseInt(args[0]);

      try{
        String name = "Auction";
        Registry registry = LocateRegistry.getRegistry("localhost");
        Auction server = (Auction) registry.lookup(name);

        AuctionItem result = server.getSpec(n);
        System.out.println("Item ID: " + n);
        System.out.println("Name: " + result.getName());
        System.out.println("Description: " + result.getDescription());
        System.out.println("Highest Bid: " + result.getHighestBid());
      }
      
      catch(Exception e){
        System.out.println("Exeption: ");
        e.printStackTrace();
      }
    }
}