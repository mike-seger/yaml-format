package com.net128.util.yaml.format;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("unchecked")
public class YamlFormatter implements StringLoader {
    public String format(String input) {
        var yaml = new Yaml();
        var config = yaml.loadAs(input, TreeMap.class);
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        var yamlOut = new Yaml(options);
        return yamlOut.dump(flattenSingletonMaps(config));
    }
    public String formatFile(File input) throws IOException {
        return format(fromFile(input));
    }
    public String formatResource(String input) throws IOException, URISyntaxException {
        return format(fromResource(input));
    }

    private static TreeMap<String, Object> flattenSingletonMaps(final TreeMap<String, Object> rootTree) {
        var result=new TreeMap<String, Object>();
        rootTree.forEach((key, value) -> addSubTree(result, new Node(key, value)));
        return result;
    }

    private static void addSubTree(TreeMap<String, Object> parentTree, Node node) {
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
}
