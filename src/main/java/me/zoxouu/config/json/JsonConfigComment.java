package me.zoxouu.config.json;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonConfigComment {
    String value();
}
