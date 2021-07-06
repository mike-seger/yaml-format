package com.net128.util.yaml.format;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

@SuppressWarnings("unchecked")
public class YamlFormatter {
	public String format(String multiInput) {
		var docSeparator = "---\n";
		return Arrays.stream(multiInput.split("\n" + docSeparator))
			.map(this::formatSingle)
			.filter(Objects::nonNull)
			.collect(Collectors.joining(docSeparator));
	}

	public String formatSingle(String input) {
	return formatSingle(input, true);
	}

	public String formatSingle(String input, boolean mapDotted) {
		var yaml = new Yaml();
		Map<String, Object> tree = yaml.loadAs(input, HashMap.class);
		if (tree == null) {
			return null;
		}
		if (mapDotted) tree = mapDottedKeys(tree);
		DumperOptions options = new DumperOptions();
		options.setIndent(2);
		options.setPrettyFlow(true);
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		var yamlOut = new Yaml(options);
		return yamlOut.dump(collapseSingletonMaps(tree));
	}

	public String identity(String input) {
		var yaml = new Yaml();
		Map<String, Object> tree = yaml.loadAs(input, HashMap.class);
		if (tree == null) {
			return null;
		}
		DumperOptions options = new DumperOptions();
		options.setIndent(2);
		options.setPrettyFlow(true);
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		var yamlOut = new Yaml(options);
		return yamlOut.dump(tree);
	}

	public String format(File file) throws IOException {
		return format(Files.readString(file.toPath()));
	}

	private Map<String, Object> collapseSingletonMaps(final Map<String, Object> rootTree) {
		var result = new TreeMap<String, Object>();
		var aliasValueMap = new HashMap<>();
		rootTree.forEach(
		(key, value) -> addCollapsedSubTree(result, new Node(key, value), aliasValueMap));
		return result;
	}

	private void addCollapsedSubTree(
		final Map<String, Object> parentTree, final Node node, Map<Object, Object> aliasValueMap) {
		var newNode = node;
		if (aliasValueMap.containsKey(node.value)) {
			newNode = new Node(node.key, aliasValueMap.get(node.value));
		} else if (node.value instanceof Map) {
			var childTree = (Map<String, Object>) node.value;
			if (childTree.size() == 1) {
				addCollapsedSubTree(parentTree, new Node(node.key, (childTree.entrySet().iterator().next())), aliasValueMap);
				newNode = null; // do not add this node to parent
			} else {
				var newChildTree = new TreeMap<String, Object>();
				childTree.forEach((key, value) -> addCollapsedSubTree(newChildTree, new Node(key, value), aliasValueMap));
				newNode = new Node(node.key, newChildTree);
				aliasValueMap.put(node.value, newNode.value);
			}
		}
		addNodeToTree(parentTree, newNode);
	}

	private Map<String, Object> mapDottedKeys(final Map<String, Object> rootTree) {
		var result = new TreeMap<String, Object>();
		rootTree.forEach((key, value) -> addMappedSubTreeKeys(result, new Node(key, value)));
		return result;
	}

	private void addMappedSubTreeKeys(Map<String, Object> parentTree, Node node) {
		if (node.key.contains(".")) {
			var keys = node.key.split("[.]");
			node.key = keys[keys.length - 1];
			for (int i = 0; i < keys.length - 1; i++) {
				parentTree = (Map<String, Object>) addNodeToTree(parentTree, keys[i], new HashMap<String, Object>());
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
		Object existingValue = tree.get(key);
		if (existingValue == null) {
			tree.put(key, value);
			return value;
		} else if (existingValue == value) {
			return value;
		} else if (existingValue instanceof Map && value instanceof Map) {
			var mergedMaps = mergeMaps((Map<String, Object>) existingValue, (Map<String, Object>) value);
			tree.put(key, mergedMaps);
			return mergedMaps;
		} else {
			throw new IllegalStateException("Merge collision at key: " + key);
		}
	}

	private Map<String, Object> mergeMaps(Map<String, Object> map1, Map<String, Object> map2) {
		return Stream.concat(map1.entrySet().stream(), map2.entrySet().stream())
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private static class Node {
		String key;
		Object value;

		public Node(String prefix, Map.Entry<String, Object> entry) {
			this(prefix + "." + entry.getKey(), entry.getValue());
		}

		public Node(String key, Object value) {
			this.key = key;
			this.value = value;
		}
	}
}
