-- Create item_images table for storing item images
CREATE TABLE item_images (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT false,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add comments for better documentation
COMMENT ON TABLE item_images IS 'Stores images associated with auction items';
COMMENT ON COLUMN item_images.is_primary IS 'Indicates if this is the primary image for the item';
COMMENT ON COLUMN item_images.display_order IS 'Order in which to display the images';

-- Create index for performance
CREATE INDEX idx_item_images_item_id ON item_images(item_id);
CREATE INDEX idx_item_images_primary ON item_images(item_id, is_primary) WHERE is_primary = true;
