import java.io.Serializable;

public class allAuctions implements Serializable {
    private int seller;
    private AuctionItem item;
    private int reservePrice; 
    private int buyer;

    /**
     * Used to retreive all information about a live auction being placed. 
     * @param seller The UserID of the user who set up the Auction
     * @param item All item information from AuctionItem Class
     * @param reservePrice Set by the Seller. This is the minimum price of the item. 
     * Buyer will be set when a user bids on the item. 
     */
    allAuctions(int seller, AuctionItem item, int reservePrice){
        this.seller = seller;
        this.item = item;
        this.reservePrice = reservePrice;
    }

    public int getBuyer() {
        return buyer;
    }
    public AuctionItem getItem() {
        return item;
    }
    public int getReservePrice() {
        return reservePrice;
    }
    public int getSeller() {
        return seller;
    }
    public void setBuyer(int buyer) {
        this.buyer = buyer;
    }
}
