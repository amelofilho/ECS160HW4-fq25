package com.ecs160.annotations;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Endpoint{
    String url();// expect one argument

}
//
// to use @Endpoint(url = "[url]")


