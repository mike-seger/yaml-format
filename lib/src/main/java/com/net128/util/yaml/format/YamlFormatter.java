package com.net128.util.yaml.format;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class YamlFormatter {
    public String format(String input) {
        var yaml = new Yaml();
        var tree = yaml.loadAs(input, TreeMap.class);
        tree = mapDottedKeys(tree);
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        var yamlOut = new Yaml(options);
        return yamlOut.dump(collapseSingletonMaps(tree));
    }

    public String format(File file) throws IOException {
        return format(Files.readString(file.toPath()));
    }

    private TreeMap<String, Object> collapseSingletonMaps(final TreeMap<String, Object> rootTree) {
        var result=new TreeMap<String, Object>();
        rootTree.forEach((key, value) -> addCollapsedSubTree(result, new Node(key, value)));
        return result;
    }

    private void addCollapsedSubTree(TreeMap<String, Object> parentTree, Node node) {
        if(node.value instanceof Map) {
            var childTree = (Map<String, Object>)node.value;
            if(childTree.size()==1) {
                addCollapsedSubTree(parentTree, new Node(node.key, (childTree.entrySet().iterator().next())));
                node = null; //do not add this node to parent
            } else {
                var newChildTree = new TreeMap<String, Object>();
                childTree.forEach((key, value) -> addCollapsedSubTree(newChildTree, new Node(key, value)));
                node = new Node(node.key, newChildTree);
            }
        }
        addNodeToTree(parentTree, node);
    }

    private TreeMap<String, Object> mapDottedKeys(final Map<String, Object> rootTree) {
        var result=new TreeMap<String, Object>();
        rootTree.forEach((key, value) -> addMappedSubTreeKeys(result, new Node(key, value)));
        return result;
    }

    private void addMappedSubTreeKeys(Map<String, Object> parentTree, Node node) {
        if(node.key.contains(".")) {
            var keys = node.key.split("[.]");
            node.key = keys[keys.length-1];
            for(int i=0; i<keys.length-1; i++) {
                parentTree = (Map<String, Object>)addNodeToTree(parentTree, keys[i], new HashMap<String, Object>());
            }
        }
        addNodeToTree(parentTree, node);
    }

    private void addNodeToTree(Map<String, Object> tree, Node node) {
        if (node != null) {
            addNodeToTree(tree, node.key, node.value);
        }
    }

    private Object addNodeToTree(Map<String, Object> tree, String key, Object value) {
        Object existingValue=tree.get(key);
        if(existingValue==null) {
            tree.put(key, value);
            return value;
        } else if(existingValue == value) {
            return value;
        } else if(existingValue instanceof Map && value instanceof Map) {
            try {
                var mergedMaps= mergeMaps((Map<String, Object>) existingValue, (Map<String, Object>) value);
                tree.put(key, mergedMaps);
                return mergedMaps;
            } catch(Exception e) {
                throw new IllegalStateException("Merge collision at key: "+key, e);
            }
        } else {
            throw new IllegalStateException("Merge collision at key: "+key);
        }
    }

    private Map<String, Object> mergeMaps(Map<String, Object> map1, Map<String, Object> map2) {
        return Stream.concat(map1.entrySet().stream(),
            map2.entrySet().stream()).collect(
            Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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
}
