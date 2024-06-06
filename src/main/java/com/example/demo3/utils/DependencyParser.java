package com.example.demo3.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class DependencyParser {

    public static List<Map<String, Map<String, List<String>>>> parseDependencies(String filePath, int chunkSize) throws IOException {
        List<Map<String, Map<String, List<String>>>> allChunks = new ArrayList<>();
        Map<String, Map<String, List<String>>> currentChunk = new LinkedHashMap<>();
        Map<String, List<String>> dependencies = null;
        Deque<DependencyNode> stack = new ArrayDeque<>();
        boolean parsingDependencies = false;
        String currentTitle = null;
        int lineCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String currentConfiguration = "implementation"; // Default configuration

            while ((line = br.readLine()) != null) {
                if (line.startsWith("> Task")) {
                    continue;
                }

                // Detect configuration changes in the log
                if (line.contains(" - implementation") || line.contains(" - api") || line.contains(" - runtimeOnly") || line.contains(" - testImplementation") || line.contains(" - testRuntimeOnly")) {
                    if (line.contains(" - implementation")) {
                        currentConfiguration = "implementation";
                    } else if (line.contains(" - api")) {
                        currentConfiguration = "api";
                    } else if (line.contains(" - runtimeOnly")) {
                        currentConfiguration = "runtimeOnly";
                    } else if (line.contains(" - testImplementation")) {
                        currentConfiguration = "testImplementation";
                    } else if (line.contains(" - testRuntimeOnly")) {
                        currentConfiguration = "testRuntimeOnly";
                    }
                    continue;
                }

                if (line.startsWith("Resolved dependencies:") || line.contains(" - ")) {
                    parsingDependencies = true;
                    currentTitle = line.trim();
                    dependencies = new LinkedHashMap<>();
                    currentChunk.put(currentTitle, dependencies);
                    continue;
                }

                if (!parsingDependencies || line.trim().isEmpty()) {
                    continue;
                }

                if (line.startsWith("+---") || line.startsWith("\\---")) {
                    String dependency = stripPrefix(line).replaceAll("->.*", "").trim();
                    DependencyNode node = new DependencyNode(dependency, getIndentLevel(line), parseDependencyDetails(dependency, currentConfiguration));
                    stack.clear();
                    stack.push(node);
                    dependencies.putIfAbsent(node.name, new ArrayList<>());
                } else {
                    String[] parts = line.split("\\s+", 2);
                    if (parts.length < 2) continue;

                    int indentLevel = getIndentLevel(line);
                    String dependency = stripPrefix(parts[1]).replaceAll("->.*", "").trim();

                    while (!stack.isEmpty() && indentLevel <= stack.peek().indentLevel) {
                        stack.pop();
                    }

                    if (!stack.isEmpty()) {
                        DependencyNode parent = stack.peek();
                        dependencies.computeIfAbsent(parent.name, k -> new ArrayList<>()).add(dependency);
                        dependencies.putIfAbsent(dependency, new ArrayList<>());
                        stack.push(new DependencyNode(dependency, indentLevel, parseDependencyDetails(dependency, currentConfiguration)));
                    }
                }

                lineCount++;
                if (lineCount >= chunkSize) {
                    allChunks.add(currentChunk);
                    currentChunk = new LinkedHashMap<>();
                    lineCount = 0;
                }
            }
        }

        if (!currentChunk.isEmpty()) {
            allChunks.add(currentChunk);
        }

        return allChunks;
    }

    private static String stripPrefix(String line) {
        return line.startsWith("+---") || line.startsWith("\\---") ? line.substring(4).trim() : line.trim();
    }

    private static int getIndentLevel(String line) {
        int level = 0;
        while (line.startsWith(" ") || line.startsWith("|")) {
            level++;
            line = line.substring(1);
        }
        return level / 4; // Adjusted for accurate indentation level
    }

    public static DependencyDetails parseDependencyDetails(String dependency, String configuration) {
        String[] parts = dependency.split(":");
        String group = parts.length > 0 ? parts[0] : null;
        String name = parts.length > 1 ? parts[1] : null;
        String version = parts.length > 2 ? parts[2] : null;
        String conflictVersion = null;
        if (version != null && version.contains("->")) {
            String[] versions = version.split("->");
            version = versions[1].trim();
            conflictVersion = versions[0].trim();
        }
        return new DependencyDetails(group, name, version, conflictVersion, configuration);
    }

    private static class DependencyNode {
        String name;
        int indentLevel;
        DependencyDetails details;

        DependencyNode(String name, int indentLevel, DependencyDetails details) {
            this.name = name;
            this.indentLevel = indentLevel;
            this.details = details;
        }
    }

    public static class DependencyDetails {
        public String group;
        public String name;
        public String version;
        public String conflictVersion;
        public String configuration;

        DependencyDetails(String group, String name, String version, String conflictVersion, String configuration) {
            this.group = group;
            this.name = name;
            this.version = version;
            this.conflictVersion = conflictVersion;
            this.configuration = configuration;
        }

        public Map<String, String> toMap() {
            Map<String, String> map = new HashMap<>();
            if (group != null) map.put("Group", group);
            if (name != null) map.put("Name", name);
            if (version != null) map.put("Version", version);
            if (conflictVersion != null) map.put("Conflict Version", conflictVersion);
            map.put("Configuration", configuration);
            return map;
        }
    }
}
