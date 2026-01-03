
# 📡 Sistema de Streaming IoT: Android a AWS (RTMP -> HLS)

[![Language](https://img.shields.io/badge/Kotlin-Android-green.svg)](https://kotlinlang.org/)
[![Server](https://img.shields.io/badge/AWS-EC2%20Ubuntu-orange.svg)](https://aws.amazon.com/ec2/)
[![Web Server](https://img.shields.io/badge/Nginx-RTMP-blue.svg)](https://github.com/arut/nginx-rtmp-module)
[![Status](https://img.shields.io/badge/Status-Completed-success.svg)]()

> **Proyecto de Sistemas Embebidos**  
> **Tema:** Implementación de arquitectura distribuida para streaming de video en tiempo real.

---

## 📋 Descripción del Proyecto

Este repositorio contiene el código fuente y los archivos de configuración para implementar un sistema de **video streaming en tiempo real**. El sistema convierte un dispositivo móvil Android (nodo embebido de borde) en una cámara de transmisión que envía video a un servidor en la nube (AWS), el cual distribuye la señal a múltiples clientes web de forma escalable.

El proyecto aborda el desafío de transmitir contenido multimedia desde dispositivos con ancho de banda de subida limitado, utilizando una arquitectura **RTMP (Ingesta) + HLS (Distribución)**.

### 🚀 Características Principales
*   **Ultra Baja Latencia:** Optimización de Nginx para reducir el delay de ~15s a **3-4 segundos**.
*   **Escalabilidad:** Un solo dispositivo móvil puede servir a cientos de clientes simultáneos gracias al servidor intermedio.
*   **Visualización Universal:** Uso de HLS para compatibilidad con cualquier navegador moderno (PC/Móvil).

---

## 📂 Estructura del Repositorio

```text
├── android-app/          # Proyecto completo de Android Studio
│   ├── app/
│   │   ├── src/main/java/     # Código Fuente Kotlin (MainActivity)
│   │   └── src/main/res/      # Layouts XML
│   └── build.gradle           # Dependencias (RootEncoder)
│
├── server-config/        # Archivos de configuración del Servidor Ubuntu
│   ├── nginx.conf        # Configuración optimizada (Low Latency)
│   └── index.html        # Cliente Web con hls.js
│
└── assets/               # Capturas de pantalla y evidencias (opcional)
```

---

## 🛠️ Arquitectura Técnica

El flujo de datos sigue el modelo **Edge-Cloud-Client**:

1.  **EMISOR (Android):** Captura cámara y audio -> Codifica H.264/AAC -> Transmite vía **RTMP** (Push).
2.  **SERVIDOR (AWS):** Recibe RTMP -> Segmenta en tiempo real (`.ts`) -> Genera playlist (`.m3u8`) -> Sirve vía **HLS** (Pull).
3.  **CLIENTE (Web):** Navegador descarga playlist -> Renderiza video usando **hls.js**.

---

## ⚙️ Guía de Instalación y Despliegue

### 1. Configuración del Servidor (AWS EC2)

Requisitos: Instancia Ubuntu 22.04 LTS con puertos **1935** (RTMP) y **80** (HTTP) abiertos.

1.  **Instalar dependencias:**
    ```bash
    sudo apt update
    sudo apt install nginx libnginx-mod-rtmp
    ```

2.  **Configurar Nginx:**
    Reemplaza el archivo `/etc/nginx/nginx.conf` con el contenido de `server-config/nginx.conf` de este repositorio. Este archivo incluye la optimización `hls_fragment 1s` para baja latencia.

3.  **Crear el sitio web:**
    Coloca el archivo `server-config/index.html` en la ruta: `/var/www/html/index.html`.

4.  **Permisos y Reinicio:**
    ```bash
    sudo mkdir -p /var/www/html/hls
    sudo chown -R www-data:www-data /var/www/html/hls
    sudo systemctl restart nginx
    ```

### 2. Configuración de la App Android

1.  Abrir la carpeta `android-app` en **Android Studio**.
2.  Verificar que el archivo `AndroidManifest.xml` incluya `android:usesCleartextTraffic="true"` (necesario para IP sin SSL).
3.  **Configurar IP:**
    Abrir `MainActivity.kt` y actualizar la variable con la IP de tu servidor AWS:
    ```kotlin
    private val rtmpUrl = "rtmp://[TU_IP_PUBLICA]/live/stream"
    ```
4.  Compilar y ejecutar en un dispositivo físico.


---

## 📝 Marco Teórico (Resumen)

Este proyecto aplica conceptos fundamentales de **Sistemas Distribuidos Multimedia**:

*   **Ingesta vs Distribución:** Separamos la captura (RTMP) de la entrega (HLS) para manejar el ancho de banda.
*   **Buffer y Latencia:** Mediante el ajuste de `hls_fragment` y `hls_playlist_length`, controlamos el trade-off entre estabilidad y tiempo real, logrando reducir la latencia de 15s a <4s.
*   **Interoperabilidad:** El sistema funciona independientemente del hardware del cliente final gracias al uso de estándares web.


---

> Este proyecto fue desarrollado con fines educativos para demostrar la integración de nodos IoT con infraestructura Cloud. 
> 
```
