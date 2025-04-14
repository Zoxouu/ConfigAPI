package me.zoxouu.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import me.zoxouu.config.annotation.ConfigField;
import me.zoxouu.config.annotation.ConfigComment;
import me.zoxouu.config.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class YamlConfig {

    private File file;
    private YamlConfig instance = null;

    public void loadInstance(String fileName, String folderName, YamlConfig instance) {
        this.file = new File(folderName + "/" + fileName + ".yml");
        this.instance = instance;
        load();
    }

    public void save() {
        StringBuilder yamlWithComments = new StringBuilder();
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));

        try {
            for (Field field : getConfigFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(ConfigComment.class)) {
                    ConfigComment comment = field.getAnnotation(ConfigComment.class);
                    for (String commentLine : comment.value()) {
                        yamlWithComments.append("# ").append(commentLine).append("\n");
                    }
                }

                String fieldName = field.getName();
                Object fieldValue = field.get(this.instance);
                Map<String, Object> tempMap = new LinkedHashMap<>();
                tempMap.put(fieldName, fieldValue);
                String yamlField = mapper.writeValueAsString(tempMap);
                yamlWithComments.append(yamlField.trim()).append("\n");
            }

            IOUtils.save(yamlWithComments.toString(), file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Field[] getConfigFields() {
        if (instance == null) {
            throw new NullPointerException("You need to define the class fields using setInstance() in your config.");
        }
        return Arrays.stream(instance.getClass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(ConfigField.class))
                .toArray(Field[]::new);
    }

    private void load() {
        if (!file.exists()) {
            System.out.println("No config detected, running on default config");
            try {
                boolean isCreated = IOUtils.create(file);
                if (!isCreated) {
                    System.out.println("Can't create the default config file, am I missing some permissions?");
                } else {
                    save();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                JsonNode fields = mapper.readTree(file);
                if (fields == null || fields.isNull()) {
                    System.out.println("Empty config detected! (" + file.getName() + ") Using default one.");
                    return;
                }
                boolean needSave = false;
                for (Field field : getConfigFields()) {
                    String fieldName = field.getName();

                    if (fields.has(fieldName)) {
                        JsonNode jsonNode = fields.get(fieldName);
                        Object value = mapper.treeToValue(jsonNode, field.getType());

                        field.setAccessible(true);
                        field.set(instance, value);
                    } else {
                        needSave = true;
                    }
                }
                if (needSave) save();
            } catch (IOException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
