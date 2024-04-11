# INFORMÁTICA III - 2024 - PROYECTO II - AFN

## Objetivo

Este proyecto tiene como finalidad la aplicación de la teoría de Autómatas Finitos No Determinísticos (AFN) con representación matricial, reconocimiento de cuerdas y conversión de AFN a AFD. El desarrollo se hará completamente en Java, siguiendo las especificaciones detalladas en este documento.

## Integración del Equipo

El proyecto es obligatorio y debe ser presentado en grupos de máximo 3 integrantes. Es imperativo que el grupo del Proyecto No. 2 sea el mismo para el Proyecto No. 3, dado que ambos proyectos están interconectados.

## Especificaciones de Implementación

- La implementación debe hacerse en la clase `AFN.java`.
- Los métodos a implementar incluyen:
  1. Constructor que acepte la ruta del archivo con la definición del AFN.
  2. Método `boolean accept(String cuerda)` que determine si una cadena es aceptada.
  3. Método `main` para ejecución interactiva y procesamiento del AFN.
  4. Método `toAFD(String afdPath)` para convertir el AFN a AFD y guardar el resultado.

## Modo de Ejecución

- Para ejecutar y probar el AFN: `$ java AFN nombre_del_afn`
- Para convertir el AFN a AFD: `$ java AFN nombre_del_afn -to-afd PATH_DEL_ARCHIVO_DE_SALIDA`

## Formato de Archivo de Entrada

- El archivo `.afn` incluirá:
  - Símbolos terminales en la primera línea, separados por comas.
  - Cantidad de estados y estados finales en las siguientes líneas.
  - Matriz de transición con transiciones para cada símbolo, incluido lambda.

## Entrega y Evaluación

- Fecha de entrega: 14 de abril de 2024.
- Método de entrega: A través de GES, subir una carpeta comprimida `.zip` con los archivos `.java`.
- Importante: Asegurarse de que el código compila correctamente.

## Indicaciones Adicionales

- No se permiten tildes en las variables ni en los comentarios.
- Se valorará la capacidad de realizar pruebas autónomas y verificar la correctitud del AFN y su conversión a AFD.
