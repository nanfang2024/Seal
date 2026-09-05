package com.wu.hen

/**
 * Version information for the application
 */
object CurrentVersion {
    const val MAJOR = 1
    const val MINOR = 0
    const val PATCH = 0
    
    const val NAME = "${MAJOR}.${MINOR}.${PATCH}"
    const val CODE = (MAJOR * 100_000_000) + (MINOR * 100_000) + PATCH
    
    val isRelease: Boolean get() = true
}
