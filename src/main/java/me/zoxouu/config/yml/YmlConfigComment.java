package me.zoxouu.config.yml;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface YmlConfigComment {
    String value();
}
