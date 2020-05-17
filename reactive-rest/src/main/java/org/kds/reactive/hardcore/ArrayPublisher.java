package org.kds.reactive.hardcore;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ArrayPublisher<T> implements Publisher<T> {

    private final T[] array;

    public ArrayPublisher(T[] array) {
        this.array = array;
    }

    @Override
    public void subscribe(Subscriber<? super T> subscriber) {
        subscriber.onSubscribe(new Subscription() {
            AtomicInteger index = new AtomicInteger();
            // how many elements, the publisher has to publish
            AtomicLong requested = new AtomicLong();
            AtomicBoolean cancelled = new AtomicBoolean();
            @Override
            public void request(long n) {
                if (n <= 0 && !cancelled.get()) {
                    cancel();
                    subscriber.onError(new IllegalArgumentException("n can not be negative"));
                    return;
                }

                // work in progress checker
                // initialRequested always will have the current value of the requested
                long initialRequested = requested.getAndAdd(n);
                if (initialRequested > 0) {
                    // this means publisher is still sending data to someone who
                    // has requested data earlier, hence work in progress.
                    return;
                }

                // how many elements publisher have already sent to
                // subscriber.
                int sent = 0;
                while (true) {
                    for (; sent < requested.get() && index.get() < array.length; sent++, index.incrementAndGet()) {
                        if (cancelled.get()) {
                            return;
                        }

                        T element = array[index.get()];

                        if (element == null) {
                            subscriber.onError(new NullPointerException());
                            return;
                        }
                        subscriber.onNext(array[index.get()]);
                    }

                    if (index.get() == array.length) {
                        subscriber.onComplete();
                        return;
                    }

                    // decrement the requested elements by number of sent elements.
                    long currentStateOfRequested = requested.addAndGet(-sent);
                    if (currentStateOfRequested == 0) {
                        // no more work to do
                        return;
                    }
                    sent = 0;
                }// repeat
            }

            @Override
            public void cancel() {
                cancelled.set(true);
            }
        });
    }
}
