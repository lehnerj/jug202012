package com.github.lehnerj.jug202012.slowallocpulockdemoapp;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class MyAttributeBusinessLogic {

    private final MyAttributeCache cache;
    private Random random = new Random();

    public MyAttributeBusinessLogic(MyAttributeCache cache) {
        this.cache = cache;
    }

    public MyAttribute getAttribute(String id) {
        // FIXME refreshData should be called async every minute or so but not with every request
        cache.refreshData();

        if (id.isEmpty()) {
            id = cache.getIds().get(random.nextInt(cache.getIds().size()));
        }

        List<MyAttribute> resultList = new ArrayList<>();

        for (int i = 0; i < random.nextInt(1000) + 100; i++) {
            resultList.add(findAttribute(new String(id + RandomStringUtils.random(i)), cache.getAttributes()));
        }

        return resultList.stream().filter(Objects::nonNull).distinct().findFirst().get();
    }

    public MyAttribute findAttribute(String id, List<MyAttribute> attributes) {
        // FIXME iterating over List is not the best idea - access via two Maps (by name / by id)
        for (MyAttribute attribute : attributes) {
            if (Objects.equals(attribute.getName(), id)
                    || Objects.equals(attribute.getId().toString(), id)) { // FIXME getId().toString() is UUID.toString() which is expensive and churns a lot
                return attribute;
            }
        }
        return null;
    }

}

