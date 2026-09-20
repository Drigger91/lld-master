import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionSystem {
    private static AuctionSystem instance;
    private final Map<String, User> users;
    private final Map<String, AuctionListing> auctionListings;

    private AuctionSystem() {
        users = new ConcurrentHashMap<>();
        auctionListings = new ConcurrentHashMap<>();
    }

    public static synchronized AuctionSystem getInstance() {
        if (instance == null) {
            instance = new AuctionSystem();
        }
        return instance;
    }

    public void registerUser(User user) {
        users.put(user.getId(), user);
    }

    public User getUser(String userId) {
        return users.get(userId);
    }

    public void createAuctionListing(AuctionListing auctionListing) {
        auctionListings.put(auctionListing.getId(), auctionListing);
        startAuctionTimer(auctionListing);
    }

    public List<AuctionListing> searchAuctionListings(String keyword) {
        List<AuctionListing> matchingListings = new ArrayList<>();
        for (AuctionListing auctionListing : auctionListings.values()) {
            if (auctionListing.getItemName().contains(keyword) || auctionListing.getDescription().contains(keyword)) {
                matchingListings.add(auctionListing);
            }
        }
        return matchingListings;
    }

    public AuctionListing getAuctionListing(String auctionListingId) {
        return auctionListings.get(auctionListingId);
    }

    /** @return true if the bid was accepted (listing exists, is ACTIVE and bid beats the current highest). */
    public boolean placeBid(String auctionListingId, Bid bid) {
        AuctionListing auctionListing = auctionListings.get(auctionListingId);
        if (auctionListing == null) {
            return false;
        }
        return auctionListing.placeBid(bid);
    }

    private void startAuctionTimer(AuctionListing auctionListing) {
        Timer timer = new Timer("auction-" + auctionListing.getId(), true); // daemon: does not keep the JVM alive
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                auctionListing.closeAuction();
                timer.cancel();
            }
        }, auctionListing.getDuration());
    }
}
