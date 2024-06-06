This project tries and helps a developer visualize the gradle dependencies in a graph format using Graphviz. The project has three main classes.

-GraphController class is a Spring MVC controller that handles web requests related to graph generation , search and export functionalities. It mainly works wwith Spring's dependency injection , asynchronous processing and web request handling capabilities to manage file uploads , process data and generate responses efficiently. 

-GraphService class is a Spring service which helps in managing and visualizing dependency graphs. It uses Graphviz for rendering graphs , Spring's @Async for asynchronous processing , it handles and visualizing dependencies , including handling circular dependencies.

-DependencyParser class provides functionality to read and parse dependency data. It supports chunking the data for efficient processing , handles hierarchical dependencies using indentation level and tries to extract information about each dependency (even their version conflicts , still work in progress).

Index and Result handles the frontend part of it . 
NOTE : You might have to wait for a few minutes if the dependencies.log file is too huge.

//THE CODE IS WORKING AT THE MOMENT . BUT STILL THERE ARE SOME THINGS TO IMPROVE AND ADD , SO IT IS A WORK IN PROGRESS.
