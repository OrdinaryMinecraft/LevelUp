package ru.flametaichou.levelup.Model;

public enum PacketChannel {
    // ids must be unique and ordered
    LEVELUPINIT(0),
    LEVELUPCLASSES(1),
    LEVELUPSKILLS(2),
    LEVELUPCFG(3),
    LEVELUPEXTPROP(4),
    LEVELUPOTHER(5);

    private final int id;

    private PacketChannel(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}
