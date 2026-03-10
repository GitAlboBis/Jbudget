[relazioneJBudgetAlbertoTuveri123718.pdf](https://github.com/user-attachments/files/22345791/relazioneJBudgetAlbertoTuveri123718.pdf)
# JBudget - Gestione Budget Familiare

## 📖 Overview
**JBudget** is a desktop software application developed in Java for managing and controlling family budgets. It was created as an examination project for the "Metodologie di programmazione" (Programming Methodologies) course at UNICAM. The application provides a comprehensive suite of tools to track expenses, manage loans, monitor deadlines, and view financial statistics.

## ✨ Features
* **Movement Tracking (Movimenti):** Record and monitor financial movements.
* **Budget Management:** Set up and manage family budgets using the dedicated Budget Calculation Service.
* **Deadline Management (Scadenze):** Keep track of upcoming financial deadlines with automatic status updates upon application launch.
* **Loan Management (Prestiti):** Manage loans and automatically update overdue payments.
* **Statistics & Reporting:** View insights and statistics regarding your financial data.

## 🛠️ Tech Stack & Architecture
* **Language:** Java 21.
* **UI Framework:** JavaFX 21 (using `javafx.controls` and `javafx.fxml` modules).
* **Build Tool:** Gradle.
* **Database:** H2 Database Engine (Version 2.2.224).
* **Testing:** JUnit 5 (Jupiter API 5.10.0).
* **Architecture:** The project follows an MVC-based structure with a custom `ServiceFactory` handling Dependency Injection to supply database connections and services to the UI controllers (e.g., `MainController`, `MovementController`, `BudgetController`).

## 🏗️ Application Structure

Upon startup, the application performs the following initializations:

  * **Database Setup:** Connects to the H2 database via DataSourceFactory and initializes tables via DatabaseInitializer.

  * **Dependency Injection:** Instantiates services via ServiceFactory.

  * **UI Loading:** Loads the primary MainView.fxml interface with a default resolution of 1400x900 (minimum 1000x700).

  * ** Data Sync:** Automatically checks and updates overdue "scadenze" and loan payments in the background.
