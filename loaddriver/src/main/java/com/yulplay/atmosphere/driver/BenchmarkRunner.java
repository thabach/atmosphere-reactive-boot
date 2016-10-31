package com.yulplay.atmosphere.driver;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class BenchmarkRunner {

    public static void main(String[] s) throws Exception {

        System.out.println("Loading " + s[0]);
        Properties p = new Properties();
        p.load(new FileInputStream(new File(s[0])));

        AsyncHttpClientDriver driver = new AsyncHttpClientDriver(p);

        BenchmarkResult result = driver.run();
        System.out.println(result);

    }
}
