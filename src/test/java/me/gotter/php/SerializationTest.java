package me.gotter.php;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class SerializationTest
{
    final Serialization u = new Serialization();

    @Test
    public void unserializePrimitives() throws Serialization.SerializationException {


        Assert.assertNull(u.parse("N;"));
        Assert.assertEquals(true, u.parse("b:1;"));
        Assert.assertEquals(false, u.parse("b:0;"));
        Assert.assertEquals(-1L, u.parse("i:-1;"));
        Assert.assertEquals(0L, u.parse("i:0;"));
        Assert.assertEquals(123456789123456789L, u.parse("i:123456789123456789;"));
        Assert.assertEquals(0.3, u.parse("d:0.29999999999999999;"));
        Assert.assertEquals(0.3, u.parse("d:0.3;"));
        Assert.assertEquals(-4.1235, u.parse("d:-4.1235;"));
    }

    @Test
    public void unserializeStrings() throws Serialization.SerializationException {
        Assert.assertEquals("Hello", u.parse("s:5:\"Hello\";"));
        Assert.assertEquals("\te\"'\n", u.parse("s:5:\"\te\"'\n\";"));
        Assert.assertEquals("", u.parse("s:0:\"\";"));
    }

    @Test
    public void unserializeMaps() throws Serialization.SerializationException {
        Map<String, Object> data;


        data = (Map<String, Object>) u.parse("a:0:{}");
        Assert.assertEquals(0, data.size());

        data = (Map<String, Object>) u.parse("a:2:{i:0;s:3:\"one\";i:1;s:4:\"t\"wo\";}");
        Assert.assertEquals(2, data.size());
        Assert.assertTrue(data.containsKey("0"));
        Assert.assertTrue(data.containsKey("1"));
        Assert.assertFalse(data.containsKey("2"));
        Assert.assertEquals("one", data.get("0"));
        Assert.assertEquals("t\"wo", data.get("1"));

        data = (Map<String, Object>) u.parse("a:3:{s:3:\"one\";b:1;s:3:\"two\";N;s:3:\"bar\";a:2:{i:0;b:1;i:1;b:0;}}");
        Assert.assertEquals(3, data.size());
        Assert.assertEquals(true, data.get("one"));
        Assert.assertNull(data.get("two"));
        Assert.assertEquals(2, ((Map)data.get("bar")).size());
        data = (Map<String, Object>) data.get("bar");
        Assert.assertEquals(true, data.get("0"));
        Assert.assertEquals(false, data.get("1"));
    }

    @Test(expected = Serialization.SerializationException.class)
    public void unserializeObjects() throws Serialization.SerializationException {
        u.parse("O:8:\"stdClass\":2:{s:3:\"foo\";s:3:\"bar\";s:3:\"bar\";N;}");
    }
}
