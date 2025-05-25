-- Create websocket_connections table for tracking WebSocket connections
CREATE TABLE websocket_connections (
    id BIGSERIAL PRIMARY KEY,
    connection_id VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    session_id VARCHAR(255) NOT NULL,
    connected_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    disconnected_at TIMESTAMP WITH TIME ZONE,
    last_active_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_agent TEXT,
    ip_address VARCHAR(45),
    status VARCHAR(20) NOT NULL DEFAULT 'CONNECTED' CHECK (status IN ('CONNECTED', 'DISCONNECTED', 'ERROR'))
);

-- Add comments for better documentation
COMMENT ON TABLE websocket_connections IS 'Tracks WebSocket connections for real-time auction updates';
COMMENT ON COLUMN websocket_connections.connection_id IS 'Unique identifier for the WebSocket connection';
COMMENT ON COLUMN websocket_connections.user_id IS 'Reference to the user who established the connection';
COMMENT ON COLUMN websocket_connections.session_id IS 'HTTP session ID associated with the connection';
COMMENT ON COLUMN websocket_connections.status IS 'Current status of the WebSocket connection';

-- Create indexes for performance
CREATE INDEX idx_ws_connections_user_id ON websocket_connections(user_id);
CREATE INDEX idx_ws_connections_status ON websocket_connections(status);
CREATE INDEX idx_ws_connections_connection_id ON websocket_connections(connection_id);
CREATE INDEX idx_ws_connections_session_id ON websocket_connections(session_id);

-- Create function to update last_active_at
CREATE OR REPLACE FUNCTION update_websocket_activity()
RETURNS TRIGGER AS $$
BEGIN
    NEW.last_active_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for updating last_active_at
CREATE TRIGGER update_websocket_activity_trigger
BEFORE UPDATE ON websocket_connections
FOR EACH ROW
WHEN (NEW.status = 'CONNECTED')
EXECUTE FUNCTION update_websocket_activity();
