package org.kds.reactive.hardcore;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

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

        Assertions.assertThat(latch.await(1000, TimeUnit.MILLISECONDS)).isTrue();

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

    static Long[] generate(long num) {
        return LongStream.range(0, num >= Integer.MAX_VALUE ? 1000000 : num)
                .boxed()
                .toArray(Long[]::new);
    }
}
