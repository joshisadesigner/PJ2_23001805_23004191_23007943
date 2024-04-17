### 1. Método `printMagenta`
- **Recibe:**
    - `boolean newLine`: Si se debe agregar una nueva línea después de imprimir los mensajes.
    - `String... messages`: Varargs de tipo `String` que contienen los mensajes a imprimir.
- **Estructuras:**
    - No utiliza estructuras de datos complejas; simplemente formatea y muestra los mensajes en consola.
- **Devuelve:**
    - `void`: Este método no devuelve ningún valor, su propósito es la salida en consola.

### 2. Constructor `AFN`
- **Recibe:**
    - `String path`: La ruta al archivo que contiene la definición del AFN.
- **Estructuras:**
    - Usa métodos de la clase para cargar datos del archivo, no mantiene por sí mismo estructuras adicionales.
- **Devuelve:**
    - No es aplicable ya que es un constructor. Inicializa la instancia de la clase.

### 3. Método `loadAFN`
- **Recibe:**
    - `String path`: Ruta al archivo de configuración del AFN.
- **Estructuras:**
    - `Scanner` para leer el archivo.
    - Listas y matrices de listas (`List<List<List<List<String>>>> transitions`) para almacenar las transiciones de forma que cada estado y símbolo puedan tener múltiples destinos posibles.
- **Devuelve:**
    - `void`: Carga los datos en las estructuras de la clase sin devolver un valor.

### 4. Método `printTransitionsTable`
- **Recibe:**
    - `int maxSymbols`: Número total de símbolos (incluyendo lambda).
    - `int maxStates`: Número total de estados.
- **Estructuras:**
    - Utiliza iteraciones sobre la lista de transiciones para formatear y mostrar una tabla en consola.
- **Devuelve:**
    - `void`: Solo imprime la tabla en consola.

### 5. Método `accept`
- **Recibe:**
    - `String string`: La cadena de entrada a evaluar.
- **Estructuras:**
    - `Set<Integer>` para representar conjuntos de estados actuales y futuros.
    - Mapas para relacionar caracteres con índices de símbolos.
- **Devuelve:**
    - `boolean`: Verdadero si la cadena es aceptada por el autómata, falso en caso contrario.

### 6. Método `applyLambdaTransitions`
- **Recibe:**
    - `Set<Integer> currentStates`: Conjunto de estados desde los cuales aplicar transiciones lambda.
- **Estructuras:**
    - `Set<Integer>` para manejar y actualizar los estados alcanzables con transiciones lambda.
- **Devuelve:**
    - `Set<Integer>`: Un nuevo conjunto de estados que incluye todos los alcanzables mediante transiciones lambda desde los estados dados.

### 7. Método `toAFD`
- **Recibe:**
    - `String afdPath`: Ruta del archivo donde se guardará la configuración del AFD generado.
- **Estructuras:**
    - `Map<Set<Integer>, Integer>` para mapear conjuntos de estados del AFN a estados únicos del AFD.
    - `List<Set<Integer>>` y `List<List<Integer>>` para manejar estados y transiciones del AFD.
    - `Queue<Set<Integer>>` para procesar estados durante la conversión.
- **Devuelve:**
    - `void`: Escribe la configuración del AFD a un archivo.

### 8. Método `main`
- **Recibe:**
    - `String[] args`: Argumentos de línea de comandos que indican la operación a realizar y los archivos involucrados.
- **Estructuras:**
    - Utiliza condicionales para manejar diferentes modos de operación (evaluación o conversión).
    - `BufferedReader` para leer la entrada del usuario en modo de evaluación.
- **Devuelve:**
    - `void`: Dependiendo de los argumentos, carga un AFN, evalúa cadenas, o convierte un AFN a AFD, con la salida adecuada en consola o archivos.
