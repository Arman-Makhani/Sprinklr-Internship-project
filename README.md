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
8. [Contributing](#contributing)
9. [License](#license)

## Introduction
The Dependency Graph Visualizer is a tool designed to convert Gradle dependency logs into interactive and visual dependency graphs. It helps users understand their project's dependencies, identify issues such as unresolved dependencies, and detect circular dependencies.

## Getting Started

### Prerequisites
Before you begin, ensure you have the following installed:

- **Java Development Kit (JDK)**: Ensure you have JDK 20 or later installed. You can download it from [Oracle's website](https://www.oracle.com/java/technologies/javase/jdk20-archive-downloads.html).
- **Graphviz**: For rendering graphs. Install Graphviz from [Graphviz's official website](https://graphviz.org/download/).
- **Spring Boot**: Ensure you have Spring Boot installed. Spring Boot simplifies the creation of stand-alone, production-grade Spring-based applications.
- **Gradle**: Make sure you have Gradle installed to handle dependencies and builds. Install it from [Gradle's official website](https://gradle.org/install/).

### Installation

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/yourusername/dependency-graph-visualizer.git
   cd dependency-graph-visualizer
