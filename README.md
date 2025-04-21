# 📱 Comandera Android - Proyecto en Java

Aplicación móvil de toma de pedidos para restaurantes, desarrollada en Android Studio con Java.

## 📂 Requisitos

- Android Studio (versión recomendada: Hedgehog o superior)
- JDK 8 o superior
- Gradle (manejada automáticamente por Android Studio)
- Conexión a Internet (para sincronizar dependencias)

## 🚀 Cómo importar el proyecto

1. **Clona el repositorio:**

   ```bash
   git clone https://github.com/carlotaGP/Comandera.git
   
### 2. Abrir el proyecto en Android Studio

- Abre Android Studio.
- Selecciona **"Open an existing project"**.
- Busca y selecciona la carpeta del proyecto que acabas de clonar.

### 3. Sincronizar Gradle

- Android Studio descargará automáticamente las dependencias.
- Si aparece algún aviso, haz clic en **"Sync Now"** en la parte superior.

### 4. Configurar emulador o dispositivo físico

- Abre el **AVD Manager** y crea un emulador, o  
- Conecta un dispositivo Android con la **depuración USB** activada.

### 5. Ejecutar la aplicación

- Haz clic en el botón verde **"Run" (▶️)** o pulsa **Shift + F10**.

---

## 🛠️ Estructura del Proyecto

```plaintext
Comandera/app/src/
├───androidTest
│   └───java
│       └───com
│           └───example
│               └───comandera
├───main
│   ├───assets
│   ├───java
│   │   └───com
│   │       └───example
│   │           └───comandera
│   │               ├───adapters
│   │               ├───data
│   │               └───utils
│   └───res
│       ├───drawable
│       ├───drawable-v24
│       ├───layout
│       ├───mipmap-anydpi-v26
│       ├───mipmap-hdpi
│       ├───mipmap-mdpi
│       ├───mipmap-xhdpi
│       ├───mipmap-xxhdpi
│       ├───mipmap-xxxhdpi
│       ├───values
│       ├───values-night
│       └───xml
└───test
