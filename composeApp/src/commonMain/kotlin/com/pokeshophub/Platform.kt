package com.pokeshophub

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
