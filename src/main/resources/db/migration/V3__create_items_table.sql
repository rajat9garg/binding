-- Create items table for auction listings
CREATE TABLE items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    base_price DECIMAL(19, 4) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'UPCOMING', 'ONGOING', 'ENDED', 'CANCELLED')),
    auction_start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    auction_end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    current_bid_id BIGINT,
    created_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

-- Add comments for better documentation
COMMENT ON TABLE items IS 'Stores information about items available for auction';
COMMENT ON COLUMN items.status IS 'Current status of the auction: DRAFT, UPCOMING, ONGOING, ENDED, or CANCELLED';
COMMENT ON COLUMN items.base_price IS 'Starting price for the auction';
COMMENT ON COLUMN items.current_bid_id IS 'Reference to the current highest bid (if any)';

-- Create indexes for performance
CREATE INDEX idx_items_status ON items(status);
CREATE INDEX idx_items_auction_times ON items(auction_start_time, auction_end_time);
CREATE INDEX idx_items_created_by ON items(created_by);

-- Add constraint to ensure end time is after start time
ALTER TABLE items 
ADD CONSTRAINT chk_auction_times 
CHECK (auction_end_time > auction_start_time);
