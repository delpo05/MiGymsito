# Visualización de Mejor Puntaje (Récord Personal) por Peso, Peso × Repeticiones y Cardio

El objetivo de esta tarea es permitir que el usuario visualice su **Mejor Puntaje / Récord Personal (PR)** directamente en las pantallas donde carga sus repeticiones o sesiones de cardio. Para los ejercicios de fuerza, se podrá alternar entre **POR PESO** y **POR PESO × REPETICIONES**. Para los ejercicios de cardio, se podrá alternar entre las métricas disponibles (Distancia, Tiempo, Calorías).

## User Review Required

> [!NOTE]
> - **Criterio para Cardio**: Se incluye selección por **Distancia**, **Tiempo / Duración** y **Calorías**, adaptándose dinámicamente según las métricas habilitadas para cada ejercicio de cardio.
> - **Paleta de Colores**: Se mantiene la identidad visual de la app usando fondos oscuros (`#1A1A1A`), bordes translúcidos (`#40FFFFFF`), texto blanco (`@color/blanco`) y acentos rojos (`@color/rojo`: `#FFC14848`).

---

## Proposed Changes

### Utility Layer & Unit Tests

#### [NEW] [PersonalRecordUtils.java](file:///C:/Users/delpo/StudioProjects/MiGymsito/app/src/main/java/com/example/migymsito/utils/PersonalRecordUtils.java)
- Crear una clase de utilidad estática pura y testeable para calcular el mejor récord por peso (`calcularMejorPeso`) y por peso × repeticiones (`calcularMejorVolumenSerie`), así como récords de cardio (`calcularMejorDistancia`, `calcularMejorDuracion`, `calcularMejorCalorias`).

#### [NEW] [PersonalRecordUtilsTest.java](file:///C:/Users/delpo/StudioProjects/MiGymsito/app/src/test/java/com/example/migymsito/utils/PersonalRecordUtilsTest.java)
- Pruebas unitarias para validar los cálculos de Récord Personal de fuerza (Peso máximo, Peso × Reps máximo, desempates por repeticiones/fecha) y cardio.

---

### Fuerza / Repeticiones Component

#### [cargar_registro_activity.xml](file:///C:/Users/delpo/StudioProjects/MiGymsito/app/src/main/res/layout/cargar_registro_activity.xml)
- Agregar un `MaterialCardView` (`cvMejorPuntaje`) con la cabecera "🏆 MEJOR PUNTAJE", un `MaterialButtonToggleGroup` con opciones `Peso` y `Peso × Reps`, y TextViews para mostrar el valor del récord y sus detalles (repeticiones y fecha).

#### [CargarRegistroFragment.java](file:///C:/Users/delpo/StudioProjects/MiGymsito/app/src/main/java/com/example/migymsito/CargarRegistroFragment.java)
- Vincular las vistas del grupo de botones toggle y la tarjeta de mejor puntaje.
- Cargar los registros históricos del ejercicio del usuario y calcular el récord actual.
- Actualizar dinámicamente la UI al cambiar entre "Peso" y "Peso × Reps", y refrescar inmediatamente al registrar, editar o borrar series.
- Mostrar una notificación emergente/Toast en caso de superar el récord personal.

---

### Cardio Component

#### [fragment_cargar_registro_cardio.xml](file:///C:/Users/delpo/StudioProjects/MiGymsito/app/src/main/res/layout/fragment_cargar_registro_cardio.xml)
- Agregar un `MaterialCardView` (`cvMejorPuntajeCardio`) con la cabecera "🏆 MEJOR PUNTAJE", un `MaterialButtonToggleGroup` con opciones `Distancia`, `Tiempo` y `Calorías`, y TextViews para el valor y detalle.

#### [CargarRegistroCardioFragment.java](file:///C:/Users/delpo/StudioProjects/MiGymsito/app/src/main/java/com/example/migymsito/CargarRegistroCardioFragment.java)
- Vincular las vistas del toggle y tarjeta de mejor puntaje de cardio.
- Configurar visibilidad de botones toggle según las métricas que registra el ejercicio (`registraDistancia`, `registraCalorias`).
- Cargar historial de cardio y calcular el mejor récord según la pestaña seleccionada.
- Refrescar al guardar o eliminar un registro de cardio.

---

## Verification Plan

### Automated Tests
- Compilación del proyecto:
  ```powershell
  ./gradlew app:assembleDebug
  ```
- Pruebas unitarias en JVM:
  ```powershell
  ./gradlew app:testDebugUnitTest --tests "com.example.migymsito.utils.PersonalRecordUtilsTest"
  ```

### Manual Verification
- Carga de repeticiones en ejercicio de fuerza:
  - Seleccionar un ejercicio de fuerza.
  - Verificar que aparezca la tarjeta "🏆 MEJOR PUNTAJE".
  - Probar cambiar el toggle entre "Peso" y "Peso × Reps".
  - Cargar una serie con mayor peso/volumen y comprobar que el Récord Personal se actualice en tiempo real.
- Carga de registro de cardio:
  - Seleccionar un ejercicio de cardio.
  - Verificar la tarjeta "🏆 MEJOR PUNTAJE" con los toggles de Distancia/Tiempo/Calorías.
  - Cargar un registro de cardio y comprobar la actualización del récord.
