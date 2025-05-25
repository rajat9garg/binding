Project Overview: Online Bidding Platform
This project involves the design and development of a scalable, real-time online bidding platform where registered users can participate in live auctions by placing bids on various items. The system ensures fair competition, real-time updates, and transparent auction outcomes.

Functional Requirements
User Registration
Users can register using their phone number and name. [No Authentication is required of any kind]
Basic validation must be in place to prevent duplicate registrations dedupe on phone number.

✅ 2. Item & Auction Listing Module
2.1. View Auctions

Endpoint: GET /auctions
Input: Optional filters: status=[ONGOING|UPCOMING]

Output:
itemName, description, auctionStartTime, auctionEndTime, currentHighestBid

Real-Time Requirements:
Integrate with WebSockets for real-time updates on currentHighestBid.

2.2. Fetch Auction Item Details
Endpoint: GET /auctions/{itemId}
Output:
Same as above but specific to the item.

Technical Notes:
Cache item metadata with TTL for quick retrieval.
Use CDN or in-memory cache for auction metadata.


✅ 3. Bidding Engine Module
3.1. Place Bid

Endpoint: POST /bids

Input: itemId, userId, bidAmount

Validation:

Ensure auction is ONGOING.

Ensure bidAmount > currentHighestBid.

Technical Implementation:

Use atomic compare-and-set (CAS) or pessimistic locking (e.g., row-level lock or Redis SETNX) to ensure bid consistency.

Ensure idempotency for retry-safe writes.

3.2. Real-Time Bid Broadcast

Mechanism: WebSocket or SSE push

Trigger: On successful new highest bid

Technical Requirements:

Broadcast updated currentHighestBid, userId to all connected clients.

Use pub/sub mechanism (Redis Pub/Sub, Kafka, etc.) behind WebSocket gateway for fanout.

✅ 4. Auction Lifecycle Management
4.1. Start Auction

Trigger: System cron/scheduler OR real-time scheduler

Action:

Mark auction as ONGOING.

Notify all clients subscribed to auction list.

4.2. End Auction

Trigger: Auto-triggered via background job at auctionEndTime

Action:

Mark auction as ENDED.

Determine winner = highest bidder.

Broadcast winner announcement.

Technical Notes:

Ensure transactional consistency when declaring winner and closing bidding.

✅ 5. Concurrency & Scalability
Concurrency Control

Strategy:

Use optimistic locking (version-based row update) OR

Redis-based distributed lock per item for bid writes.

Avoid DB-level bottlenecks.

Testing Plan:

Simulate 100,000 concurrent users with bid spikes.

Scalability Plan

Stateless API design using REST/WebSockets.

Horizontal scaling behind load balancer.

Redis/Kafka for decoupling real-time systems.

DB sharding or partitioning for auction and bid tables.

✅ 6. Monitoring and Observability
Metrics to Track:

Active auctions

Total bids per second

Real-time connection count (WebSocket/SSE)

Alerts:

Auction processing delays

Bid update failures or lock contention

Logging:

Use structured logs with correlation IDs.