package com.net128.util.yaml.format.app;

import com.net128.util.yaml.format.YamlFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

public class YamlFormat {
    private static final Logger logger = LoggerFactory.getLogger(YamlFormat.class);

    public static void main(String[] args) throws Exception {
        var list = Arrays.asList(args);
        if(list.size()!=1) {
            logger.info("Usage: {} <yaml-file>", YamlFormat.class.getSimpleName());
            System.exit(1);
        }
        File yamlFile=new File(list.get(0));
        if(!yamlFile.exists()) {
            logger.info("Cannot find yaml-file: {}", yamlFile);
            logger.info("Files found in directory {}: {}",
                yamlFile.getParentFile(),
                Arrays.asList(Objects.requireNonNull(yamlFile.getParentFile().list())));
            System.exit(1);
        }

        String result = new YamlFormatter().formatFile(yamlFile);
        logger.info(result);
    }
}
