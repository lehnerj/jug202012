package com.github.lehnerj.jug202012.cpu.waitnetwork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class GeoServiceRestClient {
    private static final Logger logger = LoggerFactory.getLogger(GeoServiceRestClient.class);
    public static final String TMP_TENANT = "tmp/tenant/";

    private void initFileSystem() {
        File data = new File(TMP_TENANT);
        if (!data.exists()) {
            logger.info("Creating data directory, successful=" + data.mkdirs());
        }
    }

    public long readGeoMappings(String tenant) {
        long response=0;
        try {
            URL url = new URL("http://localhost:9951/tenant/" + tenant);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            final InputStream responseStream = con.getInputStream();

            initFileSystem();

            try(BufferedInputStream inputStreamReader = new BufferedInputStream(responseStream);
                BufferedOutputStream bf = new BufferedOutputStream(new FileOutputStream(TMP_TENANT + tenant))) {
                int read;
                while ((read = inputStreamReader.read()) != -1) {
                    bf.write(read);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                response++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}
