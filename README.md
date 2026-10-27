# GymPlanner 

Aplicación Android para **planificar tu semana de entrenamiento**: crea entrenamientos con sus ejercicios y los músculos que trabajan, arrástralos a cada día de la semana y recibe un recordatorio antes de cada sesión.

Proyecto personal desarrollado como parte de mi portfolio (ciclo DAM).

## Capturas

|Entrenamientos|Nuevo entrenamiento|Semana|
|-|-|-|
|!\[Entrenamientos](docs/entrenamientos.png)|!\[Nuevo entrenamiento](docs/nuevo.png)|!\[Semana](docs/semana.png)|

!\[Demo arrastrar y soltar](docs/demo.gif)

## Funcionalidades

* **Entrenamientos reutilizables** de tres tipos: fuerza, cardio suave (paseos) y running.
* **Ejercicios con series, repeticiones y músculo trabajado** en los entrenamientos de fuerza.
* **Calendario semanal con arrastrar y soltar**: mantén pulsado un entrenamiento y suéltalo sobre un día; después eliges la hora.
* **Recordatorios semanales** mediante notificaciones, con antelación configurable (10, 30 o 60 minutos).
* **Buscador de ejercicios** conectado a la API pública de [wger](https://wger.de): al elegir uno, se rellenan su nombre y el músculo principal.
* **Datos guardados en el dispositivo**, así que funciona sin conexión (salvo el buscador).

## Tecnologías

|Área|Tecnología|
|-|-|
|Lenguaje|Kotlin|
|Interfaz|Jetpack Compose + Material 3|
|Persistencia|Room (SQLite)|
|Estado y lógica|ViewModel, StateFlow y corrutinas|
|Tareas programadas|WorkManager + notificaciones|
|Red|Retrofit + Gson (API REST de wger)|

## Estructura del proyecto

```
app/src/main/java/.../gymplanner
├── data              # Entidades, DAO y base de datos (Room), cliente de la API de wger
├── ui.theme          # Pantallas en Compose y ViewModel
├── notificaciones    # Programación de recordatorios (WorkManager)
└── MainActivity.kt
```

**Modelo de datos:** un `Entrenamiento` tiene muchos `Ejercicio` y puede colocarse en la semana varias veces mediante `Sesion` (día, hora y antelación del aviso). Las relaciones se definen con claves foráneas y borrado en cascada.

## Cómo ejecutarlo

1. Clona el repositorio:

```bash
   git clone https://github.com/samusanchezblanco1/GymPlanner.git
   ```

2. Ábrelo con **Android Studio** y espera a que termine la sincronización de Gradle.
3. Ejecuta la app en un emulador o en un móvil con Android.
4. Acepta el permiso de notificaciones para recibir los recordatorios (Android 13 o superior).

## Qué he aprendido

* Diseñar una base de datos relacional con **Room** (relaciones uno a muchos y borrado en cascada).
* Construir interfaces declarativas con **Jetpack Compose** y gestionar el estado.
* Implementar **arrastrar y soltar** con gestos propios, calculando qué zona queda bajo el dedo.
* Programar tareas periódicas con **WorkManager** y gestionar el permiso de notificaciones.
* Consumir una **API REST** con Retrofit, con manejo de errores y búsqueda con retardo (*debounce*).

## Posibles mejoras

* Mover sesiones ya colocadas arrastrándolas a otro día.
* Animaciones en las interacciones.
* Historial de entrenamientos realizados y estadísticas por músculo.
* Tests unitarios para el ViewModel y el DAO.

## Créditos

Los datos del buscador de ejercicios proceden de [wger](https://wger.de), un proyecto de código abierto. Consulta su licencia y condiciones de uso en su sitio web.

## Autor

**Samuel Sánchez Blanco** — Estudiante de DAM 

