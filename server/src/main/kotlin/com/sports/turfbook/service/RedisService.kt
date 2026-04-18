package com.sports.turfbook.service

import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.params.SetParams

object RedisService {

    private val pool: JedisPool by lazy {
        JedisPool(
            JedisPoolConfig(),
            System.getenv("REDIS_HOST") ?: "localhost",
            System.getenv("REDIS_PORT")?.toInt() ?: 6379
        )
    }

    private fun slotKey(courtId: String, date: String, startTime: String, durationMinutes: Int) =
        "slot_lock:$courtId:$date:$startTime:$durationMinutes"

    /**
     * Atomically acquires a 5-minute slot hold for [userId].
     * Returns true if the lock was acquired (slot was free), false if already held.
     */
    fun lockSlot(courtId: String, date: String, startTime: String, durationMinutes: Int, userId: String): Boolean =
        pool.resource.use { redis ->
            val key = slotKey(courtId, date, startTime, durationMinutes)
            redis.set(key, userId, SetParams.setParams().nx().ex(300L)) == "OK"
        }

    fun releaseSlot(courtId: String, date: String, startTime: String, durationMinutes: Int) =
        pool.resource.use { redis ->
            redis.del(slotKey(courtId, date, startTime, durationMinutes))
        }

    fun isSlotLocked(courtId: String, date: String, startTime: String, durationMinutes: Int): Boolean =
        pool.resource.use { redis ->
            redis.exists(slotKey(courtId, date, startTime, durationMinutes))
        }
}
