📱 Aplicación de Control de Gastos (`com.example.gastos`)
1. Descripción General de la Arquitectura
Esta aplicación Android permite a múltiples usuarios registrar, consultar, editar y eliminar sus gastos mensuales. Su diseño destaca por utilizar una arquitectura modular desacoplada mediante el Patrón Repository (Repositorio) y Herencia de Actividades, permitiendo alternar en tiempo real entre dos motores de persistencia distintos (SQLite y archivos CSV) sin afectar la lógica de la interfaz de usuario.
La aplicación se divide en 4 capas o módulos lógicos:
Capa de Modelo y Catálogos: Estructura de datos (`Expense`) y constantes (`Categories`).
Capa de Preferencias y Utilidades: Gestión de sesión/tema (`Prefs`) y alertas del sistema (`NotificationHelper`).
Capa de Persistencia (Datos): Base de datos relacional (`DbHelper`), contrato de acceso (`ExpenseRepository`) y sus dos implementaciones (`SqliteExpenseRepository` y `CsvExpenseRepository`).
Capa de Presentación (UI / Activities): Clase base (`BaseActivity`) y pantallas de flujo (`LoginActivity`, `RegisterActivity`, `MainActivity`, `SettingsActivity`).
---
2. Explicación Detallada por Módulo y Clase
📦 Módulo A: Modelo y Catálogos
1. `Expense.java` (Modelo de Dominio)
Responsabilidad: Representa la entidad central de un gasto dentro de la aplicación (POJO - Plain Old Java Object).
Atributos: `id` (identificador único), `concept` (descripción), `amount` (monto numérico), `category` (clasificación) y `date` (fecha en formato estándar `yyyy-MM-dd`).
Interconexión: Actúa como el objeto de transferencia de datos (DTO) que viaja entre la capa de persistencia (`ExpenseRepository`) y la pantalla principal (`MainActivity`).
2. `Categories.java` (Catálogo Estático)
Responsabilidad: Centraliza las categorías permitidas (`Comida`, `Transporte`, `Servicios`, `Ocio`, `Salud`, `Otros`).
Métodos clave: `indexOf(String category)` busca la posición de una categoría en el arreglo; si recibe una categoría desconocida o corrupta, devuelve por defecto el índice de `"Otros"`, evitando desbordamientos (`IndexOutOfBoundsException`).
Interconexión: Alimenta los `Spinner` (listas desplegables) tanto al crear como al editar un gasto en `MainActivity`.
---
⚙️ Módulo B: Preferencias y Utilidades del Sistema
3. `Prefs.java` (Gestor de Estado y Configuración)
Responsabilidad: Encapsula el acceso a `SharedPreferences` (`gastos_prefs`) para persistir configuraciones ligeras de clave-valor.
Funciones principales:
Sesión: Guarda y recupera `user_id` y `username` (`saveSession`, `getUserId`, `logout`). Si `getUserId()` devuelve `-1`, no hay sesión activa.
Apariencia: Almacena el color primario en hexadecimal (`DEFAULT_COLOR = 0xFF3F51B5`) y el estado del modo oscuro (`isDark`).
Motor de almacenamiento: Define si los gastos se guardan en `"sqlite"` o `"csv"`.
Interconexión: Es instanciada automáticamente por `BaseActivity`, haciéndola accesible en todas las pantallas de la app.
4. `NotificationHelper.java` (Notificaciones Locales)
Responsabilidad: Gestiona la creación y emisión de notificaciones en la barra de estado de Android.
Funcionamiento técnico:
Verifica permisos en tiempo de ejecución para Android 13+ (API 33) (`POST_NOTIFICATIONS`).
Crea obligatoriamente un `NotificationChannel` (`gastos_channel`) para Android 8+ (API 26).
Usa `(int) System.currentTimeMillis()` como ID único de notificación para que cada gasto registrado genere una alerta independiente sin sobrescribir la anterior.
Interconexión: Es invocada por `MainActivity` inmediatamente después de guardar un nuevo gasto.
---
💾 Módulo C: Capa de Persistencia (Datos)
5. `DbHelper.java` (Gestor de SQLite y Seguridad)
Responsabilidad: Administra el ciclo de vida de la base de datos local `gastos.db` (versión 2) y la autenticación de usuarios.
Estructura de tablas:
`users`: `id`, `username` (UNIQUE), `password`.
`expenses`: `id`, `user_id`, `concept`, `amount`, `category` (agregada en `onUpgrade` de v1 a v2 mediante `ALTER TABLE` para no perder datos previos), `date`.
Seguridad: Implementa `hash(String s)` utilizando SHA-256 en formato hexadecimal. Las contraseñas nunca se almacenan ni comparan en texto plano.
Interconexión: Es utilizada directamente por `LoginActivity` y `RegisterActivity` para validar cuentas, y por `SqliteExpenseRepository` para operar sobre la tabla `expenses`.
6. `ExpenseRepository.java` (Interfaz / Contrato)
Responsabilidad: Define las operaciones CRUD estándar (`add`, `update`, `delete`, `getByMonth`) que cualquier sistema de almacenamiento debe cumplir.
Importancia arquitectónica: Aplica el Principio de Inversión de Dependencias. `MainActivity` no sabe ni le importa si los datos vienen de una base de datos o de un archivo de texto; solo habla con esta interfaz.
7. `SqliteExpenseRepository.java` (Implementación Relacional)
Responsabilidad: Implementa `ExpenseRepository` usando consultas SQL parametrizadas y `ContentValues` (previniendo inyección SQL).
Detalle clave: En `getByMonth`, filtra por `user_id=? AND date LIKE 'yyyy-MM-%'` y ordena descendentemente (`ORDER BY id DESC`) para mostrar primero los gastos más recientes.
8. `CsvExpenseRepository.java` (Implementación en Archivo Plano)
Responsabilidad: Implementa `ExpenseRepository` escribiendo y leyendo un archivo `gastos.csv` en el almacenamiento interno privado (`getFilesDir()`).
Formato: `id,userId,fecha,monto,categoria,concepto`.
Funcionamiento técnico:
Sanitiza entradas (`clean()`) reemplazando comas y saltos de línea por espacios para no romper la estructura del CSV.
Para actualizar o eliminar, lee todo el archivo en memoria (`readAll`), modifica la lista y sobrescribe el archivo completo (`writeAll`).
Calcula el siguiente `id` autoincremental buscando el máximo ID existente + 1, e invierte la lista (`Collections.reverse`) al consultar para mantener el orden cronológico inverso igual que SQLite.
---
🖥️ Módulo D: Capa de Presentación (Activities)
9. `BaseActivity.java` (Clase Padre Abstracta)
Responsabilidad: Evita la duplicación de código en las pantallas mediante herencia.
Funcionamiento: Antes de renderizar la vista (`super.onCreate`), lee `Prefs` y aplica globalmente el tema claro/oscuro (`AppCompatDelegate.setDefaultNightMode`). Además, provee `applyColor(Button...)` para teñir dinámicamente la barra superior (`ActionBar`) y los botones con el color elegido por el usuario.
Interconexión: Todas las demás Activities (`Login`, `Register`, `Main`, `Settings`) heredan de ella.
10. `LoginActivity.java` (Punto de Entrada y Autenticación)
Responsabilidad: Pantalla inicial de la app.
Flujo:
Verifica si ya existe una sesión (`prefs.getUserId() != -1`). Si existe, redirige directo a `MainActivity` y cierra el login (`finish()`).
Si no hay sesión, valida credenciales contra `DbHelper.login()`. Si son correctas, guarda la sesión en `Prefs` y abre `MainActivity`.
11. `RegisterActivity.java` (Alta de Usuarios)
Responsabilidad: Permite crear nuevas cuentas de usuario.
Validaciones: Comprueba campos vacíos, verifica que ambas contraseñas coincidan y delega a `DbHelper.registerUser()` la inserción (detectando si el nombre de usuario ya está ocupado). Al terminar, ejecuta `finish()` para volver a `LoginActivity`.
12. `MainActivity.java` (Núcleo Operativo)
Responsabilidad: Pantalla principal donde se gestionan los gastos del mes en curso.
Interconexión clave:
En `onResume()`, reaplica colores y llama a `loadExpenses()`, lo que garantiza que cualquier cambio hecho en `SettingsActivity` (color o cambio de SQLite a CSV) se refleje inmediatamente al regresar.
El método `repo()` actúa como una fábrica dinámica (Factory) que instancia `CsvExpenseRepository` o `SqliteExpenseRepository` según lo guardado en `Prefs`.
Maneja creación (formulario superior + `NotificationHelper`), lectura (cálculo de suma mensual y `ListView`) y edición/borrado (`AlertDialog` al tocar un elemento de la lista).
13. `SettingsActivity.java` (Personalización y Sesión)
Responsabilidad: Permite configurar la experiencia de usuario.
Controles:
Switch Modo Oscuro: Actualiza `Prefs` y recrea la actividad con el nuevo tema.
Selector de Color: Genera dinámicamente 5 botones de colores que actualizan `Prefs` y repintan la interfaz al instante.
RadioGroup Almacenamiento: Permite alternar entre SQLite y CSV en `Prefs`.
Cerrar Sesión: Borra los datos de usuario en `Prefs` y lanza `LoginActivity` con los flags `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK`, limpiando el historial de pantallas para impedir que el usuario regrese con el botón "Atrás".
---
3. Diagrama de Flujo e Interconexión
```text
[ LoginActivity ] <---> [ RegisterActivity ]
       │                         │
       ▼ (Valida usuario)        ▼ (Registra SHA-256)
  [ DbHelper ] <─────────────────┘
       │
       ▼ (Guarda user_id en Prefs)
[ MainActivity ] ──────────────────────────> [ SettingsActivity ]
  │    │    │                                       │
  │    │    └──> (Al agregar gasto) ──> [ NotificationHelper ]
  │    │                                            │
  │    └──> (Consulta categorías) ────> [ Categories ]
  │                                                 │
  ▼ (Método repo() decide según Prefs) <────────────┘ (Cambia SQLite/CSV, Color, Tema)
[ <<Interface>> ExpenseRepository ]
       ├───> [ SqliteExpenseRepository ] ───> [ DbHelper (Tabla expenses) ]
       └───> [ CsvExpenseRepository ]    ───> [ Archivo interno gastos.csv ]
```
Resumen del Ciclo de Vida de una Interacción Típica
El usuario inicia sesión en `LoginActivity` $\rightarrow$ `DbHelper` verifica el hash SHA-256 $\rightarrow$ `Prefs` guarda el ID de sesión.
Se abre `MainActivity`, la cual hereda el tema y color de `BaseActivity`.
Al agregar un gasto, `MainActivity` lee la categoría de `Categories`, consulta a `Prefs` qué repositorio usar mediante `repo()`, guarda el registro mediante `ExpenseRepository` (en SQLite o CSV) y dispara una alerta visual con `NotificationHelper`.
Si el usuario entra a `SettingsActivity` y cambia el almacenamiento de SQLite a CSV, al volver atrás se ejecuta `onResume()` en `MainActivity`, recargando automáticamente la lista desde el archivo `gastos.csv`.
