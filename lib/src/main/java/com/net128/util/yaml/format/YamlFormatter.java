package com.net128.util.yaml.format;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

@SuppressWarnings("unchecked")
public class YamlFormatter {
    public String format(String input) {
        var yaml = new Yaml();
        var tree = yaml.loadAs(input, TreeMap.class);
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        var yamlOut = new Yaml(options);
        return yamlOut.dump(flattenSingletonMaps(tree));
    }

    public String format(File input) throws IOException {
        return format(fromFile(input));
    }

    @SuppressWarnings("unused")
    public String formatResource(String input) throws IOException, URISyntaxException {
        return format(fromResource(input));
    }

    private TreeMap<String, Object> flattenSingletonMaps(final TreeMap<String, Object> rootTree) {
        var result=new TreeMap<String, Object>();
        rootTree.forEach((key, value) -> addSubTree(result, new Node(key, value)));
        return result;
    }

    private void addSubTree(TreeMap<String, Object> parentTree, Node node) {
        if(node.value instanceof Map) {
            var childTree = (Map<String, Object>)node.value;
            if(childTree.size()==1) {
                addSubTree(parentTree, new Node(node.key, (childTree.entrySet().iterator().next())));
                node = null; //do not add this node to parent
            } else {
                var newChildTree = new TreeMap<String, Object>();
                childTree.forEach((key, value) -> addSubTree(newChildTree, new Node(key, value)));
                node = new Node(node.key, newChildTree);
            }
        }
        if(node!=null)
            parentTree.put(node.key, node.value);
    }

    private static class Node {
        String key;
        Object value;

        public Node(String prefix, Map.Entry<String, Object> entry) {
            this(prefix+"."+entry.getKey(), entry.getValue());
        }

        public Node(String key, Object value) {
            this.key = key;
            this.value = value;
        }
    }

    private String fromFile(File file) throws IOException {
        return Files.readString(file.toPath());
    }

    private String fromResource(String location) throws IOException, URISyntaxException {
        return new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getResource(location)).toURI())));
    }
}
