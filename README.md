# Dependency Graph Visualizer

## Table of Contents
- [Introduction](#introduction)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Running the Project](#running-the-project)
- [Usage Guide](#usage-guide)
- [Developer Guide](#developer-guide)
  - [Prerequisites for Development](#prerequisites-for-development)
  - [Understanding the Code](#understanding-the-code)
  - [Extending the Project](#extending-the-project)
- [FAQ](#faq)

## Introduction
The Dependency Graph Visualizer is a web application that allows users to upload dependency files and visualize them as graphs. The frontend is built using React and D3, while the backend is powered by Spring Boot.

## Getting Started

### Prerequisites
- Java 17 or higher
- Node.js 16 or higher
- npm 7 or higher
- Gradle 8.7

### Installation
1. Clone the repository:
    ```bash
    git clone <repository-url>
    cd <repository-directory>
    ```

2. Install backend dependencies:
    ```bash
    ./gradlew build
    ```

3. Install frontend dependencies:
    ```bash
    cd frontend
    npm install
    ```

### Running the Project
1. Start the backend server:
    ```bash
    ./gradlew bootRun
    ```

2. Start the frontend development server:
    ```bash
    cd frontend
    npm start
    ```

## Usage Guide
1. Navigate to the home page and click on the "Upload Dependency File" button.
2. Choose a `.log` or `.txt` file containing the Gradle dependencies.
3. Click the "Generate Graph" button to visualize the dependency graph.

## Developer Guide

### Prerequisites for Development
- Java 17 or higher
- Node.js 16 or higher
- npm 7 or higher
- Gradle 8.7

### Understanding the Code
#### Frontend

**Graph.js**

This component renders the dependency graph using D3.js. It supports different layouts (default, grid, level-wise) and allows interaction with the nodes, such as clicking and dragging.

**GraphResult.js**

This is the main component that manages the graph visualization state and interactions. It handles the fetching of graph data, the display of the graph, and interactions such as node clicks and layout changes.

**Home.js**

Displays the landing page with a welcome message and a link to the upload page.

**SearchBar.js**

Provides an input field for searching nodes in the graph. It fetches and displays search suggestions as the user types.

**SidePanel.js**

Displays details of the selected node and allows navigation to its child nodes. It also provides a button to toggle the display of the graph.

**Upload.js**

Provides a form to upload the dependency file. It validates the file format and initiates the graph generation process upon submission.

**GraphPage.js**

A wrapper for the `Graph` component, ensuring it re-renders when the graph data changes. It also handles node clicks to distinguish between different types of nodes.

#### Backend

**WebConfig.java**

Configures CORS and resource handlers for serving static files and enabling cross-origin requests from the frontend.

**GraphController.java**

Handles API requests for generating and retrieving graph data. It includes endpoints for file upload, fetching graph data, searching nodes, and getting autocomplete suggestions.

**GraphService.java**

Provides methods to parse dependency files and generate graph data. It uses the Graphviz library to create SVG representations of the dependency graphs.

**DependencyParser.java**

Contains methods to parse Gradle dependency files and detect circular dependencies. It processes the input files and structures the dependency information for graph generation.

### Extending the Project
1. **Optimization**: Implement asynchronous processing using `@EnableAsync` to generate the graph incrementally. This can improve performance and allow the application to handle larger files more efficiently.
2. **Future Scopes**:
   - **Real-time Updates**: Implement WebSockets to provide real-time updates of the graph as the dependency file is being processed.
   - **Advanced Search**: Enhance the search functionality to support complex queries and filtering options.
   - **Enhanced Visualization**: Add more visualization options, such as different node shapes and edge styles to represent various dependency types.

## FAQ

### Buttons and Their Functions
- **Upload Dependency File**: Opens a dialog to select a dependency file for upload.
- **Generate Graph**: Processes the uploaded file and generates the dependency graph.
- **Arrange in Grid**: Organizes the nodes in a grid layout for better readability.
- **Settle Levelwise**: Arranges the nodes in a level-wise structure based on their hierarchy.
- **Show Circular Dependencies**: Highlights nodes and edges that are part of circular dependencies.
- **Hide Circular Dependencies**: Hides the highlighted circular dependencies.
- **Reset**: Resets the graph to its initial state.

### Detecting Circular Dependencies
Circular dependencies are detected by building a graph of the dependencies and using a depth-first search (DFS) algorithm to find cycles. The `GraphService` class implements this functionality, marking edges that form part of a cycle.

### Parsing the File
The `DependencyParser` class reads the dependency file line by line, identifies the structure of the dependencies, and builds a hierarchical representation. It handles different configurations and resolves conflicts in dependency versions.

### How to Add a New Node Type in the Graph?
Update the getNodeColor method in Graph.js to include the new node type and its color. 

### How to Increase the Upload File Size Limit?
Modify the spring.servlet.multipart.max-file-size and spring.servlet.multipart.max-request-size properties in application.properties. Currently, it is 10MB.

### How to Change the Layout of the Graph?
Use the toggleGridArrangement and toggleLevelWiseArrangement methods exposed by the Graph component to switch between different layouts.
