# Auction WebSocket Implementation Plan

## 1. Overview
This document outlines the implementation of WebSocket support for real-time auction updates, including connection tracking in the database and OpenAPI documentation.

## 2. Database Schema Updates

### 2.1 WebSocket Connection Table
```sql
CREATE TABLE websocket_connections (
    id BIGSERIAL PRIMARY KEY,
    connection_id VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT REFERENCES users(id),
    session_id VARCHAR(255) NOT NULL,
    connected_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    disconnected_at TIMESTAMP WITH TIME ZONE,
    last_active_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_agent TEXT,
    ip_address VARCHAR(45),
    status VARCHAR(20) NOT NULL DEFAULT 'CONNECTED' CHECK (status IN ('CONNECTED', 'DISCONNECTED', 'ERROR'))
);

-- Indexes for performance
CREATE INDEX idx_ws_connections_user_id ON websocket_connections(user_id);
CREATE INDEX idx_ws_connections_status ON websocket_connections(status);
CREATE INDEX idx_ws_connections_connection_id ON websocket_connections(connection_id);
```

## 3. OpenAPI Specification

### 3.1 WebSocket Connection Endpoint
```yaml
openapi: 3.0.3
info:
  title: Auction WebSocket API
  version: 1.0.0
  description: Real-time auction updates via WebSocket

servers:
  - url: ws://localhost:8080/ws
    description: WebSocket server

components:
  schemas:
    WebSocketConnectionRequest:
      type: object
      properties:
        userId:
          type: integer
          format: int64
          description: ID of the user connecting
          example: 123
      required:
        - userId

    AuctionItem:
      type: object
      properties:
        id:
          type: integer
          format: int64
          example: 1
        name:
          type: string
          example: "Rare Collectible Item"
        description:
          type: string
          example: "A rare collectible in excellent condition"
        status:
          type: string
          enum: [DRAFT, UPCOMING, ONGOING, ENDED, CANCELLED]
          example: "ONGOING"
        currentPrice:
          type: number
          format: decimal
          example: 150.50
        auctionStartTime:
          type: string
          format: date-time
          example: "2025-06-01T14:00:00Z"
        auctionEndTime:
          type: string
          format: date-time
          example: "2025-06-05T14:00:00Z"

    WebSocketMessage:
      type: object
      oneOf:
        - $ref: '#/components/schemas/AuctionListMessage'
        - $ref: '#/components/schemas/ErrorMessage'

    AuctionListMessage:
      type: object
      properties:
        type:
          type: string
          enum: [AUCTION_LIST]
          example: "AUCTION_LIST"
        data:
          type: array
          items:
            $ref: '#/components/schemas/AuctionItem'

    ErrorMessage:
      type: object
      properties:
        type:
          type: string
          enum: [ERROR]
          example: "ERROR"
        message:
          type: string
          example: "Failed to process request"

  securitySchemes:
    WebSocketAuth:
      type: apiKey
      in: header
      name: X-User-ID
      description: User ID for WebSocket authentication
```

## 4. Implementation Details

### 4.1 WebSocket Configuration
```kotlin
@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {
    
    @Value("\${app.websocket.allowed-origins:*}")
    private val allowedOrigins: Array<String> = arrayOf("*")

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        config.enableSimpleBroker("/topic")
        config.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns(*allowedOrigins)
            .withSockJS()
    }
}
```

### 4.2 WebSocket Connection Interceptor
```kotlin
@Component
class WebSocketConnectionInterceptor(
    private val connectionRepository: WebSocketConnectionRepository
) : ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val accessor = StompHeaderAccessor.wrap(message)
        
        when (accessor.command) {
            StompCommand.CONNECT -> handleConnect(accessor)
            StompCommand.DISCONNECT -> handleDisconnect(accessor)
            StompCommand.SUBSCRIBE -> handleSubscribe(accessor)
            else -> {}
        }
        
        return message
    }
    
    private fun handleConnect(accessor: StompHeaderAccessor) {
        val userId = accessor.getFirstNativeHeader("X-User-ID")?.toLongOrNull()
        val sessionId = accessor.sessionId ?: return
        val userAgent = accessor.getFirstNativeHeader("user-agent")
        
        connectionRepository.saveConnection(
            WebSocketConnection(
                connectionId = sessionId,
                userId = userId,
                sessionId = sessionId,
                userAgent = userAgent,
                ipAddress = getClientIp(accessor)
            )
        )
    }
    
    private fun handleDisconnect(accessor: StompHeaderAccessor) {
        accessor.sessionId?.let { connectionRepository.markDisconnected(it) }
    }
    
    private fun handleSubscribe(accessor: StompHeaderAccessor) {
        accessor.sessionId?.let { connectionRepository.updateLastActive(it) }
    }
    
    private fun getClientIp(accessor: StompHeaderAccessor): String? {
        // Implementation to extract client IP from headers
        return accessor.getFirstNativeHeader("X-Forwarded-For")
            ?.split(",")
            ?.firstOrNull()
            ?.trim()
    }
}
```

### 4.3 Auction Controller
```kotlin
@Controller
@Tag(name = "Auction WebSocket", description = "Real-time auction updates")
class AuctionWebSocketController(
    private val auctionService: AuctionService,
    private val messagingTemplate: SimpMessagingTemplate
) {
    
    @MessageMapping("/auctions/subscribe")
    @SendTo("/topic/auctions/updates")
    fun subscribeToAuctions(
        @Header("simpSessionId") sessionId: String,
        @Header("X-User-ID") userId: Long
    ): WebSocketResponse<List<AuctionItem>> {
        val auctions = auctionService.getActiveAuctions()
        return WebSocketResponse(
            type = "AUCTION_LIST",
            data = auctions
        )
    }
}
```

### 4.4 Connection Repository
```kotlin
@Repository
class JooqWebSocketConnectionRepository(
    private val dsl: DSLContext
) : WebSocketConnectionRepository {
    
    override fun saveConnection(connection: WebSocketConnection) {
        dsl.insertInto(WEBSOCKET_CONNECTIONS)
            .set(WEBSOCKET_CONNECTIONS.CONNECTION_ID, connection.connectionId)
            .set(WEBSOCKET_CONNECTIONS.USER_ID, connection.userId)
            .set(WEBSOCKET_CONNECTIONS.SESSION_ID, connection.sessionId)
            .set(WEBSOCKET_CONNECTIONS.USER_AGENT, connection.userAgent)
            .set(WEBSOCKET_CONNECTIONS.IP_ADDRESS, connection.ipAddress)
            .set(WEBSOCKET_CONNECTIONS.STATUS, "CONNECTED")
            .onConflict(WEBSOCKET_CONNECTIONS.CONNECTION_ID)
            .doUpdate()
            .set(WEBSOCKET_CONNECTIONS.STATUS, "CONNECTED")
            .set(WEBSOCKET_CONNECTIONS.LAST_ACTIVE_AT, OffsetDateTime.now())
            .execute()
    }
    
    override fun markDisconnected(connectionId: String) {
        dsl.update(WEBSOCKET_CONNECTIONS)
            .set(WEBSOCKET_CONNECTIONS.STATUS, "DISCONNECTED")
            .set(WEBSOCKET_CONNECTIONS.DISCONNECTED_AT, OffsetDateTime.now())
            .where(WEBSOCKET_CONNECTIONS.CONNECTION_ID.eq(connectionId))
            .execute()
    }
    
    override fun updateLastActive(connectionId: String) {
        dsl.update(WEBSOCKET_CONNECTIONS)
            .set(WEBSOCKET_CONNECTIONS.LAST_ACTIVE_AT, OffsetDateTime.now())
            .where(WEBSOCKET_CONNECTIONS.CONNECTION_ID.eq(connectionId))
            .execute()
    }
    
    override fun getActiveConnections(): List<WebSocketConnection> {
        return dsl.selectFrom(WEBSOCKET_CONNECTIONS)
            .where(WEBSOCKET_CONNECTIONS.STATUS.eq("CONNECTED"))
            .fetch { it.into(WebSocketConnection::class.java) }
    }
}
```

## 5. Client Connection Example

### 5.1 Connecting to WebSocket
```javascript
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect(
    {'X-User-ID': '123'}, // User ID from your auth system
    function(frame) {
        console.log('Connected: ' + frame);
        
        // Subscribe to auction updates
        stompClient.subscribe('/topic/auctions/updates', function(message) {
            const update = JSON.parse(message.body);
            if (update.type === 'AUCTION_LIST') {
                // Handle auction list update
                console.log('Received auctions:', update.data);
            }
        });
        
        // Request initial auction list
        stompClient.send('/app/auctions/subscribe');
    },
    function(error) {
        console.error('WebSocket Error:', error);
    }
);
```

## 6. Monitoring and Maintenance

### 6.1 Scheduled Cleanup
```kotlin
@Scheduled(fixedRate = 3600000) // Run every hour
fun cleanupStaleConnections() {
    val cutoffTime = OffsetDateTime.now().minusHours(1)
    connectionRepository.cleanupStaleConnections(cutoffTime)
}
```

### 6.2 Connection Statistics Endpoint
```kotlin
@RestController
@RequestMapping("/api/ws-stats")
class WebSocketStatsController(
    private val connectionRepository: WebSocketConnectionRepository
) {
    @GetMapping("/connections")
    fun getConnectionStats(): ConnectionStats {
        return connectionRepository.getConnectionStats()
    }
}
```

## 7. Testing Strategy

### 7.1 Unit Tests
- Test WebSocket configuration
- Test connection interceptor
- Test message handling

### 7.2 Integration Tests
- Test WebSocket connection lifecycle
- Test message broadcasting
- Test error scenarios

### 7.3 Load Testing
- Test with multiple concurrent connections
- Measure message delivery latency
- Monitor memory usage

## 8. Deployment Considerations

### 8.1 Development
- Use simple in-memory broker
- Enable debug logging
- Monitor connection count

### 8.2 Production
- Consider using Redis for distributed messaging
- Set up monitoring and alerts
- Configure appropriate timeouts
- Plan for horizontal scaling

## 9. Future Enhancements

1. **Connection Management**
   - Add connection heartbeats
   - Implement reconnection strategies
   - Add connection quality metrics

2. **Monitoring**
   - Add Prometheus metrics
   - Set up Grafana dashboards
   - Implement alerting for abnormal conditions

3. **Scalability**
   - Add support for Redis Pub/Sub
   - Implement sticky sessions
   - Add connection sharding
