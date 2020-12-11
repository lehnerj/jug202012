package com.github.lehnerj.jug202012.cpu.waitnetwork.geoserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@SpringBootApplication
@RestController
public class GeoServer {

    public static void main(String[] args) {
        final SpringApplication app = new SpringApplication(GeoServer.class);
        app.setDefaultProperties(Collections
                .singletonMap("server.port", "9951"));
        app.run(args);
    }

    @GetMapping("/tenant/{tenant}")
    public String handleRequest (@PathVariable String tenant) {
        int count=1;
        int sleep=50;
        if(tenant.startsWith("tenant-100")) {
            count=1000;
            sleep=100;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= count; i++) {
            sb.append(Integer.toString(i) + " - ");
            activeSleep(sleep);
        }
        return sb.toString();
    }

    private void activeSleep(int sleep) {
        final long end = System.currentTimeMillis()+sleep;
        while (System.currentTimeMillis() < end);
    }
}