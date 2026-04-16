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

    private fun slotKey(
        turfId: String, date: String, startTime: String,
        durationMinutes: Int, sport: String
    ) = "slot_lock:$turfId:$date:$startTime:$durationMinutes:$sport"

    /**
     * Atomically acquires a 5-minute slot hold for [userId].
     * Returns true if the lock was acquired (slot was free), false if already held.
     */
    fun lockSlot(
        turfId: String, date: String, startTime: String,
        durationMinutes: Int, sport: String, userId: String
    ): Boolean = pool.resource.use { redis ->
        val key = slotKey(turfId, date, startTime, durationMinutes, sport)
        redis.set(key, userId, SetParams.setParams().nx().ex(300L)) == "OK"
    }

    fun releaseSlot(
        turfId: String, date: String, startTime: String,
        durationMinutes: Int, sport: String
    ) = pool.resource.use { redis ->
        redis.del(slotKey(turfId, date, startTime, durationMinutes, sport))
    }

    fun isSlotLocked(
        turfId: String, date: String, startTime: String,
        durationMinutes: Int, sport: String
    ): Boolean = pool.resource.use { redis ->
        redis.exists(slotKey(turfId, date, startTime, durationMinutes, sport))
    }
}
