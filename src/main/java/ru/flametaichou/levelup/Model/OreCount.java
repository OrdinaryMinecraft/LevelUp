package ru.flametaichou.levelup.Model;

public class OreCount {

    private String oreName;
    private int count;

    public OreCount() {
    }

    public OreCount(String oreName, int count) {
        this.oreName = oreName;
        this.count = count;
    }

    public String getOreName() {
        return oreName;
    }

    public void setOreName(String oreName) {
        this.oreName = oreName;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
