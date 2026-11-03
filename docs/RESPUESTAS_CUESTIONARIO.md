# Cuestionario de Análisis Conceptual y Arquitectura Distribuida

**Taller: Integración de JavaScript Vainilla con ZeroC Ice sobre WebSockets Bidireccionales**
Computación en Internet I (09776) — Semestre 2026-2
Estudiante: Felipe Cuadros Muñoz — Código: A00410604

---

## 1. Conexiones Bidireccionales (BiDir) vs. Callbacks Tradicionales

En el modelo de callbacks clásico, que es el que usa el cliente JavaFX de este proyecto, el
cliente crea su propio `ObjectAdapter` con endpoints reales:

```java
callbackAdapter = communicator.createObjectAdapterWithEndpoints(
        "CallbackAdapter-" + UUID.randomUUID().toString().substring(0, 8), "default");
```

Ese `"default"` abre un puerto TCP dinámico en la máquina del cliente. Cuando el servidor
necesita notificar un cambio de estado, **abre una conexión nueva en sentido inverso** hacia
ese puerto. El esquema funciona porque el cliente de escritorio puede aceptar conexiones
entrantes.

Un cliente que corre dentro del navegador no puede hacer eso. El sandbox de la plataforma web
no expone sockets de escucha: JavaScript puede iniciar conexiones salientes, pero no puede
ponerse a escuchar en un puerto. Por eso el único adaptador posible es uno **sin endpoints**:

```js
this.adapter = await communicator.createObjectAdapter("");
```

Ese adaptador sí puede alojar servants y despachar peticiones, pero es inalcanzable desde
fuera porque no tiene dirección. El puente se construye en la línea siguiente:

```js
const connection = await servicePrx.ice_getConnection();
connection.setAdapter(this.adapter);
```

`setAdapter` le dice a la conexión WebSocket **que el cliente ya abrió** hacia el servidor:
"las peticiones que lleguen por ti, despáchalas contra este adaptador local". Como las
conexiones de Ice son full-duplex, el servidor puede emitir peticiones por el mismo socket en
dirección contraria. Una sola conexión transporta entonces el RPC de ida (`makeMove`,
`joinRoom`) y los callbacks de vuelta (`onGameStateChanged`). Esto es lo que se conoce como
conexión bidireccional o BiDir.

Del lado del servidor Java, el proxy que llega a `registerObserver` trae la identidad del
observador pero **ningún endpoint**, porque el adaptador que lo creó no tiene ninguno. Si el
servidor intentara invocarlo tal cual, Ice no tendría a dónde conectarse y la invocación
fallaría con `NoEndpointException`. De ahí el requerimiento del Paso 2:

```java
if (target != null && target.ice_getEndpoints().length == 0 && current.con != null) {
    target = target.ice_fixed(current.con);
}
```

`ice_fixed(Connection)` devuelve un **fixed proxy**: un proxy amarrado a ese objeto
`Connection` concreto, cuyas invocaciones se escriben directamente sobre esa conexión sin
pasar por la resolución de endpoints ni por un locator. Y `current.con` es, precisamente, la
conexión entrante del despacho actual, es decir el WebSocket del navegador que acaba de
llamar a `registerObserver`.

Un fixed proxy tiene propiedades que encajan bien con el caso: no es enrutable, no se puede
serializar a string con `ice_toString`, y deja de ser válido cuando la conexión muere. Eso
último es la semántica correcta para una pestaña del navegador, que el usuario puede cerrar
en cualquier momento.

La validación de endpoints vacíos no es opcional, porque el mismo método `registerObserver`
atiende a los dos tipos de cliente. El cliente JavaFX manda un proxy con endpoints y debe
registrarse tal cual, para que el servidor pueda reconectarse a él si hace falta. Aplicar
`ice_fixed` indiscriminadamente lo amarraría también a su conexión actual y le quitaría esa
capacidad sin necesidad.

---

## 2. Transporte TCP vs. WebSockets en ZeroC Ice

El servidor no duplica nada porque hay **un solo adaptador con dos endpoints**, declarados en
la misma cadena y separados por `:` en `ChessServerApp`:

```java
String endpoint = String.format("default -p %d:ws -h 0.0.0.0 -p %d", port, wsPort);
ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("ChessServerAdapter", endpoint);
adapter.add(servant, Util.stringToIdentity("ChessService"));
```

El servant se registra **una sola vez**, en el Active Servant Map de ese adaptador, bajo la
identidad `ChessService`.

Ice separa el transporte del pipeline de despacho. Cada endpoint tiene su propio acceptor y su
propio transceiver, que es la pieza que sabe leer y escribir bytes sobre ese medio concreto.
Pero una vez la petición se lee del socket, deja de importar por dónde entró: se convierte en
un despacho que se resuelve por *identidad + operación* contra el mismo ASM y se ejecuta en el
mismo pool de hilos del servidor, configurado en este proyecto con
`Ice.ThreadPool.Server.Size`.

Además, el protocolo de aplicación es idéntico en los dos casos. `ws` no es un protocolo
distinto: es el **mismo protocolo binario de Ice encapsulado dentro de frames WebSocket
(RFC 6455)**. Es una capa de transporte delgada que añade el handshake HTTP inicial y el
framing, no una traducción de la semántica de las invocaciones. Los parámetros se serializan
con el mismo encoding 1.1 y las excepciones de usuario viajan igual.

El resultado es que `ChessServiceImpl` no sabe —ni necesita saber— por qué transporte le llegó
cada llamada. El único punto de todo el servant donde el transporte se asoma es justamente el
`current.con` del Paso 2, y aparece ahí porque el problema que resuelve es inherentemente de
transporte.

Hay una consecuencia que conviene hacer explícita: como ambos endpoints comparten el mismo
pool de hilos y el mismo servant, dos clientes pueden invocar `makeMove` **concurrentemente**
desde TCP y desde WebSocket. Por eso `GameRoom` tiene que ser thread-safe y por eso
`GameState` incluye un campo `version`, que permite detectar y ordenar actualizaciones.

---

## 3. Empaquetado del Contrato con `slice2js`

El archivo que genera `slice2js` no es un módulo ES ni un módulo CommonJS: es una función
anónima invocada inmediatamente (IIFE) que recibe tres argumentos elegidos por un ternario al
final del propio archivo.

```js
(function(module, require, exports)
{
    const Ice = require("ice").Ice;
    const Slice = Ice.Slice;
    let ChessApp = Ice._ModuleRegistry.module("ChessApp");
    // ... definicion de enums, structs, excepciones y proxies ...
    exports.ChessApp = ChessApp;
}
(typeof(global) !== "undefined" && typeof(global.process) !== "undefined" ? module : undefined,
 typeof(global) !== "undefined" && typeof(global.process) !== "undefined" ? require : window.Ice._require,
 typeof(global) !== "undefined" && typeof(global.process) !== "undefined" ? exports : window));
```

En Node, donde `global.process` existe, recibe el `module`, `require` y `exports` de CommonJS.
En el navegador, donde no existe, recibe `undefined`, `window.Ice._require` y `window`. Por eso
la última línea del cuerpo, `exports.ChessApp = ChessApp`, es literalmente
`window.ChessApp = ChessApp`. Ese es todo el mecanismo de registro del espacio de nombres
global: el mismo archivo sirve para los dos entornos sin empaquetador.

El namespace en sí no se crea con un objeto literal sino con
`Ice._ModuleRegistry.module("ChessApp")`, un registro que devuelve el módulo si ya existe y lo
crea si no. Eso permite que varios archivos `.ice` compilados por separado aporten tipos al
mismo módulo sin pisarse entre ellos.

`Ice.min.js` tiene que cargarse primero por dos razones, una inmediata y una de fondo.

La inmediata es que la primera línea del cuerpo es `const Ice = require("ice").Ice`, donde ese
`require` **es** `window.Ice._require`. Si el runtime no se ha evaluado todavía, `window.Ice`
es `undefined` y el archivo revienta con `Cannot read properties of undefined (reading
'_require')` antes de definir un solo tipo.

La de fondo es que el archivo generado contiene únicamente *metadatos*, no maquinaria. Los
tipos se construyen llamando a `Slice.defineEnum`, `Slice.defineStruct`,
`Slice.defineException` y `Slice.defineOperations`, todas funciones que viven en `Ice.Slice`;
los proxies heredan de `Ice.ObjectPrx` y los servants de `Ice.Object`; y la serialización usa
los `InputStream` / `OutputStream` del runtime. El contrato describe *qué* hay que serializar,
el runtime sabe *cómo*. Sin runtime, el contrato no tiene sobre qué apoyarse.

En este proyecto el orden lo garantizan los imports estáticos al inicio de
`chessClientService.js`:

```js
import "../../lib/Ice.min.js";
import "./ChessService.js";
```

Los módulos ES se evalúan en profundidad y en orden de declaración, de modo que `Ice.min.js`
termina de ejecutarse —y de publicar `window.Ice`— antes de que empiece `ChessService.js`.
Invertir esas dos líneas rompe la aplicación.

---

## 4. Validación Autoritativa

El servidor revalida cada movimiento en `GameRoom.makeMove` por dos motivos independientes, y
cada uno por separado ya sería suficiente.

**El cálculo del cliente es deliberadamente incompleto.** La función
`calculateLocalCandidateMoves` de `chessRulesHelper.js` resuelve geometría de
pseudo-legalidad: sabe cómo se desplaza cada tipo de pieza, que las piezas deslizantes no
atraviesan obstáculos y que no se captura pieza propia. Pero no verifica si el movimiento deja
al rey propio en jaque, con lo cual permite mover una pieza clavada; no implementa captura al
paso, enroque ni coronación; y no comprueba si la partida ya terminó. Eso no es un defecto: su
trabajo es dar **feedback visual inmediato sin esperar un viaje de red**, para que el tablero
responda al instante cuando el usuario selecciona una pieza. El motor completo, con detección
de jaque, mate y ahogado, vive en `ChessRules` y es el que importa.

**El cliente no es confiable.** El código JavaScript que corre en el navegador está
íntegramente bajo el control del usuario: puede editarse desde las herramientas de desarrollo,
puede invocarse `makeMove` directamente desde la consola saltándose la interfaz, o puede
escribirse un cliente Ice propio que hable el mismo contrato Slice sin usar el frontend para
nada. Cualquier validación que exista solo en el cliente es una sugerencia, no una
restricción. Este punto aplica igual al cliente JavaFX: el hecho de que sea una aplicación de
escritorio compilada no lo hace confiable.

A esos dos motivos se suma uno arquitectónico. `GameRoom` es el **único punto de verdad** del
sistema: mantiene el tablero canónico, serializa las jugadas que llegan concurrentemente por
TCP y por WebSocket, lleva el contador `version` del estado, calcula las capturas y los
cambios de puntaje, y es quien emite las notificaciones a jugadores y espectadores. Si el
servidor aceptara sin verificar lo que le manda cada cliente, dos tableros podrían divergir y
la vista de los espectadores dejaría de significar algo, porque no habría un estado común que
reflejar.

El reparto correcto de responsabilidades, entonces, es: el cliente optimiza la latencia
percibida y el servidor define la realidad. Cuando los dos discrepan, gana el servidor, y la
notificación `onGameStateChanged` que llega después de cada jugada es justamente el mecanismo
que vuelve a sincronizar al cliente con el estado autoritativo.