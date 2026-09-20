# Test cases — Online Auction System

Legend: ✅ exercised by the demo `Main` · ⬜ described but not exercised

## Happy path
| # | Scenario | Steps | Expected |
|---|---|---|---|
| H1 ✅ | Register users | `auctionSystem.registerUser(new User("1", "John Doe", email))` | `auctionSystem.getUser("1")` returns the user |
| H2 ✅ | Create listing | `auctionSystem.createAuctionListing(new AuctionListing("1", "Vintage Camera", desc, 100.0, 1500, seller))` | Status `ACTIVE`; `getCurrentHighestBid()` is 100.0; highest bidder null; timer started |
| H3 ✅ | Search by keyword | `auctionSystem.searchAuctionListings("Camera")` | Returns only listings whose name or description contains "Camera" |
| H4 ✅ | First valid bid | `auctionSystem.placeBid("1", new Bid("b1", jane, 150.0))` | Returns true; highest bid 150.0 by Jane; bids size 1 |
| H5 ✅ | Higher bid replaces | `placeBid("1", new Bid("b4", bob, 200.0))` | Returns true; highest 200.0 by Bob; bids size 2 |
| H6 ✅ | Auto-close after duration | Wait past 1500 ms; `listing1.getStatus()` | `CLOSED`; winner is highest bidder (Bob at 200.0) |
| H7 ✅ | Unrelated listing unaffected | `listing2.getStatus()` after listing1 closes | Still `ACTIVE`, highest bidder null |

## Edge cases
| # | Scenario | Steps | Expected |
|---|---|---|---|
| E1 ✅ | Bid equal to current highest | Highest 150; `placeBid("1", new Bid("b3", bob, 150.0))` | Returns false; nothing changes |
| E2 ⬜ | Auction with no bids closes | listing2 closes with no bids | `CLOSED`, highest bid == starting price, bidder null (no winner) |
| E3 ⬜ | Bid exactly at starting price | Fresh listing at 100; bid 100 | Rejected (must be strictly greater) |
| E4 ⬜ | Same user outbids themselves | Bob bids 200 then 210 | Both accepted; bids size 2 |
| E5 ⬜ | Closing twice | `listing.closeAuction()` twice | Idempotent; status stays `CLOSED`, observers notified once |
| E6 ⬜ | Search is case-sensitive | `searchAuctionListings("camera")` | No match for "Vintage Camera" (documented behaviour) |

## Invalid input & error handling
| # | Scenario | Steps | Expected |
|---|---|---|---|
| X1 ✅ | Bid below current highest | Highest 150; `placeBid("1", new Bid("b2", bob, 120.0))` | Returns false |
| X2 ✅ | Bid on unknown listing | `placeBid("999", bid)` | Returns false, no exception |
| X3 ✅ | Bid after close | After H6, `placeBid("1", new Bid("b6", jane, 300.0))` | Returns false; winner unchanged |
| X4 ⬜ | Negative or zero starting price / bid | `new Bid("x", user, -5)` | Not validated today; should be rejected |
| X5 ⬜ | Seller bids on own item | Seller places a bid | Not prevented today; design decision |

## Concurrency
| # | Scenario | Steps | Expected |
|---|---|---|---|
| C1 ⬜ | Many concurrent bids | N threads bid random amounts on one listing | Highest bid is the max of accepted bids; bid history is strictly increasing (listing `placeBid` is synchronized) |
| C2 ⬜ | Bid racing the close | Timer closes while a bid is in flight | Bid is either accepted before the status flips or rejected after; never accepted on a `CLOSED` listing |
| C3 ⬜ | Iterating bids while bidding | Read `getBids()` in one thread, bid in another | No `ConcurrentModificationException` (`CopyOnWriteArrayList`) |

## Interviewer follow-ups / extensions
- How would you notify outbid users? — Implement `notifyObservers`: bidders register as observers on the listing; publish "outbid" and "closed" events.
- How would you replace one `Timer` per listing? — A shared `ScheduledExecutorService` or a delay queue; store the scheduled future to allow cancel/extend.
- How would you add anti-sniping? — On a bid in the last N seconds, reschedule the close (`extend`) under the listing lock.
- How would you add reserve price and buy-now? — Extra fields checked in `placeBid`; buy-now closes immediately with that bidder.
- Money? — Switch bid amounts to `BigDecimal` or minor units.
- Persistence and recovery? — Persist listings with an `endsAt` timestamp; on restart, reschedule timers for still-active listings.
