package me.zoxouu.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.marhali.json5.Json5;
import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;
import me.zoxouu.config.annotation.ConfigComment;
import me.zoxouu.config.annotation.ConfigField;
import me.zoxouu.config.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonConfig {
    private File file;

    private JsonConfig instance = null;

    public void loadInstance(String fileName, String folderName, JsonConfig instance) {
        this.file = new File(folderName + "/" + fileName + ".json5");
        this.instance = instance;
        load();
    }

    public void save() {
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();

        Json5 json5 = Json5.builder(options ->
                options.allowInvalidSurrogate().quoteSingle().prettyPrinting().build());

        try {
            StringBuilder jsonWithComments = new StringBuilder("{\n");

            for (Field field : getConfigFields()) {
                field.setAccessible(true);

                ConfigComment comment = field.getAnnotation(ConfigComment.class);
                if (comment != null) {
                    jsonWithComments.append("  // ").append(comment.value()).append("\n");
                }

                Object value = field.get(this);
                String fieldName = field.getName();
                jsonWithComments.append("  \"").append(fieldName).append("\": ")
                        .append(prettyGson.toJson(value)).append(",\n");
            }

            if (jsonWithComments.charAt(jsonWithComments.length() - 2) == ',') {
                jsonWithComments.deleteCharAt(jsonWithComments.length() - 2);
            }
            jsonWithComments.append("\n}");

            IOUtils.save(jsonWithComments.toString(), file.getAbsolutePath());
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
                String json = IOUtils.readFileToString(file.toPath());

                Json5 json5Parser = new Json5();
                Json5Element jsonElement = json5Parser.parse(json);

                if (jsonElement instanceof Json5Object) {
                    Json5Object fields = (Json5Object) jsonElement;

                    if (fields == null) {
                        return;
                    }

                    boolean needSave = false;
                    for (Field field : getConfigFields()) {
                        String fieldName = field.getName();

                        if (fields.has(fieldName)) {
                            Json5Element fieldElement = fields.get(fieldName);
                            Object value = new Gson().fromJson(fieldElement.toString(), field.getType());

                            field.setAccessible(true);
                            field.set(this, value);
                        } else {
                            needSave = true;
                        }
                    }

                    if (needSave) save();
                } else {
                    System.out.println("Invalid JSON format in config file.");
                }
            } catch (IOException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}