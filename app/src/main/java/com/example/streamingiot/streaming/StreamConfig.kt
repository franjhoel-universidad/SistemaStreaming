package com.example.streamingiot.streaming

/**
 * ============================================================================ CONFIGURACIÓN DE
 * STREAMING ============================================================================
 *
 * Clase de datos que contiene toda la configuración para el streaming RTMP. Incluye parámetros de
 * video, audio y conexión al servidor.
 *
 * ============================================================================
 */

/**
 * Configuración del streaming RTMP
 *
 * @param rtmpUrl URL base del servidor RTMP (ej: rtmp://192.168.1.100:1935/live)
 * @param streamKey Clave única del stream (ej: "stream")
 * @param videoWidth Ancho del video en píxeles
 * @param videoHeight Alto del video en píxeles
 * @param videoBitrate Bitrate del video en bps
 * @param videoFps Frames por segundo
 * @param audioBitrate Bitrate del audio en bps
 * @param audioSampleRate Tasa de muestreo del audio en Hz
 * @param audioIsStereo Si el audio es estéreo
 */
data class StreamConfig(
        val rtmpUrl: String = "", // Dejar vacío para que el usuario lo configure
        val streamKey: String = "stream",

        // Configuración de Video
        val videoWidth: Int = 1280,
        val videoHeight: Int = 720,
        val videoBitrate: Int = 2500000, // 2.5 Mbps
        val videoFps: Int = 30,

        // Configuración de Audio
        val audioBitrate: Int = 128000, // 128 Kbps
        val audioSampleRate: Int = 44100,
        val audioIsStereo: Boolean = true
) {
        /** Retorna la URL completa del stream incluyendo la stream key */
        fun getFullRtmpUrl(): String = "$rtmpUrl/$streamKey"

        /** Presets de calidad predefinidos */
        companion object {
                /**
                 * Preset de baja calidad - Para conexiones lentas 480p @ 15fps, 500 Kbps video, 64
                 * Kbps audio
                 */
                val LOW_QUALITY =
                        StreamConfig(
                                videoWidth = 854,
                                videoHeight = 480,
                                videoBitrate = 500000,
                                videoFps = 15,
                                audioBitrate = 64000,
                                audioSampleRate = 22050,
                                audioIsStereo = false
                        )

                /**
                 * Preset de calidad media - Balance entre calidad y ancho de banda 720p @ 24fps,
                 * 1.5 Mbps video, 96 Kbps audio
                 */
                val MEDIUM_QUALITY =
                        StreamConfig(
                                videoWidth = 1280,
                                videoHeight = 720,
                                videoBitrate = 1500000,
                                videoFps = 24,
                                audioBitrate = 96000,
                                audioSampleRate = 44100,
                                audioIsStereo = true
                        )
        }
}

/** Estados posibles del streaming */
enum class StreamState {
        IDLE, // Sin iniciar
        PREPARING, // Preparándose
        STREAMING, // Transmitiendo
        STOPPED, // Detenido
        ERROR // Error
}

/** Información del stream en tiempo real */
data class StreamStats(
        val fps: Double = 0.0,
        val bitrate: Long = 0L,
        val droppedFrames: Int = 0,
        val duration: Long = 0L,
        val isConnected: Boolean = false
)