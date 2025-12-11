package com.ecs160;

// import com.ecs160.persistence.annotations.Id;
// import com.ecs160.persistence.annotations.LazyLoad;
// import com.ecs160.persistence.annotations.PersistableField;
// import com.ecs160.persistence.annotations.PersistableObject;
import com.ecs160.persistence.annotations.*;
import com.ecs160.persistence.RedisDB;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.List;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * tests for the persistence framework: RedisDB & annotations
 * run with: mvn test (assuming ur in the persistence-framework dir)
 */

@PersistableObject
class MockRepo {
    @Id
    @PersistableField
    private String id;

    @PersistableField
    private String name;

    @PersistableField
    private String language;

    @PersistableField
    private int stars;

    public MockRepo() {}

    public MockRepo(String id, String name, String language, int stars) {
        this.id = id;
        this.name = name;
        this.language = language;
        this.stars = stars;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }
}

@PersistableObject
class RepoLazy {
    @Id @PersistableField String id = "300";

    @LazyLoad(field = "OpenIssues")
    private List<Object> OpenIssues = null;

    public RepoLazy() {}

    public List<Object> getOpenIssues() {
        return OpenIssues;
    }
}

@PersistableObject
class Commit {
    @Id @PersistableField
    private String id;

    @PersistableField
    private String msg;

    public Commit() {}

    public String getId() { return id; }
    public String getMsg() { return msg; }
    public void setId(String id) { this.id = id; }
    public void setMsg(String msg) { this.msg = msg; }
}

@PersistableObject
class Issue {
    @Id @PersistableField
    private String id;

    @PersistableField
    private String title;

    public Issue() {}

    public String getId() { return id; }
    public String getTitle() { return title; }
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
}

public class AppTest {

    private RedisDB redisDB;
    private Jedis jedis;

    @Before
    public void setUp() {
        jedis = new Jedis("localhost", 6379);
        jedis.select(9);
        jedis.flushDB();
        redisDB = new RedisDB("localhost", 6379, 9);
    }

    @After
    public void tearDown() {
        jedis.close();
    }

        // verifies that persist stores all persistable fields in redis
    @Test
    public void testPersistStoresObject() throws Exception {
        MockRepo repo = new MockRepo("101", "ecs160-framework", "Java", 1500);
        assertTrue(redisDB.persist(repo));

        Map<String, String> stored = jedis.hgetAll("101");
        assertEquals("ecs160-framework", stored.get("name"));
        assertEquals("Java", stored.get("language"));
        assertEquals("1500", stored.get("stars"));
    }

    // verifies that persist rejects a class without the annotation
    @Test
    public void testNonPersistableClass() {
        class NotPersistable { public String field = "nope"; }
        assertFalse(redisDB.persist(new NotPersistable()));
    }

    // verifies that persist fails when no id field exists
    @Test
    public void testMissingIdField() {
        @PersistableObject
        class NoIdClass {
            @PersistableField String name = "test";
        }
        assertFalse(redisDB.persist(new NoIdClass()));
    }

    // verifies that persist returns false for a null object
    @Test
    public void testPersistNullObject() {
        assertFalse(redisDB.persist(null));
    }

    // verifies that persist fails when the id field value is null
    @Test
    public void testPersistNullIdValue() {
        @PersistableObject
        class NullIdClass {
            @Id @PersistableField String id = null;
            @PersistableField String name = "data";
        }
        assertFalse(redisDB.persist(new NullIdClass()));
    }

    // verifies that null persistable fields get replaced with a placeholder in redis
    @Test
    public void testPersistNullFieldReplacedWithPlaceholder() {
        MockRepo repo = new MockRepo("102", null, "Java", 1500);
        redisDB.persist(repo);

        Map<String, String> stored = jedis.hgetAll("102");
        assertEquals("NULL", stored.get("name"));
    }

    // verifies that persist overwrites any existing redis data under the same id
    @Test
    public void testPersistOverwritesExistingData() {
        redisDB.persist(new MockRepo("200", "first", "Java", 100));
        redisDB.persist(new MockRepo("200", "second", "Python", 200));

        Map<String, String> stored = jedis.hgetAll("200");
        assertEquals("second", stored.get("name"));
        assertEquals("Python", stored.get("language"));
        assertEquals("200", stored.get("stars"));
    }

    // verifies that load populates an object using data stored in redis
    @Test
    public void testLoadPopulatesObject() throws Exception {
        jedis.hset("101", Map.of(
            "name", "ecs160-framework",
            "language", "Java",
            "stars", "1500"
        ));

        MockRepo loaded = new MockRepo();
        loaded.setId("101");
        redisDB.load(loaded);

        assertEquals("ecs160-framework", loaded.getName());
        assertEquals("Java", loaded.getLanguage());
        assertEquals(1500, loaded.getStars());
    }

    // verifies that load returns null if the redis key does not exist
    @Test
    public void testLoadMissingKey() {
        MockRepo loaded = new MockRepo();
        loaded.setId("does-not-exist");
        assertNull(redisDB.load(loaded));
    }

    // verifies that load correctly converts string fields to their proper types
    @Test
    public void testLoadParsesIntAndString() {
        jedis.hset("999", Map.of(
            "name", "ecs160",
            "language", "Java",
            "stars", "42"
        ));

        MockRepo loaded = new MockRepo();
        loaded.setId("999");
        redisDB.load(loaded);

        assertEquals(42, loaded.getStars());
    }

    // verifies that proxy based lazy loading reconstructs objects when getter is invoked
    @Test
    public void testCreateProxyLazyLoad() {
        RepoLazy repo = new RepoLazy();
        RepoLazy proxy = (RepoLazy) redisDB.createProxy(repo);

        jedis.hset("300", "OpenIssues", "11,12");

        jedis.hset("11", Map.of(
            "id","11",
            "title","Bug found",
            "__class__", Issue.class.getName()
        ));

        jedis.hset("12", Map.of(
            "id","12",
            "title","Fix this",
            "__class__", Issue.class.getName()
        ));

        List<?> result = proxy.getOpenIssues();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    // verifies that handle lazy load fills a list field with reconstructed child objects
    @Test
    public void testHandleLazyLoadPopulatesList() throws Exception {

        @PersistableObject
        class RepoX {
            @Id @PersistableField String id = "400";
            @LazyLoad(field = "recentCommits")
            private List<Object> recentCommits = null;
        }

        RepoX repo = new RepoX();

        jedis.hset("400", "recentCommits", "11");

        jedis.hset("11", Map.of(
            "id","11",
            "msg","Initial commit",
            "__class__", Commit.class.getName()
        ));

        Field f = repo.getClass().getDeclaredField("recentCommits");
        f.setAccessible(true);

        Method m = RedisDB.class.getDeclaredMethod("handleLazyLoad", Object.class, Field.class);
        m.setAccessible(true);
        m.invoke(redisDB, repo, f);

        List<?> loaded = (List<?>) f.get(repo);
        assertNotNull(loaded);
        assertEquals(1, loaded.size());
    }

    // verifies that get object rebuilds a java object from a redis hash using class inference
    @Test
    public void testGetObjectRebuildsFromRedis() {
        jedis.hset("99", Map.of(
            "id","99",
            "title","Memory leak bug",
            "__class__", Issue.class.getName()
        ));

        Object obj = redisDB.getObject("99");
        assertNotNull(obj);

        // assertEquals("com.ecs160.hw.Issue", obj.getClass().getName());
        assertTrue(obj instanceof Issue);
    }

    // verifies that get id value retrieves the primary key stored in the id field
    @Test
    public void testGetIdValueReturnsCorrectId() throws Exception {
        @PersistableObject
        class Dummy {
            @Id @PersistableField String id = "abc123";
            @PersistableField String name = "test";
        }

        Dummy d = new Dummy();

        Method m = RedisDB.class.getDeclaredMethod("getIdValue", Object.class);
        m.setAccessible(true);

        String idValue = (String) m.invoke(redisDB, d);
        assertEquals("abc123", idValue);
    }
}