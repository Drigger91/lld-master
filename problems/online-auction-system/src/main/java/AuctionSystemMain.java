import java.util.List;

public class AuctionSystemMain {
    public static void main(String[] args) throws InterruptedException {
        AuctionSystem auctionSystem = AuctionSystem.getInstance();

        // Register users
        User user1 = new User("1", "John Doe", "john@example.com");
        User user2 = new User("2", "Jane Smith", "jane@example.com");
        User user3 = new User("3", "Bob Lee", "bob@example.com");
        auctionSystem.registerUser(user1);
        auctionSystem.registerUser(user2);
        auctionSystem.registerUser(user3);

        // Create auction listings (durations in ms; short so the demo can watch them close)
        AuctionListing listing1 = new AuctionListing("1", "Vintage Camera", "1970s film camera", 100.0, 1500, user1);
        AuctionListing listing2 = new AuctionListing("2", "Item 2", "Description 2", 50.0, 3000, user2);
        auctionSystem.createAuctionListing(listing1);
        auctionSystem.createAuctionListing(listing2);

        // Search auction listings
        List<AuctionListing> searchResults = auctionSystem.searchAuctionListings("Camera");
        System.out.println("Search results for 'Camera':");
        for (AuctionListing listing : searchResults) {
            System.out.println("  " + listing.getItemName() + " starting at " + listing.getStartingPrice());
        }

        // Place bids
        System.out.println("Jane bids 150: " + auctionSystem.placeBid(listing1.getId(), new Bid("b1", user2, 150.0)));
        System.out.println("Bob bids 120 (below current highest): " + auctionSystem.placeBid(listing1.getId(), new Bid("b2", user3, 120.0)));
        System.out.println("Bob bids 150 (equal, not higher): " + auctionSystem.placeBid(listing1.getId(), new Bid("b3", user3, 150.0)));
        System.out.println("Bob bids 200: " + auctionSystem.placeBid(listing1.getId(), new Bid("b4", user3, 200.0)));
        System.out.println("Bid on unknown listing: " + auctionSystem.placeBid("999", new Bid("b5", user3, 500.0)));
        System.out.println("Highest so far: " + listing1.getCurrentHighestBid() + " by " + listing1.getCurrentHighestBidder().getUsername()
                + ", status " + listing1.getStatus() + ", accepted bids " + listing1.getBids().size());

        // Wait for the timer to close listing1
        System.out.println("Waiting for auction 1 to end...");
        Thread.sleep(2000);
        System.out.println("Status: " + listing1.getStatus());
        System.out.println("Jane bids 300 after close: " + auctionSystem.placeBid(listing1.getId(), new Bid("b6", user2, 300.0)));
        System.out.println("Winner: " + listing1.getCurrentHighestBidder().getUsername() + " at " + listing1.getCurrentHighestBid());
        System.out.println("Listing 2 still " + listing2.getStatus() + " with no bids, highest bidder: " + listing2.getCurrentHighestBidder());
    }
}
