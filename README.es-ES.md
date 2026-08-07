

# face_ncnn
Android Detección y reconocimiento facial

[![](https://jitpack.io/v/zhu260824/face_ncnn.svg)](https://jitpack.io/#zhu260824/face_ncnn)

#### Descripción de los algoritmos de código abierto
- Algoritmo de inferencia: [ncnn](https://github.com/Tencent/ncnn)
- Algoritmo de detección: [Ultra](https://github.com/Linzaer/Ultra-Light-Fast-Generic-Face-Detector-1MB)
- Algoritmo de reconocimiento: mobilefacenet

#### Uso
-  Añadir dependencias
    1. Añádelo al final de los repositorios en tu build.gradle raíz:
        ```
        allprojects {
        	repositories {
	            	...
	        	maven { url 'https://jitpack.io' }
        	}
        }
        ```
    2. Añade la dependencia
        ```
        dependencies {
            implementation 'com.github.zhu260824:face_ncnn:2.0.0'
        }
        ```
-  Uso en el código
    1.  Inicialización del SDK (inicialización por defecto)
        ```
        /**
        * Inicializa el modelo del SDK
        *
        * @param mContext Contexto de la aplicación
        * @param gpu      Indica si el algoritmo de inferencia se ejecutará en la GPU
        * @return Resultado de la inicialización
        */
        public boolean init(Context mContext, boolean gpu){}
        ```
        Inicialización individual
        ```
        /**
        * Inicializa el modelo de detección facial
        *
        * @param binPath   Ruta al archivo .bin del modelo
        * @param paramPath Ruta al archivo .param del modelo
        * @param reWidth   Ancho de la imagen para la detección facial
        * @param reHeight  Alto de la imagen para la detección facial
        * @param gpu       Indica si el algoritmo de inferencia se ejecutará en la GPU
        */
        public native boolean initDetector(String binPath, String paramPath, int reWidth,int reHeight, boolean gpu);
        ```
        ```
        /**
        * Inicializa el reconocimiento facial
        *
        * @param binPath   Ruta al archivo .bin del modelo
        * @param paramPath Ruta al archivo .param del modelo
        * @param gpu       Indica si el algoritmo de inferencia se ejecutará en la GPU
        */
        public native boolean initRecognize(String binPath, String paramPath, boolean gpu);
        ```
    2.  Uso
        ```
        /**
        * Analiza la información facial
        *
        * @param imgPath Ruta de la imagen
        * @return Información facial
        */
        public native FaceInfo[] detect(String imgPath);
        /**
        * Analiza la información facial
        *
        * @param yuv    Fotograma de vista previa de la cámara
        * @param width  Ancho del fotograma
        * @param height Alto del fotograma
        * @deprecated Para usar este método, es necesario rotar el fotograma de la cámara a 0 grados
        */
        public native FaceInfo[] detectYuv(byte[] yuv, int width, int height);
        ```
        ```
        /**
        * Obtiene los valores de características de la imagen
        *
        * @param imgPath Ruta de la imagen
        * @return Valores de características
        */
        public native float[] getFeature(String imgPath);
        /**
        * Obtiene los valores de características de la imagen
        *
        * @param yuv    Fotograma de vista previa de la cámara
        * @param width  Ancho del fotograma
        * @param height Alto del fotograma
        * @deprecated Para usar este método, es necesario rotar el fotograma de la cámara a 0 grados
        */
        public native float[] getFeatureYuv(byte[] yuv, int width, int height);
        /**
        * Comparación de valores de características
        *
        * @param feature1 Valores de características
        * @param feature2 Valores de características
        * @return Similitud de los valores de características
        */
        public native double featureCompare(float[] feature1, float[] feature2);
        ```
    3.  Configuración
        ```
        /**
        * Establece el número de hilos de inferencia del algoritmo
        *
        * @param threads Número de hilos
        */
        public native void setThreadNum(int threads);
        /**
        * Establece el umbral de confianza para la detección facial
        *
        * @param score Confianza (valor por defecto: 0.7)
        */
        public native void setScoreThreshold(int score);
        /**
        * Indica si el algoritmo de inferencia se ejecutará en la GPU
        */
        public native void setRunGpu(boolean run);
        /**
        * Establece el tamaño de la imagen para la detección facial
        */
        public native void setReSize(int w, int h);
        ```
-  Reducción del tamaño del APK (.apk) excluyendo paquetes .so según el dispositivo
    ```
    android {
        ...
        defaultConfig {
           ...
            ndk {
                abiFilters "armeabi-v7a", "arm64-v8a" //, "x86", "x86_64"
            }
        }
    }
    ```
#### Resumen
-  Este proyecto se basa en la compilación e integración de código abierto. Sin embargo, con esta implementación, el uso se simplifica considerablemente, al tiempo que se reducen los errores de compilación causados por problemas del entorno. De esta manera, permite a los desarrolladores implementar la detección y el reconocimiento facial de manera más rápida.
