package com.jackywong;

import com.jackywong.pool.Pool;
import org.junit.Test;

import java.sql.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by huangziqi on 2020/4/26
 */
public class TestDBPool {

    @Test
    public void testPool() throws Exception {
        Pool<Connection> pool = Pool.createPool(
                () -> DriverManager.getConnection("jdbc:postgresql://localhost:5432/mytest","huangziqi","yang520"),
                connection -> connection.close(),
                3, 1000L, 10);

        AtomicInteger invokes = new AtomicInteger(0);
        CountDownLatch countDownLatch = new CountDownLatch(100);
        for (int i = 0; i < 100; i++) {
            int idx = i;
            new Thread(() -> {
                try {
                    pool.withResource(conn -> {
                        try (PreparedStatement stmt = conn.prepareStatement("select 1");
                             ResultSet rs = stmt.executeQuery()) {
                            while (rs.next()) {
                                System.out.println("index: " + idx + " result: "+ rs.getObject(1));
                            }
                            invokes.addAndGet(1);
                            countDownLatch.countDown();
                        }
                        return null;
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        }
        countDownLatch.await();
        System.out.println("invokes: "+invokes.get());
    }
}
