package com.tinyappsdev.forestsupply.helper;

import java.util.List;
import java.util.Map;

public class TinyMap {
    private Map mMap;
    public static TinyMap AsTinyMap(Map map) { return map == null ? null : new TinyMap(map); }

    TinyMap(Map map) { mMap = map; }
    public Map map() { return mMap; }

    public boolean getBoolean(String name) {
        return ToBoolean(mMap.get(name));
    }

    public double getDouble(String name) {
        return ToDouble(mMap.get(name));
    }

    public int getInt(String name) {
        return ToInt(mMap.get(name));
    }

    public long getLong(String name) {
        return ToLong(mMap.get(name));
    }

    public String getString(String name) {
        return ToString(mMap.get(name));
    }

    public TinyMap getTinyMap(String name) {
        return ToTinyMap(mMap.get(name));
    }

    public TinyList getTinyList(String name) {
        return ToTinyList(mMap.get(name));
    }

    public Object get(String name) {
        return mMap.get(name);
    }

    public boolean hasKey(String name) { return mMap.containsKey(name); }

    public static boolean ToBoolean(Object v) {
        if(v == null) return false;
        if(v instanceof Boolean) return ((Boolean)v).booleanValue();
        return Boolean.parseBoolean(v.toString());
    }

    public static double ToDouble(Object v) {
        if(v == null) return 0.0;
        if(v instanceof Number) return ((Number)v).doubleValue();
        return Double.parseDouble(v.toString());
    }

    public static int ToInt(Object v) {
        if(v == null) return 0;
        if(v instanceof Number) return ((Number)v).intValue();
        return Integer.parseInt(v.toString());
    }

    public static long ToLong(Object v) {
        if(v == null) return 0L;
        if(v instanceof Number) return ((Number)v).longValue();
        return Long.parseLong(v.toString());
    }

    public static String ToString(Object v) {
        if(v == null) return null;
        if(v instanceof String) return (String)v;
        return String.valueOf(v);
    }

    public static TinyMap ToTinyMap(Object v) {
        return TinyMap.AsTinyMap((Map)v);
    }

    public static TinyList ToTinyList(Object v) {
        return TinyList.AsTinyList((List)v);
    }


    public static class TinyList {
        List mList;
        public static TinyList AsTinyList(List list) { return list == null ? null : new TinyList(list); }

        TinyList(List list) { mList = list; }
        public List list() { return mList; }

        public boolean getBoolean(int index) {
            return ToBoolean(mList.get(index));
        }

        public double getDouble(int index) {
            return ToDouble(mList.get(index));
        }

        public int getInt(int index) {
            return ToInt(mList.get(index));
        }

        public long getLong(int index) {
            return ToLong(mList.get(index));
        }

        public String getString(int index) {
            return ToString(mList.get(index));
        }

        public TinyMap getTinyMap(int index) {
            return ToTinyMap(mList.get(index));
        }

        public TinyList getTinyList(int index) {
            return ToTinyList(mList.get(index));
        }

        public Object get(int index) {
            return mList.get(index);
        }

        public boolean hasIndex(int index) { return index >= 0 && index < mList.size(); }

    }
}
