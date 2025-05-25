package com.biding.db.jooq

import com.biding.domain.model.WebSocketConnection
import com.biding.db.WebSocketConnectionRepository
import com.biding.infrastructure.jooq.Tables
import com.biding.infrastructure.jooq.tables.records.WebsocketConnectionsRecord
import com.biding.exception.DataAccessException
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.RecordMapper
import org.jooq.Result
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Repository
class JooqWebSocketConnectionRepository(
    private val dsl: DSLContext
) : WebSocketConnectionRepository {

    private val table = Tables.WEBSOCKET_CONNECTIONS
    
    private val recordToModel: (WebsocketConnectionsRecord) -> WebSocketConnection = { record ->
        WebSocketConnection(
            id = record.id,
            connectionId = record.connectionId,
            userId = record.userId,
            sessionId = record.sessionId,
            connectedAt = record.connectedAt.toInstant(),
            disconnectedAt = record.disconnectedAt?.toInstant(),
            lastActiveAt = record.lastActiveAt.toInstant(),
            userAgent = record.userAgent,
            ipAddress = record.ipAddress,
            status = WebSocketConnection.ConnectionStatus.valueOf(record.status)
        )
    }


    override fun save(connection: WebSocketConnection): WebSocketConnection {
        return try {
            val now = OffsetDateTime.now()
            val record = dsl.insertInto(table)
                .set(table.CONNECTION_ID, connection.connectionId)
                .set(table.USER_ID, connection.userId)
                .set(table.SESSION_ID, connection.sessionId)
                .set(table.CONNECTED_AT, connection.connectedAt.atOffset(ZoneOffset.UTC))
                .set(table.DISCONNECTED_AT, connection.disconnectedAt?.atOffset(ZoneOffset.UTC))
                .set(table.LAST_ACTIVE_AT, connection.lastActiveAt.atOffset(ZoneOffset.UTC))
                .set(table.USER_AGENT, connection.userAgent)
                .set(table.IP_ADDRESS, connection.ipAddress)
                .set(table.STATUS, connection.status.name)
                .onConflict(table.CONNECTION_ID)
                .doUpdate()
                .set(table.USER_ID, connection.userId)
                .set(table.LAST_ACTIVE_AT, connection.lastActiveAt.atOffset(ZoneOffset.UTC))
                .set(table.STATUS, connection.status.name)
                .returning()
                .fetchOne()
                ?: throw DataAccessException("Failed to save WebSocket connection: ${connection.connectionId}")
                
            recordToModel(record)
        } catch (e: Exception) {
            throw DataAccessException("Error saving WebSocket connection")
        }
    }

    override fun findById(connectionId: String): WebSocketConnection? {
        return try {
            dsl.selectFrom(table)
                .where(table.CONNECTION_ID.eq(connectionId))
                .fetchOne()
                ?.let(recordToModel)
        } catch (e: Exception) {
            throw DataAccessException("Error finding WebSocket connection: $connectionId")
        }
    }

    override fun markDisconnected(connectionId: String): WebSocketConnection? {
        return try {
            dsl.update(table)
                .set(table.STATUS, WebSocketConnection.ConnectionStatus.DISCONNECTED.name)
                .set(table.DISCONNECTED_AT, OffsetDateTime.now())
                .where(table.CONNECTION_ID.eq(connectionId))
                .returning()
                .fetchOne()
                ?.let(recordToModel)
        } catch (e: Exception) {
            throw DataAccessException("Error marking WebSocket connection as disconnected: $connectionId")
        }
    }

    override fun updateLastActive(connectionId: String): WebSocketConnection? {
        return try {
            dsl.update(table)
                .set(table.LAST_ACTIVE_AT, OffsetDateTime.now())
                .where(table.CONNECTION_ID.eq(connectionId))
                .returning()
                .fetchOne()
                ?.let(recordToModel)
        } catch (e: Exception) {
            throw DataAccessException("Error updating last active time for connection: $connectionId")
        }
    }

    override fun findAllActive(): List<WebSocketConnection> {
        return try {
            dsl.selectFrom(table)
                .where(table.STATUS.eq(WebSocketConnection.ConnectionStatus.CONNECTED.name))
                .fetch()
                .map(recordToModel)
        } catch (e: Exception) {
            throw DataAccessException("Error finding active WebSocket connections")
        }
    }

    override fun findByUserId(userId: Long): List<WebSocketConnection> {
        return try {
            dsl.selectFrom(table)
                .where(table.USER_ID.eq(userId))
                .fetch()
                .map(recordToModel)
        } catch (e: Exception) {
            throw DataAccessException("Error finding WebSocket connections for user: $userId")
        }
    }

    override fun cleanupStaleConnections(olderThan: Instant): Int {
        return try {
            dsl.deleteFrom(table)
                .where(table.LAST_ACTIVE_AT.lt(olderThan.atOffset(ZoneOffset.UTC)))
                .execute()
        } catch (e: Exception) {
            throw DataAccessException("Error cleaning up stale WebSocket connections")
        }
    }


}
