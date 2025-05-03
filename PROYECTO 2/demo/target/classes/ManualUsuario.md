## UNIVERSIDAD DE SAN CARLOS DE GUATEMALA
### FACULTAD DE INGENIERÍA
#### LENGUAJES FORMALES Y DE PROGRAMACIÓN
#### CATEDRÁTICO: Ing. Zulma Karina Aguirre Ordoñez
#### TUTOR ACADÉMICO: Jonatan Leonel García Arana

#### Angel José Cardenas Pocón
#### CARNÉ: 202304063
#### SECCIÓN: B-

#### Guatemala, 3 de mayo del 2025




## Manual de Usuario - Generador Visual de Mapas Narrativos 

## 1. Introducción
El presente proyecto tiene como finalidad la implementación de un sistema analizador léxico y sintáctico capaz de procesar archivos .txt que contienen la definición estructurada de mundos virtuales compuestos por lugares, conexiones y objetos. El sistema no solo valida la correcta sintaxis del archivo de entrada, sino que también proporciona una interfaz gráfica intuitiva que permite visualizar los errores encontrados y generar un mapa dinámico del mundo especificado, utilizando Graphviz como herramienta de renderizado visual

## 2. Objetivos del Sistema

### **Objetivo General**
Desarrollar una aplicación en Java que realice el análisis léxico y sintáctico de archivos .txt, permitiendo detectar errores en su estructura y visualizando gráficamente la representación del mundo descrito en dichos archivos.

### **Objetivos Específicos**
1. Implementar un analizador léxico que reconozca y clasifique tokens válidos de un archivo .txt, como palabras reservadas, identificadores, símbolos, cadenas y separadores.

2. Incorporar un analizador sintáctico que valide la estructura jerárquica y gramatical de los mundos definidos.

3. Diseñar una interfaz gráfica con Java Swing que permita al usuario cargar archivos, visualizar los resultados del análisis, mostrar errores y renderizar gráficamente los mundos.



## 3. Información del Sistema

### **3.1. Requisitos Previos**
- Tener instalado **Java (JDK 8 o superior)** en el sistema.
- Disponer de un archivo `.txt` con la información de los personajes.

### **3.2. Instalación**
1. Descargar el repositorio y abrirlo
2. Compilar el código 
3. Ejecutar el programa 

---

## 4. Flujo del sistema

# Flujo del Sistema

## Carga del Archivo:
1. El usuario selecciona un archivo .lfp mediante el botón correspondiente en la interfaz. El contenido del archivo es leído y enviado al analizador.

## Análisis Léxico:
2. Se identifican y clasifican los tokens válidos (palabras reservadas, cadenas, coordenadas, símbolos, etc.) y se registran errores léxicos por símbolos inválidos o faltantes.

## Análisis Sintáctico:
3. A partir de los tokens válidos, se realiza el análisis descendente del archivo, verificando la correcta estructura de definición de mundos, lugares, conexiones y objetos. Se detectan errores como llaves faltantes, comandos mal estructurados o secuencias incorrectas.

## Visualización de Errores:
4. Si existen errores, se muestran en una tabla detallada con tipo, línea y columna. Si no hay errores, la tabla permanece vacía.

## Carga de Mundos:
5. Si el archivo contiene varios mundos, se habilita un ComboBox para seleccionar cuál mundo se desea visualizar.

## Generación del Mapa:
6. Al presionar el botón de graficar, se genera el archivo .dot correspondiente y se usa Graphviz para renderizar una imagen del mapa. Los nodos y aristas se personalizan según las tablas predefinidas de tipos de lugares, objetos y conexiones.

## Visualización Gráfica:
7. La imagen del mundo seleccionado se muestra en un panel gráfico, al lado de la tabla de errores. 

## Cierre del programa  
8. El usuario puede cerrar la aplicación cuando haya terminado su análisis.  
