import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuctionListing {
    private final String id;
    private final String itemName;
    private final String description;
    private final double startingPrice;
    private final long duration;
    private final User seller;
    private AuctionStatus status;
    private double currentHighestBid;
    private User currentHighestBidder;
    private final List<Bid> bids;

    public AuctionListing(String id, String itemName, String description, double startingPrice, long duration, User seller) {
        this.id = id;
        this.itemName = itemName;
        this.description = description;
        this.startingPrice = startingPrice;
        this.duration = duration;
        this.seller = seller;
        this.status = AuctionStatus.ACTIVE;
        this.currentHighestBid = startingPrice;
        this.currentHighestBidder = null;
        this.bids = new CopyOnWriteArrayList<>();
    }

    /** @return true if accepted: auction still ACTIVE and amount strictly above the current highest bid. */
    public synchronized boolean placeBid(Bid bid) {
        if (status == AuctionStatus.ACTIVE && bid.getAmount() > currentHighestBid) {
            currentHighestBid = bid.getAmount();
            currentHighestBidder = bid.getBidder();
            bids.add(bid);
            notifyObservers();
            return true;
        }
        return false;
    }

    public synchronized void closeAuction() {
        if (status == AuctionStatus.ACTIVE) {
            status = AuctionStatus.CLOSED;
            notifyObservers();
        }
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public String getItemName() {
        return itemName;
    }

    public String getDescription() {
        return description;
    }

    public long getDuration() {
        return duration;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public User getSeller() {
        return seller;
    }

    public synchronized AuctionStatus getStatus() {
        return status;
    }

    public synchronized double getCurrentHighestBid() {
        return currentHighestBid;
    }

    public synchronized User getCurrentHighestBidder() {
        return currentHighestBidder;
    }

    public List<Bid> getBids() {
        return bids;
    }

    private void notifyObservers() {
        // Notify observers (bidders) about the updated highest bid or auction closure
        // ...
    }
}
