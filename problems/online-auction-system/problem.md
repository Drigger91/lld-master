# Online Auction System

| Difficulty | Patterns | Key concepts |
|---|---|---|
| Medium | Singleton, State (enum status), Observer (stubbed), Timer-driven closing | synchronized bidding, monotonic highest bid, timed state transition, keyword search |

## Problem statement
Design an online auction platform like eBay. Sellers list an item with a starting price and a duration; while the auction is active, registered users place bids. A bid is accepted only if it is strictly higher than the current highest bid. When the duration elapses the auction closes automatically and the highest bidder wins; no further bids are accepted. Users can search listings by keyword.

## Functional requirements
1. Register users (id, username, email).
2. Create an auction listing: id, item name, description, starting price, duration (ms), seller. It starts `ACTIVE` with the starting price as the bar to beat.
3. Search listings whose item name or description contains a keyword.
4. Place a bid on a listing: accepted only if the listing exists, is `ACTIVE`, and the amount is strictly greater than the current highest bid (initially the starting price). Accepted bids update the highest bid/bidder and are appended to the bid history.
5. Close the auction automatically when its duration elapses; closing is idempotent.
6. Expose status, current highest bid, highest bidder and the list of accepted bids.
7. Notify interested parties on new highest bid and on close (hook present, not implemented).

## Non-functional requirements & constraints
- In-memory; `AuctionSystem` is a singleton with `ConcurrentHashMap` registries.
- `placeBid` and `closeAuction` are `synchronized` on the listing, so concurrent bids on one item are serialised and a bid can never be accepted after close.
- Bid history is a `CopyOnWriteArrayList` (safe to iterate while bids arrive).
- Closing uses one daemon `java.util.Timer` per listing that cancels itself after firing.
- Rejected bids are not stored; prices are `double`.

## Clarifying questions to ask
- Must a bid beat the current highest by a minimum increment? — No, strictly greater is enough.
- Can the seller bid on their own item? — Not prevented.
- What happens to a bid equal to the current highest? — Rejected.
- Can an auction be closed manually or extended (anti-sniping)? — Only the timer closes it; no extension.
- Is there a reserve price or buy-now? — No.
- Are bidders notified? — `notifyObservers` is a placeholder for an Observer implementation.

## Core entities
- `AuctionSystem` — singleton: users, listings, `createAuctionListing` (starts the timer), `searchAuctionListings`, `placeBid`, `getAuctionListing`.
- `AuctionListing` — item data, seller, `AuctionStatus`, current highest bid/bidder, bid history; synchronized `placeBid` / `closeAuction`.
- `Bid` — id, bidder, amount, timestamp.
- `User` — id, username, email.
- `AuctionStatus` — `ACTIVE`, `CLOSED`.

## Design hints
- **Invariant lives in the listing.** "Highest bid only ever increases and only while ACTIVE" is enforced inside one `synchronized` method; the system-level `placeBid` just routes. This is what an interviewer wants to see guarded, then asked about under concurrency.
- **State via enum + guard clauses.** Two states do not need a full State pattern; the check in `placeBid`/`closeAuction` is enough. Say when you would upgrade (e.g. `SCHEDULED`, `PAUSED`, `SETTLED`).
- **Timer per listing vs one scheduler.** A `Timer` per auction is simple but creates a thread each; a shared `ScheduledExecutorService` scales better. Either way the timer thread must be a daemon or be cancelled, or the JVM never exits.
- **Observer hook.** `notifyObservers()` is where outbid notifications and close events go; bidders subscribe to the listing.
- Common mistakes: `>=` instead of `>` when comparing bids; checking status outside the lock; using `double` for money; never cancelling timers; storing rejected bids.

## Run
mvn -q -pl problems/online-auction-system compile exec:java
