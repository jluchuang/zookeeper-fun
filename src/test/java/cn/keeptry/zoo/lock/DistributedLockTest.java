package cn.keeptry.zoo.lock;

import org.apache.zookeeper.KeeperException;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by chuang on 2017/8/25.
 */
public class DistributedLockTest {

    @Test
    public void distributedLockTest() {
        ExecutorService executor = Executors.newCachedThreadPool();
        final int count = 50;
        final CountDownLatch latch = new CountDownLatch(count);
        final AtomicInteger succeedCount = new AtomicInteger(0);
        final AtomicInteger failedCount = new AtomicInteger(0);
        for (int i = 0; i < count; i++) {
            final DistributedLock node = new DistributedLock("/locks");
            executor.submit(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(2000);
                        node.tryLock(); // 无阻塞获取锁
//                        node.lock(); // 阻塞获取锁
                        Thread.sleep(100);

                        System.out.println("id: " + node.getId() + " is leader: " +
                                node.isOwner() + ":" + succeedCount.incrementAndGet());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (KeeperException e) {
                        System.out.println("Failed :" + failedCount.incrementAndGet());
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                        try {
                            node.unlock();
                        } catch (KeeperException e) {
                            e.printStackTrace();
                        }
                    }

                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executor.shutdown();
    }

}
