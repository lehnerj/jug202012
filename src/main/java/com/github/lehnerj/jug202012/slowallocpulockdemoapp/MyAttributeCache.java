package com.github.lehnerj.jug202012.slowallocpulockdemoapp;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public class MyAttributeCache {
    public static final int SIZE = 1000;
    private final Object lock = new Object();
    private final List<MyAttribute> attributes = new CopyOnWriteArrayList<>();
    private final List<String> ids = new ArrayList<>();

    public void refreshData() {
        refreshData(new Function<UUID, UUID>() {
            @Override
            public UUID apply(UUID uuid) {
                return uuid;
            }
        });
    }

    public void refreshData(Function<UUID, UUID> r) {
        synchronized (lock) {
            // stable ids per app run, don't recalc otherwise load generator breaks
            if(ids.isEmpty()) {
                for (int i = 0; i < SIZE; i++) {
                    UUID id = UUID.randomUUID();
                    ids.add(id.toString());
                }
            }

            attributes.clear();

            int i=0;
            for (String idStr : ids) {
                i++;
                UUID id = r.apply(UUID.fromString(idStr));
                attributes.add(new MyAttribute(id, "attribute-" + i));
            }
        }
    }


    public List<MyAttribute> getAttributes() {
        synchronized (lock) {
            return attributes;
        }
    }

    public List<String> getIds() {
        synchronized (lock) {
            return ids;
        }
    }
}
