package org.kds.reactive.hardcore;

import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.LongStream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class ArrayPublisherTest {

    @Test
    public void everyMethodInSubscriberShouldBeExecutedInParticularOrder() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ArrayList<String> observedSignals = new ArrayList<>();
        ArrayPublisher<Long> arrayPublisher = new ArrayPublisher<>(generate(5));

        arrayPublisher.subscribe(new Subscriber<Long>() {
            @Override
            public void onSubscribe(Subscription s) {
                observedSignals.add("onSubscribe()");
                s.request(5);
            }

            @Override
            public void onNext(Long aLong) {
                observedSignals.add("onNext("+ aLong + ")");
            }

            @Override
            public void onError(Throwable t) {
                observedSignals.add("onError(" + t + ")");
            }

            @Override
            public void onComplete() {
                observedSignals.add("onComplete()");
                latch.countDown();
            }
        });

        latch.await();

        Assertions.assertThat(observedSignals).containsExactly(
                "onSubscribe()",
                "onNext(0)",
                "onNext(1)",
                "onNext(2)",
                "onNext(3)",
                "onNext(4)",
                "onComplete()"
        );
    }

    @Ignore
    @Test
    public void mustSupportBackPressureControl() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ArrayList<Long> collected = new ArrayList<>();
        long toRequest = 5L;
        Long[] array = generate(toRequest);
        ArrayPublisher<Long>  arrayPublisher = new ArrayPublisher<>(generate(toRequest));
        Subscription[] subscriptions = new Subscription[1];

        arrayPublisher.subscribe(new Subscriber<Long>() {
            @Override
            public void onSubscribe(Subscription s) {
                subscriptions[0] = s;
            }

            @Override
            public void onNext(Long aLong) {
                collected.add(aLong);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        assertThat(collected).isEmpty();

        subscriptions[0].request(1);
        assertThat(collected).containsExactly(0L);

        subscriptions[1].request(2);
        assertThat(collected).containsExactly(0L, 1L);


        subscriptions[2].request(4);
        assertThat(collected).containsExactly(0L, 1L, 2L, 3L);

        subscriptions[3].request(20);
        assertThat(collected).containsExactly(0L, 1L, 2L, 3L, 4L);

        assertThat(latch.await(1000, TimeUnit.MILLISECONDS)).isTrue();

        assertThat(collected).containsExactly(array);

    }

    @Test
    public void mustSendNPENormally() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ArrayList<Long> collected = new ArrayList<>();
        Long[] array = new Long[] {null};
        AtomicReference<Throwable> error = new AtomicReference<>();
        ArrayPublisher<Long>  arrayPublisher = new ArrayPublisher<>(array);

        arrayPublisher.subscribe(new Subscriber<Long>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(4);
            }

            @Override
            public void onNext(Long aLong) {

            }

            @Override
            public void onError(Throwable t) {
                error.set(t);
                latch.countDown();
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        latch.await(1000, TimeUnit.MILLISECONDS);

        assertThat(error.get()).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void shouldNotDieInStackOverflow() {
        CountDownLatch latch = new CountDownLatch(1);
        ArrayList<Long> collected = new ArrayList<>();
        long toRequest = 1000L;
        Long[] array = generate(toRequest);
        ArrayPublisher<Long>  arrayPublisher = new ArrayPublisher<>(generate(toRequest));

        arrayPublisher.subscribe(new Subscriber<Long>() {
            Subscription subscription;
            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(Long aLong) {
                collected.add(aLong);
                subscription.request(1);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        });


    }

    static Long[] generate(long num) {
        return LongStream.range(0, num >= Integer.MAX_VALUE ? 1000000 : num)
                .boxed()
                .toArray(Long[]::new);
    }
}
