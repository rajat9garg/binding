Project Overview: Online Bidding Platform
This project involves the design and development of a scalable, real-time online bidding platform where registered users can participate in live auctions by placing bids on various items. The system ensures fair competition, real-time updates, and transparent auction outcomes.

Functional Requirements
User Registration

Users can register using their phone number and name. [No Authentication is required of any kind]
Basic validation must be in place to prevent duplicate registrations dedupe on phone number.

Item Browsing
Registered users can view a list of items available in ongoing and upcoming auctions.
Each item should display relevant details: item name, description, auction start and end time, and current highest bid.

Bidding Mechanism
Users can place bids on items within the auction time window.
Each bid must be higher than the current maximum bid.
The system should ensure atomicity and consistency while updating bid values (to avoid race conditions).


Non-Functional Requirements
Scalability
The platform must be able to handle at least 100,000 concurrent users without degradation in performance.

Concurrency Handling
The system must support simultaneous bidding operations, ensuring data integrity with proper synchronization or optimistic locking mechanisms.
Real-Time Bid Updates
The current highest bid on any item should be visible to all users in real-time, preferably using WebSockets or Server-Sent Events (SSE).
Automated Auction Lifecycle
The system must automatically declare the winner and close the auction when the predefined end time is reached.
No further bids should be accepted after the auction ends.