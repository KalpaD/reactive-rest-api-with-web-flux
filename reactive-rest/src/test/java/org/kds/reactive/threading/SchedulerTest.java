package org.kds.reactive.threading;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.CountDownLatch;

public class SchedulerTest {

    private static Logger LOG = LoggerFactory.getLogger(SchedulerTest.class);

    CountDownLatch countDownLatch = new CountDownLatch(2);

    /**
     * 1. Placement of publishOn() in the chain matters
     *
     * 2. Once subscribe a chain of Subscriber objects is created , backward to the first publisher.
     *
     * 3. publishOn applies in the sam way as any other operator, in the middle of the subscriber chain.
     * It takes signals from the upstream and replays them downstream while executing the call back on a worker from the associated Scheduler.
     * Hence, it affects where the subsequent operators will execute.
     *
     * result
     * map op 1 thread name :Runner Thread
     * map op 2 thread name :parallel-scheduler-1
     */
    @Test
    public void testPublishOn() throws InterruptedException {
        countDownLatch = new CountDownLatch(1);

        Scheduler scheduler = Schedulers.newParallel("parallel-scheduler", 4);

        Flux<String> flux = Flux.just(1)
                .map(i -> {
                     LOG.info("map op 1 thread name :" + Thread.currentThread().getName());
                     return 10 + i;
                })
                // publishOn() affects where the subsequent operators will execute
                .publishOn(scheduler)
                // the following map operation runs on the provided scheduler since
                // that is positioned in the chain after publishOn(scheduler)
                .map(i -> {
                    LOG.info("map op 2 thread name :" + Thread.currentThread().getName());
                    return "value = " + i;
                });

        new Thread(() -> flux.subscribe( e -> {
            countDownLatch.countDown();
            System.out.println(e);
        }), "Runner Thread").start();

        countDownLatch.await();
    }

    /**
     * subscribeOn() always affects the context of the source emission.
     * However, it does not affect the behavior of subsequent calls to publishOn.
     *
     * result
     * map op 1 thread name :parallel-scheduler-1
     * map op 2 thread name :parallel-scheduler-1
     */
    @Test
    public void testSubscribeOn() throws InterruptedException {
        countDownLatch = new CountDownLatch(1);

        Scheduler scheduler = Schedulers.newParallel("parallel-scheduler", 4);

        final Flux<String> flux = Flux.just(1)
                .map(i -> {
                    LOG.info("map op 1 thread name :" + Thread.currentThread().getName());
                    return 10 + i;
                })
                .subscribeOn(scheduler)
                .map(i -> {
                    LOG.info("map op 2 thread name :" + Thread.currentThread().getName());
                    return "value = " + i;
                });

        new Thread(() -> flux.subscribe( e -> {
            countDownLatch.countDown();
            System.out.println(e);
        }), "Runner Thread").start();

        countDownLatch.await();
    }
}
