package com.net128.util.yaml.format;

import com.net128.util.yaml.format.yaml2props.Yaml2Props;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import pl.szczepanik.silencio.api.Format;
import pl.szczepanik.silencio.api.Processor;
import pl.szczepanik.silencio.core.Builder;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.TreeMap;

public class YamlFormatter implements StringLoader {
    public String format(String input) {
        Yaml yaml = new Yaml();
        var config = yaml.loadAs(input, TreeMap.class);

//        DumperOptions options = new DumperOptions();
//        options.setIndent(2);
//        options.setPrettyFlow(true);
//        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
//        Yaml yamlOut = new Yaml(options);
//
//        Reader reader = new StringReader(input);
//        Writer output = new StringWriter();
//
//        Processor processor = new Builder(Format.YAML).with(Builder.BLANK).build();
//        processor.load(reader);
//        processor.process();
//        processor.write(output);
//        return output.toString();

        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yamlOut = new Yaml(options);
        //yamlOut.represent(config);
        return yamlOut.dump(config);

        //return toProperties(config);
        //return new Yaml2Props(input).convert();
    }
    public String formatFile(File input) throws IOException {
        return format(fromFile(input));
    }
    public String formatResource(String input) throws IOException, URISyntaxException {
        return format(fromResource(input));
    }

    private static final TreeMap<String, Object> flattenSingletonMaps(final TreeMap<String, Object> rootTree) {
        var result=new TreeMap<String, Object>();
        for (final String key : rootTree.keySet()) {
            Node node=addSubTree(rootTree, new Node(key, rootTree.get(key)));
            result.put(node.key, node.value);
        }
        return result;
    }

    private static void addSubTree(TreeMap<String, Object> parentTree, Node node) {
        if(node.value instanceof Map) {
            var childTree = (TreeMap<String, Object>)node.value;
            if(childTree.size()==1) {
                addSubTree(parentTree, new Node(node.key, childTree.firstEntry()));
            } else {
                var newChildTree = new TreeMap<String, Object>();
                childTree.forEach((key, value) -> addSubTree(newChildTree, new Node(key, value)));
                node = new Node(node.key, newChildTree);
            }
        }
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

    private static String toProperties(final TreeMap<String, Map<String, Object>> config) {
        StringBuilder sb = new StringBuilder();
        for (final String key : config.keySet()) {
            sb.append(toString(key, config.get(key)));
        }
        return sb.toString();
    }

    private static String toString(final String key, final Object o) {
        StringBuilder sb = new StringBuilder();
        if (o instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) o;
            for (final String mapKey : map.keySet()) {
                if (map.get(mapKey) instanceof Map) {
                    sb.append(toString(String.format("%s.%s", key, mapKey), map.get(mapKey)));
                } else {
                    sb.append(String.format("%s.%s=%s%n", key, mapKey, (null == map.get(mapKey)) ? null : map.get(mapKey).toString()));
                }
            }
        } else {
            sb.append(String.format("%s=%s%n", key, o));
        }
        return sb.toString();
    }
}
