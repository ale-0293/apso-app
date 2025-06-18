# APSO App - Aplicación de Sorteo de Estudiantes

**APSO** (Aplicación de Sorteo) es una plataforma web desarrollada con **Spring Boot** y **Thymeleaf**, orientada a facilitar la gestión de listas de estudiantes y la asignación aleatoria en grupos de trabajo. La autenticación se gestiona mediante **Auth0**, permitiendo roles diferenciados y operaciones CRUD seguras.

---

## ✨ Características principales

- Gestión de listas de estudiantes por usuario autenticado
- Carga masiva de estudiantes mediante archivos CSV
- Sorteo dinámico de grupos
- CRUD completo para estudiantes y listas
- Roles de acceso diferenciados (`admin`, `user`, `guest`)
- Autenticación segura con Auth0 (correo electrónico y Google)
- Despliegue en la nube con Render
- Base de datos PostgreSQL gestionada desde [Neon.tech](https://neon.tech)

---

## 📁 Estructura del Proyecto
```bash
apso-app/
├── src/
│ └── main/
│ ├── java/com/apso/app/
│ │ ├── controller/
│ │ ├── service/
│ │ ├── repository/
│ │ └── model/
│ └── resources/
│ ├── templates/
│ ├── static/
│ └── application.properties
├── recursos/
│ └── ejemplo-estudiantes.csv
├── README.md
├── pom.xml
```
---

## 🚀 Despliegue rápido

1. Sube el proyecto a un repositorio en GitHub.
2. Ingresa a [Render.com](https://render.com/) y crea un nuevo servicio de tipo **Web Service**.
3. Conecta tu repositorio y configura:
   - **Build Command:** `./mvnw clean install`
   - **Start Command:** `java -jar target/*.jar`
   - **Environment:** `JAVA_VERSION = 17`
4. Establece variables de entorno necesarias (como las credenciales de Auth0 y la URL de la base de datos Neon).
5. Render se encargará del resto 🚀

Si quieres visitar nuestro despliegue ve al siguiente enlace: https://apso-appbackend.onrender.com/

---

## 📄 Carga de estudiantes vía CSV

Puedes cargar estudiantes desde la interfaz web accediendo a:

Ruta: /cargacsv

El archivo CSV debe tener el siguiente formato:
```bash
nombre,email,grupo_teorico,asignatura,carga_id
Andrea Lopez,andrea.lopez@ues.edu.sv,GT01,Fisica I,202501
Carlos Rivera,carlos.rivera@ues.edu.sv,GT02,Quimica General,202502
```

Nota: El archivo ejemplo: estudiantes.csv se encuentra en la carpeta recursos/ en la raíz del proyecto.
El campo carga_id debe ser un número entero con el formato 2025##.

---

## 🛠️ Tecnologías utilizadas

- Java 17
- Spring Boot 3
- Spring Security + Auth0
- Thymeleaf
- PostgreSQL + Neon.tech
- Render.com
- Apache Commons CSV
- Maven

---

## 📌 Consideraciones adicionales

El sistema está preparado para desplegarse sin login para invitados, pero con autenticación obligatoria para funcionalidades clave.