package com.jackywong;

import com.jackywong.pool.Pool;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by huangziqi on 2020/4/26
 */
public class TestSinglePool {
    class MyResource {
        public String name;
        public AtomicInteger uses;

        public MyResource(String name, AtomicInteger uses) {
            this.name = name;
            this.uses = uses;
        }
    }

    private String nowString() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss_SSS"));
    }

    @Test
    public void testPool() throws Exception {
        AtomicInteger index = new AtomicInteger(0);
        AtomicInteger uses = new AtomicInteger(0);
        Pool<MyResource> pool = Pool.createPool(
                () -> {
                    MyResource myResource = new MyResource("Name_"+index.addAndGet(1), new AtomicInteger(0));
                    System.out.println(myResource.name+ " create at "+nowString());
                    return myResource;
                },
                myResource -> {
                    uses.addAndGet(myResource.uses.get());
                    System.out.println(myResource.name+" close at: " + nowString() + " and it uses: " + myResource.uses.get());
                },
                3, 500L, 10);

        AtomicInteger invokes = new AtomicInteger(0);
        for (int i = 0; i < 100; i++) {
            testWithPool(i, invokes, pool);
        }

        Thread.sleep(5000);
        System.out.println("invokes:"+invokes.get());
        System.out.println("uses:"+uses.get());
    }

    private void testWithPool(int idx, AtomicInteger invokes, Pool<MyResource> pool) {
        new Thread(() -> {
            try {
                invokes.addAndGet(1);
                pool.withResource(myResource -> {
                    System.out.println("Thread-" + idx + " invoking " + myResource.name + " at: " + nowString());
                    myResource.uses.addAndGet(1);
                    return null;
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }
}
