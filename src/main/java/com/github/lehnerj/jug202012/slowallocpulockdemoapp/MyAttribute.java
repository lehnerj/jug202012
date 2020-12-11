package com.github.lehnerj.jug202012.slowallocpulockdemoapp;

import java.util.UUID;

public class MyAttribute {
    private final UUID id;
    private final String name;

    public MyAttribute(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MyAttribute that = (MyAttribute) o;

        if (!id.equals(that.id)) return false;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "MyAttribute{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
