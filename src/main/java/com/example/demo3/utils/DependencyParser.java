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

                if (line.contains(" - implementation") || line.contains(" - api") || line.contains(" - runtimeOnly") || line.contains(" - testImplementation") || line.contains(" - testRuntimeOnly")) {
                    currentConfiguration = parseConfiguration(line);
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
                    int indentLevel = getIndentLevel(line);
                    String dependency = stripPrefix(line).replaceAll("->.*", "").trim();

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
        int index = line.indexOf("---");
        return (index != -1) ? line.substring(index + 3).trim() : line.trim();
    }

    private static int getIndentLevel(String line) {
        int level = 0;
        while (line.startsWith(" ") || line.startsWith("|")) {
            level++;
            line = line.substring(1);
        }
        return level / 4;
    }

    private static String parseConfiguration(String line) {
        if (line.contains(" - implementation")) return "implementation";
        if (line.contains(" - api")) return "api";
        if (line.contains(" - runtimeOnly")) return "runtimeOnly";
        if (line.contains(" - testImplementation")) return "testImplementation";
        if (line.contains(" - testRuntimeOnly")) return "testRuntimeOnly";
        return "implementation";
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
