package me.zoxouu.config.yml;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import me.zoxouu.config.IOUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class YmlConfig {
    private File file;

    private YmlConfig instance = null;

    public void loadInstance(String fileName, String folderName, YmlConfig instance) {
        this.file = new File(folderName + "/" + fileName + ".yml");
        this.instance = instance;
        load();
    }

    public void save() {
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(options);

        try {
            StringBuilder yamlWithComments = new StringBuilder();

            for (Field field : getConfigFields()) {
                field.setAccessible(true);

                YmlConfigComment comment = field.getAnnotation(YmlConfigComment.class);
                if (comment != null) {
                    yamlWithComments.append("# ").append(comment.value()).append("\n");
                }

                Object value = field.get(this);
                String fieldName = field.getName();
                yamlWithComments.append(fieldName).append(": ").append(value).append("\n");
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
                .filter(f -> f.isAnnotationPresent(YmlConfigField.class))
                .toArray(Field[]::new);
    }

    private void load() {
        if (!file.exists()) {
            try {
                boolean isCreated = IOUtils.create(file);
                if (!isCreated) {
                    System.out.println("Can't create the default config file, am I missing some permissions ?");
                } else {
                    save();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                String yamlContent = IOUtils.readFileToString(file.toPath());

                Yaml yaml = new Yaml();

                Map<String, Object> loadedData = yaml.load(yamlContent);

                if (loadedData == null) {
                    return;
                }

                boolean needSave = false;
                for (Field field : getConfigFields()) {
                    String fieldName = field.getName();

                    if (loadedData.containsKey(fieldName)) {
                        Object value = loadedData.get(fieldName);
                        field.setAccessible(true);
                        field.set(this, value);
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