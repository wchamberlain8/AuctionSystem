public class AuctionItem implements java.io.Serializable {
    int itemID;
    String name;
    String description;
    int highestBid;

    public AuctionItem(int itemID, String name, String description, int highestBid){
        this.itemID = itemID;
        this.name = name;
        this.description = description;
        this.highestBid = highestBid;
    }

    public int getItemID() {
        return itemID;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getHighestBid() {
        return highestBid;
    }

 }
    

    