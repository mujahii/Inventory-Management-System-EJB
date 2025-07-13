# Inventory Management System

A modern, full-featured Inventory Management System built with Java EE, JSF (Jakarta Server Faces), PrimeFaces, EJB, JPA, and MySQL. This project provides a robust platform for managing inventory, users, and administrative tasks with a clean, professional, and responsive user interface.

---

## Features

### User Management
- **Registration & Login:** Secure authentication for both users and administrators.
- **Role-Based Access:** Distinct dashboards and permissions for regular users and admins.
- **Profile Sidebar:** Users can view their profile, email, and quick stats on their items and low stock.

### Inventory Management
- **CRUD Operations:** Add, edit, delete, and view inventory items.
- **Item Details:** Track item name, category, quantity, price, location, and description.
- **Stock Status:** Visual indicators for low, medium, and high stock levels.
- **Search & Filter:** Quickly find items by keyword, category, or status.
- **Responsive Item Grid:** Modern card-based layout for item display.

### Admin Panel
- **System Overview:** Dashboard with real-time statistics (total users, active users, total items, system uptime).
- **User Management:** View, filter, and delete users. Role badges for admin and regular users.
- **User Details Dialog:** View detailed user information in a modal dialog.
- **Confirmation Dialogs:** Safe deletion with confirmation prompts.

### UI/UX
- **Modern Design:** Consistent, attractive look using PrimeFaces, Font Awesome icons, and custom CSS.
- **Responsive Layout:** Works well on desktops and tablets.
- **Navigation:** Sidebar for users, top navigation for admins, and dynamic section switching.
- **Feedback:** Growl notifications for actions and errors.

### Technical
- **Java EE (Jakarta EE):** Enterprise-grade backend with EJB and JPA.
- **JSF & PrimeFaces:** Component-based UI with Facelets templating.
- **MySQL Database:** Persistent storage for users and inventory.
- **RESTful APIs:** Ready for integration and future expansion.
- **Maven Build:** Easy dependency management and deployment.

---

## Getting Started

1. **Clone the repository.**
2. **Configure your database connection** in `src/main/resources/META-INF/persistence.xml`.
3. **Build and deploy** the project on a Java EE-compatible server (e.g., GlassFish, Payara).
4. **Access the application** in your browser at the deployed URL.

---

## Folder Structure

- `src/main/webapp/` — JSF pages (`.xhtml`), CSS, and static resources
- `src/main/java/com/inventory/` — Java source code (beans, EJBs, DAOs, DTOs, entities)
- `src/main/resources/META-INF/` — Persistence configuration
- `pom.xml` — Maven project file

---

## Screenshots

- Modern login, registration, and dashboard pages
- Admin panel with user and inventory statistics
- Item management with CRUD operations and visual feedback

---

## License

This project is open-source and available under the MIT License.
