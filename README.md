
# Configuration Library for JSON5 and YAML

This repository provides a set of utility classes to manage configurations in various formats (JSON and YAML). The configuration system supports annotations for fields and comments, allowing you to easily load, save, and modify configuration files in your Java application.

## Features

- Supports configuration files in JSON and YAML formats.
- Allows annotations to define configurable fields and add comments for easy configuration management.
- Auto-loads configuration files on initialization.
- Saves configurations back to the respective file formats with proper formatting and optional comments.


## Installation

To use this configuration system, simply clone this repository and include the necessary files in your project.

```xml
<repository>
  <id>zoxouu-repository-releases</id>
  <name>Zoxouu Repository</name>
  <url>https://repo.zoxouu.me/releases</url>
</repository>

<dependency>
  <groupId>me.zoxouu</groupId>
  <artifactId>ConfigAPI</artifactId>
  <version>1.0.2</version>
</dependency>
```
```gradle
maven {
    name = "reposiliteRepositoryReleases"
    url = uri("https://repo.zoxouu.me/releases")
}

implementation("me.zoxouu:ConfigAPI:1.0.2")
```


## Usage

### JSON Configuration

To use the JSON configuration system, create a class that extends `JsonConfig` and annotate the fields with `@ConfigField`.

```java
import me.zoxouu.config.json.JsonConfig;
import me.zoxouu.config.json.JsonConfigField;
import me.zoxouu.config.json.JsonConfigComment;

public class MyConfig extends JsonConfig {

    @ConfigField
    private int serverPort = 8080;

    @ConfigField
    private String dbUsername = "admin";

    @JConfigField
    private String dbPassword = "password";
}
```

### YAML Configuration

The YAML configuration works similarly to JSON. Define your configuration class extending `YmlConfig` and annotate the fields with `@ConfigField` and `@ConfigComment`.

```java
import me.zoxouu.config.yml.YmlConfig;
import me.zoxouu.config.yml.YmlConfigField;
import me.zoxouu.config.yml.YmlConfigComment;

public class MyYmlConfig extends YmlConfig {

    @ConfigField
    @ConfigComment("This is the server port")
    private int serverPort = 8080;

    @ConfigField
    @ConfigComment("This is the database username")
    private String dbUsername = "admin";

    @ConfigField
    @ConfigComment("This is the database password")
    private String dbPassword = "password";
}
```

### Get Values

You can retrieve/assign configuration values ​​by calling the getter/setter created in your config class.

```java
MyConfig config = new MyConfig();
config.getValue(); // You need to create the getters in your config class.
```

### Saving Configurations

You can save the configuration by calling the `save()` method.

```java
config.save();  // Saves the config to the file
```

## Annotations

### `@ConfigField`

This annotation is used to mark fields that are part of the configuration. Fields marked with `@ConfigField` will be automatically processed during saving and loading.

```java
@ConfigField
private String serverAddress;
```

### `@ConfigComment`

This annotation allows you to add comments to the fields in the configuration file. The comment will be inserted above the field value in the saved configuration.

```java
@ConfigComment("This is the server address")
@ConfigField
private String serverAddress;
```

## Supported Formats

- **JSON**: JSON format with support for comments.
- **YAML**: YAML format with support for comments.
