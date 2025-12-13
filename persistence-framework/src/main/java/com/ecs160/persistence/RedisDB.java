package com.ecs160.persistence;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ecs160.persistence.annotations.Id;
import com.ecs160.persistence.annotations.LazyLoad;
import com.ecs160.persistence.annotations.PersistableField;
import com.ecs160.persistence.annotations.PersistableObject;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import redis.clients.jedis.Jedis;

public class RedisDB {

    // jedis client
    private final Jedis jedis;

    public RedisDB(String host, int port) {
        this.jedis = new Jedis(host, port);
    }

    // new constructor with dbIndex for selecting Redis DB (testing purposes)
    public RedisDB(String host, int port, int dbIndex) {
        this.jedis = new Jedis(host, port);
        this.jedis.select(dbIndex);
    }

    public RedisDB(Jedis jedis){
        this.jedis = jedis;
        
    }

    /* 
     * Saves any object annotated with @PersistableObject into Redis.
     * Each field annotated with @PersistableField is written as a hash field.
     *
     * HW1 DB KEY SCHEME:
     *  We use the raw @Id value as the Redis key (starts with 1): 
     *      1s = Repo hash
     *      3s = Issue hash
     *      2s = Commit hash
     */
    public boolean persist(Object o) {
        if (o == null) return false; // prevents NullPointerException
        Class<?> clazz = o.getClass();

        // reject if no persistable annotation
        if (!clazz.isAnnotationPresent(PersistableObject.class)) {
            return false;
        }

        String key = null;
        Map<String, String> map = new HashMap<>();

        try {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                // handles @Id
                if (field.isAnnotationPresent(Id.class)) {
                    Object idValue = field.get(o);
                    if (idValue == null) {
                        System.err.println("Cannot persist object: @Id value is null in class " + clazz.getSimpleName());
                        return false;
                    }
                    key = idValue.toString();
                }

                // handles @PersistableField
                if (field.isAnnotationPresent(PersistableField.class)) {
                    Object value = field.get(o);
                    if (value != null) {
                        map.put(field.getName(), value.toString());
                    } else {
                        map.put(field.getName(), "NULL");
                    }
                }
            }

            // if no @Id field was found log error and reject
            if (key == null) {
                System.err.println("Cannot persist object: missing @Id field " + clazz.getSimpleName());
                return false;
            }

            map.put("__class__", clazz.getName());
            jedis.hset(key, map);
            // String info = jedis.info("persistence");
            // if (!info.contains("rdb_bgsave_in_progress:1")) {
            //     jedis.bgsave();
            // }
            return true;
        } catch (IllegalAccessException e) {
            System.err.println("Access error: " + e.getMessage());
            return false;
        }
    } // end persist

    /* 
     * Reads an object back from Redis based on its @Id field.
     * Populates all @PersistableField fields.
     * Uses the raw @Id value as the Redis key
     */
    public Object load(Object o) {
        if (o == null) return null;

        Class<?> clazz = o.getClass();
        // reject if persistable annotation isn’t there
        if (!clazz.isAnnotationPresent(PersistableObject.class)) {
            return null;
        }

        String key = null;
        try {
            // find @Id field to construct Redis key
            for (Field f : clazz.getDeclaredFields()) {
                f.setAccessible(true);
                if (f.isAnnotationPresent(Id.class)) {
                    Object idVal = f.get(o);
                    if (idVal == null) {
                        System.err.println("No @Id value set on object of class: " + clazz.getSimpleName());
                        return null;
                    }
                    key = idVal.toString();
                    break;
                }
            }

            // handles null ID
            if (key == null) {
                System.err.println("No @Id field found in class: " + clazz.getSimpleName());
                return null;
            }

            // fetch hash map from Redis
            Map<String, String> map = jedis.hgetAll(key);
            if (map == null || map.isEmpty()) {
                System.err.println("No data found in Redis for key: " + key);
                return null;
            }

            // populate all @PersistableField fields
            for (Field f : clazz.getDeclaredFields()) {
                f.setAccessible(true);
                if (f.isAnnotationPresent(PersistableField.class)) {
                    String val = map.get(f.getName());
                    if (val != null) {
                        // parse integers correctly; stores strings otherwise
                        if (f.getType() == int.class || f.getType() == Integer.class) {
                            f.set(o, Integer.parseInt(val));
                        } else {
                            if ("NULL".equals(val)) {
                                f.set(o, null);
                            } else {
                                f.set(o, val);
                            }
                        }
                    } else if (f.isAnnotationPresent(Id.class)) {
                        f.set(o, key);
                    }
                }
            }
            // return reconstructed object
            return o;

        } catch (IllegalAccessException e) {
            System.err.println("Access error: " + e.getMessage());
            return null;
        }
    } // end load

    // helper function check if object is null or empty list
    private boolean isNullOrEmpty(Object val) {
        if (val == null) {
            return true;
        }
        if (val instanceof List<?> list) {
            return list.isEmpty();
        }
        return false;
    }

    // intercepts method calls for @LazyLoad fields
    public Object createProxy(Object o) {

        // already proxied. don't wrap again
        if (o instanceof javassist.util.proxy.ProxyObject) {
            return o;
        }

        Class<?> clazz = o.getClass();
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setSuperclass(clazz);

        try {
            //System.out.println("[DEBUG] Creating proxy for class: " + clazz.getSimpleName());
            Class<?> proxyClass = proxyFactory.createClass();
            Object proxyObject = proxyClass.getDeclaredConstructor().newInstance();

            // copy all fields from original object into proxy
            for (Field f : clazz.getDeclaredFields()) {
                f.setAccessible(true);
                f.set(proxyObject, f.get(o));
            }

            MethodHandler myHandler = (self, thisMethod, proceed, args) -> {
                String methodName = thisMethod.getName();
                //System.out.println("[DEBUG] Invoked method: " + methodName);

                // only intercept getters
                if (methodName.startsWith("get")) {
                    String getterName = methodName.substring(3);   // "Name" or "OpenIssues"
                    //System.out.println("[DEBUG] Intercepted getter for: " + getterName);

                    Field field = null;
                    field = resolveFieldFlexible(clazz, getterName);

                    if (field == null) {
                        //System.out.println("[DEBUG] Field not found: " + getterName);
                        thisMethod.setAccessible(true);
                        return thisMethod.invoke(o, args);
                    }

                    //System.out.println("[DEBUG] Resolved field: " + field.getName());

                    // lazy load handling
                    if (field.isAnnotationPresent(LazyLoad.class)) {
                        //System.out.println("[DEBUG] Field has @LazyLoad");

                        field.setAccessible(true);
                        Object currentVal = field.get(o);
                        //System.out.println("[DEBUG] Current lazy value: " + currentVal);

                        if (isNullOrEmpty(currentVal)) {
                            //System.out.println("[DEBUG] Triggering handleLazyLoad() for: " + field.getName());
                            handleLazyLoad(o, field);
                        }
                    }
                }

                thisMethod.setAccessible(true);
                return thisMethod.invoke(o, args);
            };

            ((javassist.util.proxy.Proxy) proxyObject).setHandler(myHandler);
            //System.out.println("[DEBUG] Proxy created successfully for: " + clazz.getSimpleName());
            return proxyObject;

        } catch (Exception e) {
            System.err.println("[ERROR] Proxy creation failed: " + e.getMessage());
            e.printStackTrace();
            return o;
        }
    }

    private void handleLazyLoad(Object o, Field f) throws IllegalAccessException {
        f.setAccessible(true);
        LazyLoad fieldMeta = f.getAnnotation(LazyLoad.class);
        String fieldName = fieldMeta.field();

        //System.out.println("[DEBUG] handleLazyLoad() called for field: " + fieldName);

        String parentKey = getIdValue(o);
        //System.out.println("[DEBUG] Parent key: " + parentKey);
        if (parentKey == null) {
            //System.out.println("[DEBUG] No @Id value on parent; cannot lazy load.");
            return;
        }

        // String rawIds = jedis.hget(parentKey, fieldName);
        // if (rawIds == null) {
        //     rawIds = jedis.hget(parentKey, capitalize(fieldName));
        // }
        String rawIds = hgetFlexible(parentKey, fieldName);
        //System.out.println("[DEBUG] Raw IDs from Redis: " + rawIds);

        if (rawIds == null || rawIds.isEmpty()) {
            f.set(o, new ArrayList<>());
            //System.out.println("[DEBUG] No IDs found for lazy field " + fieldName);
            return;
        }

        List<String> ids = new ArrayList<>();
        Arrays.stream(rawIds.split(","))
              .map(String::trim)
              .filter(s -> !s.isEmpty())
              .forEach(ids::add);

        // prints first 5 IDs for debugging
        // System.out.println(
        //     "[DEBUG] Parsed IDs (first 5 of " + ids.size() + "): " 
        //     + ids.subList(0, Math.min(5, ids.size())) + " ..."
        // );

        List<Object> loaded = new ArrayList<>();
        for (String id : ids) {
            //System.out.println("[DEBUG] Attempting to getObject for ID: " + id);
            Object child = getObject(id);
            if (child != null) {
                //System.out.println("[DEBUG] Object loaded successfully for " + id);
                loaded.add(child);
            } else {
                //System.out.println("[DEBUG] Object load returned null for " + id);
            }
        }

        f.set(o, loaded);
        //System.out.println("[DEBUG] Field " + fieldName + " populated with " + loaded.size() + " objects");
    }

    // helper to capitalize first letter (avoid letter case issues)
    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }

    public Object getObject(String key) {
        //System.out.println("[DEBUG] getObject() called with key: " + key);
        Map<String, String> map = jedis.hgetAll(key);
        
        // System.out.println("[DEBUG] Redis returned map: " + map);
        
        // prevent printing massive crash dumps from Issue.body
        String preview = map.toString();
        if (preview.length() > 300) {
            preview = preview.substring(0, 300) + "...";
        }
        //System.out.println("[DEBUG] Redis returned map (trimmed): " + preview);
        

        if (map == null || map.isEmpty()) {
            //System.out.println("[DEBUG] No Redis data found for key: " + key);
            return null;
        }

        try {
            // for testing purposes only 
            Class<?> clazz = inferClassFromMap(map);
            if (clazz == null) {
                //System.out.println("[DEBUG] Could not infer class type for key: " + key);
                return null;
            }

            Constructor<?> structor = clazz.getDeclaredConstructor();
            structor.setAccessible(true);
            Object instance = structor.newInstance();

            for (Field f : clazz.getDeclaredFields()) {
                f.setAccessible(true);

                if (f.isAnnotationPresent(PersistableField.class)) {
                    // String val = map.get(f.getName());
                    String val = getFlexibleMapValue(map, f.getName());

                    if (val != null) {
                        if (f.getType() == int.class || f.getType() == Integer.class) {
                            f.set(instance, Integer.parseInt(val));
                        } else if ("NULL".equals(val)) {
                            f.set(instance, null);
                        } else {
                            f.set(instance, val);
                        }
                    } else if (f.isAnnotationPresent(Id.class)) {
                        // fill id from key when Redis doesn't store it
                        f.set(instance, key);
                    }
                }
            }

            return createProxy(instance);

        } catch (Exception e) {
            System.err.println("[ERROR] Error reconstructing object: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    private Class<?> inferClassFromMap(Map<String, String> map) {

        if (map.containsKey("__class__")) {
        try {
            return Class.forName(map.get("__class__"));
        } catch (Exception ignored) {}
    }

        // Issue
        if (map.containsKey("title") || map.containsKey("body")) {
            try { return Class.forName("com.ecs160.hw.model.Issue"); }
            catch (Exception ignored) {}
        }

        // Commit
        if (map.containsKey("sha") || map.containsKey("authorName")
                || map.containsKey("msg") || map.containsKey("message")) {
            try { return Class.forName("com.ecs160.hw.model.Commit"); }
            catch (Exception ignored) {}
        }

        // Repo
        if (map.containsKey("name") && map.containsKey("ownerLogin")) {
            try { return Class.forName("com.ecs160.hw.model.Repo"); }
            catch (Exception ignored) {}
        }

        // MockRepo (used only in persistence-framework tests)
        if (map.containsKey("stars") || map.containsKey("language")) {
            try { return Class.forName("com.ecs160.MockRepo"); }
            catch (Exception ignored) {}
        }

        return null;
    }


    // helper to read the value of the @Id field for creating key
    private String getIdValue(Object o) {
        for (Field f : o.getClass().getDeclaredFields()) {
            if (f.isAnnotationPresent(Id.class)) {
                try {
                    f.setAccessible(true);
                    Object id = f.get(o);
                    //System.out.println("[DEBUG] getIdValue() found @Id: " + id);
                    if (id != null) return id.toString();
                    else return null;
                } catch (IllegalAccessException e) {
                    //System.err.println("[ERROR] getIdValue() access error: " + e.getMessage());
                    return null;
                }
            }
        }
        //System.out.println("[DEBUG] getIdValue() found no @Id field in class: " + o.getClass().getSimpleName());
        return null;
    }

    
    // Try all possible casings, return the first Redis value that exists
    private String hgetFlexible(String key, String base) {
        // try exact
        String v = jedis.hget(key, base);
        if (v != null) return v;

        String lower = Character.toLowerCase(base.charAt(0)) + base.substring(1);
        v = jedis.hget(key, lower);
        if (v != null) return v;

        String upper = Character.toUpperCase(base.charAt(0)) + base.substring(1);
        v = jedis.hget(key, upper);
        if (v != null) return v;

        v = jedis.hget(key, base.toLowerCase());
        if (v != null) return v;

        v = jedis.hget(key, base.replace("_", ""));
        if (v != null) return v;

        return null;
    }

        // function to resolve field name variations from HW1 (OpenIssues vs open_issues, etc)
        private String getFlexibleMapValue(Map<String,String> map, String base) {
        if (map.containsKey(base)) return map.get(base);

        String lower = Character.toLowerCase(base.charAt(0)) + base.substring(1);
        if (map.containsKey(lower)) return map.get(lower);

        String upper = Character.toUpperCase(base.charAt(0)) + base.substring(1);
        if (map.containsKey(upper)) return map.get(upper);

        if (map.containsKey(base.toLowerCase())) return map.get(base.toLowerCase());

        String noUnderscore = base.replace("_", "");
        if (map.containsKey(noUnderscore)) return map.get(noUnderscore);

        return null;
    }

        private Field resolveFieldFlexible(Class<?> c, String base) {
        List<String> candidates = new ArrayList<>();

        candidates.add(base); // exact
        candidates.add(Character.toLowerCase(base.charAt(0)) + base.substring(1)); // lowerCamel
        candidates.add(Character.toUpperCase(base.charAt(0)) + base.substring(1)); // UpperCamel
        candidates.add(base.toLowerCase()); // lower
        candidates.add(base.replace("_","")); // remove underscores

        for (String name : candidates) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {}
        }
        return null;
    }
}