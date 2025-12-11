package com.ecs160.persistence.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface LazyLoad{
    String field();// expect one argument

}
// to use @LazyLoad(field=FIELDNAME)


