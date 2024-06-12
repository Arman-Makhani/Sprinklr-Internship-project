# Dependency Graph Visualizer

## Table of Contents
1. [Introduction](#introduction)
2. [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Installation](#installation)
3. [Running the Project](#running-the-project)
4. [Project Structure](#project-structure)
5. [Usage Guide](#usage-guide)
6. [Developer Guide](#developer-guide)
    - [Prerequisites for Development](#prerequisites-for-development)
    - [Understanding the Code](#understanding-the-code)
    - [Extending the Project](#extending-the-project)
7. [FAQ](#faq)


## Introduction
The Dependency Graph Visualizer is a tool designed to convert Gradle dependency logs into interactive and visual dependency graphs. It helps users understand their project's dependencies, identify issues such as unresolved dependencies, detect circular dependencies , and helps them refactor their code.

## Getting Started

### Prerequisites
Before you begin, ensure you have the following installed:

- **Java Development Kit (JDK)**: Ensure you have JDK 20 or later installed. You can download it from [Oracle's website](https://www.oracle.com/java/technologies/downloads/). To check is if it is correctly installed run the command "java --version" on your terminal.
- **Graphviz**: For rendering graphs. Install Graphviz from [Graphviz's official website](https://graphviz.org/download/) and run the command "dot -V" to check.
- **Spring Boot**: Ensure you have Spring Boot installed. Spring Boot simplifies the creation of stand-alone, production-grade Spring-based applications.
- **Gradle**: Make sure you have Gradle installed to handle dependencies and builds. Install it from [Gradle's official website](https://gradle.org/install/) and run the command "gradle -v" to check 

### Installation

1. **Clone the Repository**:
    ```bash
    git clone https://github.com/Arman-Makhani/Sprinklr-Internship-project.git
    cd dependency-graph-visualizer
    ```

2. **Build the Project**:
    ```bash
    ./gradlew build
    ```

3. **Install Graphviz**:
    Follow the instructions for your operating system from [Graphviz's official website](https://graphviz.org/download/).

## Running the Project

1. **Start the Application**:
    ```bash
    ./gradlew bootRun
    ```
    The application will be accessible at `http://localhost:8080`.

2. **Access the Application**:
    Open your browser and navigate to `http://localhost:8080`. You should see the upload page for your dependency logs.

3. **Upload a Dependency Log**:
    Upload your Gradle dependency log file to generate the dependency graph.

## Project Structure

The project follows a standard Gradle structure :


- **`controller`**: Contains the Spring Boot controller for handling web requests. (src/main/java/com.example)
- **`service`**: Contains the service logic for generating graphs. (src/main/java/com.example)
- **`utils`**: Contains utility classes, including the dependency parser. (src/main/java/com.example)
- **`templates`**: Thymeleaf templates for the web application. (src/main/resources)
- **`static`**: Contains static files such as JavaScript and CSS. (src/main/resources)

## Usage Guide

1. **Upload Your Dependency Log**:
   - Navigate to the upload page (`/`).
   - Choose a Gradle dependency log file and click "Generate Graph".

2. **View the Graph**:
   - The result page will show an SVG representation of your dependency graph.
   - Hover over nodes to view detailed information about each dependency.

3. **Search Dependencies**:
   - Use the search bar on the result page to find specific dependencies within the graph.

4. **Highlight Circular Dependencies**:
   - Click the "Show Circular Dependencies" button to highlight any circular dependencies in the graph.

5. **Export Graph**:
   - Click the "Download PNG" button to download the graph as a PNG file.

## Developer Guide

### Prerequisites for Development
- **Java Development Kit (JDK)**: JDK 20 or later.
- **Graphviz**: Installed and configured on your system.
- **IDE**: IntelliJ IDEA or Eclipse for Java development.
- **Gradle**: For managing dependencies and builds.

### Understanding the Code

- **GraphController.java**:
  - Handles HTTP requests for generating and displaying the dependency graph.
  - Uses `GraphService` to perform backend operations.

- **GraphService.java**:
  - Generates the dependency graph asynchronously using `Graphviz`.
  - Contains logic for detecting circular dependencies and handling SVG generation.

- **DependencyParser.java**:
  - Parses the Gradle dependency log file into structured data.
  - Splits dependencies into manageable chunks for processing.

### Extending the Project

1. **Adding New Features**:
   - Make the nodes clickable , so that user can see only the necessary paths. If there are multiple paths , a side panel will open from which user can choose their own path.
   - Improving the visualization for huge dependency files.

2. **Improving the UI**:
   
3. Deploying in Sprinklr's QA instance

### Logic and Explanation

- **Asynchronous Processing**: The project uses asynchronous processing to handle large dependency graphs without blocking the main thread. This is implemented using the `@Async` annotation in `GraphService`.
- **Graphviz Integration**: The project integrates with Graphviz to generate dependency graphs in SVG format. This allows for clear visualization and easy manipulation of graph data.
- **Dependency Parsing**: The `DependencyParser` class reads and parses the dependency logs. It handles large files by processing them in chunks to avoid memory overload and improve performance.

## FAQ

**Q1**: How do I upload my dependency log file?
- Navigate to the home page, select your Gradle dependency log file, and click "Generate Graph".

**Q2**: What do the colors in the graph mean?
- Red (n): Not resolved dependencies.
- Green (c): Constraints.
- Yellow (*): Omitted or previously used dependencies.

**Q3**: How can I highlight circular dependencies?
- Click the "Show Circular Dependencies" button on the result page.



