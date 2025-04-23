# FIXARO - Error handling app

FIXARO is a comprehensive web application built to streamline the process of error reporting, user management, and internal communication within organizations. Designed with scalability and usability in mind, the platform features a robust role-based access control system and a clean, intuitive interface for managing various operations.

Its standout capabilities include automated email notifications, real-time communication via chat, attachment handling for issue reports, and on-demand PDF report generation. This makes it an ideal solution for organizations looking to manage support tickets and IT errors efficiently.

---

## 🛠 Used Technologies

- Java 17  
- Spring Boot 3.0.6  
- Spring Security  
- Spring Data JPA  
- Spring Mail  
- Spring Scheduling  
- Thymeleaf  
- RabbitMQ
- HTML
- JavaScript
- Tailwind CSS
- H2 Database  
- Liquibase  
- WebSocket (for real-time chat)  
- iTextPDF (for PDF generation)  
- Maven 4.0.0  

---

## ✨ Features

### 🔐 User Management
- User registration with email verification  
- Role-based access (Admin, Employee, Standard User)  
- Password reset with email token support  

### 📝 Ticket Management
- Creating, editing, and deleting issue reports  
- Assigning reports to employees  
- Tracking issue statuses and categories  
- Uploading attachments to reports  

### 🏢 Company Management
- Admins can create, update, and delete company records  
- Association of users with specific companies  

### 🗂 Category Management
- Creation and management of issue categories for better classification  

### 💬 Real-time Chat
- Chat system built on WebSocket protocol  
- Enables users to discuss issues in real time with support staff  

### 📎 Attachment Uploading
- Users can upload files (screenshots, documents) with their reports  

### 📄 PDF Report Generation
- Generate detailed PDF summaries of individual reports or system-wide summaries  
- Includes metadata, timestamps, user information, and status tracking  

### 📧 Email Notification System
- Welcome email upon user registration  
- Password reset email with secure token link  
- All emails are queued and sent asynchronously using a scheduled job  

---

## 🚀 Getting Started

### ✅ Prerequisites

- Java 17 or higher  
- Maven 3.6+  
- Docker  

---

### 🧭 Step-by-Step Setup

### 1. Start RabbitMQ using Docker

```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 -p 61613:61613 rabbitmq:management
docker exec -it rabbitmq rabbitmq-plugins enable rabbitmq_stomp


### 2. Clone the repository
```bash
git clone https://github.com/szymon-kwiecien/error_handling_app.git
cd error_handling_app

### 3. Configure Application Properties

Edit the `src/main/resources/application.properties` file:

```properties
spring.application.name=error_handling_app
spring.datasource.url=jdbc:h2:mem:app
spring.liquibase.change-log=classpath:db/changelog/master.xml
spring.jpa.hibernate.ddl-auto=validate

spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
spring.rabbitmq.virtual-host=/

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=noreply.fixaro@gmail.com
spring.mail.password=dled eldt yvqk udlb
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

### 4. Build and Run the Application

Run the following commands in your terminal:

```bash
mvn clean install
mvn spring-boot:run

### 👥 Default Users for Testing

You can log in with the following demo credentials:

| Role          | Email                        | Password         |
| ------------- | ---------------------------- | ---------------- |
| Administrator | admin@example.com            | tajneHaslo1      |
| Regular User  | jan.kowalski@example.com      | haslo123         |
| Employee      | anna.nowak@example.com        | bezpieczneHaslo! |

