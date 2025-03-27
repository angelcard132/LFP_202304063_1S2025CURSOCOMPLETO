## UNIVERSIDAD DE SAN CARLOS DE GUATEMALA
### FACULTAD DE INGENIERÍA
#### LENGUAJES FORMALES Y DE PROGRAMACIÓN
#### CATEDRÁTICO: Ing. Zulma Karina Aguirre Ordoñez
#### TUTOR ACADÉMICO: Jonatan Leonel García Arana

#### Angel José Cardenas Pocón
#### CARNÉ: 202304063
#### SECCIÓN: B-

#### Guatemala, 26 de marzo del 2025




## Manual de Usuario

## 1. Introducción
 Este software ha sido diseñado para facilitar la construcción, análisis y visualización de AFD, permitiendo a los usuarios cargar definiciones formales, generar representaciones gráficas y obtener reportes detallados sobre el análisis léxico de cadenas de entrada.

## 2. Objetivos del Sistema

### **Objetivo General**
Desarrollar una herramienta computacional que permita la creación, análisis y visualización de Autómatas Finitos Deterministas (AFD), facilitando la comprensión de su funcionamiento mediante una interfaz gráfica interactiva y la generación de reportes detallados sobre el análisis léxico de cadenas de entrada.

### **Objetivos Específicos**
1. Implementar un analizador léxico que procese archivos en formato .lfp, almacenando la definición del AFD en una estructura de datos eficiente y permitiendo la detección de tokens válidos y errores léxicos.

2. Desarrollar una interfaz gráfica en Java Swing que represente visualmente los autómatas generados, permitiendo a los usuarios interactuar con ellos y ejecutar pruebas con diferentes cadenas de entrada.



## 3. Información del Sistema

### **3.1. Requisitos Previos**
- Tener instalado **Java (JDK 8 o superior)** en el sistema.
- Disponer de un archivo `.lfp` con la información de los personajes.

### **3.2. Instalación**
1. Descargar el repositorio y abrirlo
2. Compilar el código 
3. Ejecutar el programa 

---

## 4. Flujo del sistema

# Flujo del Sistema

## 1. Ejecución del programa  
- El usuario abre la aplicación, iniciando la interfaz gráfica desarrollada en **Java Swing**.  

## 2. Carga del archivo .lfp  
- El usuario selecciona un archivo con la definición del **Autómata Finito Determinista (AFD)**.  

## 3. Análisis léxico  
- El usuario presiona el botón **"Graficar"** para analizar la definición del autómata.  
- El sistema verifica la validez del archivo **.lfp** y revisa la estructura del AFD.  

## 4. Verificación de errores  
### - **Si no hay errores léxicos:**  
  - Se genera el autómata finito determinista y se muestra en la interfaz gráfica.  
### - **Si hay errores léxicos:**  
  - Se despliega una tabla detallando los errores encontrados en el archivo **.lfp**, indicando su ubicación y descripción.  

## 5. Interacción con el autómata  
- Si el AFD es válido, el usuario puede probar distintas cadenas de entrada para verificar su aceptación o rechazo.  
- Se generan reportes de tokens y errores, según los resultados obtenidos.  

## 6. Cierre del programa  
- El usuario puede cerrar la aplicación cuando haya terminado su análisis.  
