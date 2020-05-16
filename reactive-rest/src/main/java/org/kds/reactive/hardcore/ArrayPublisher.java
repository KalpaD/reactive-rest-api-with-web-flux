package org.kds.reactive.hardcore;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class ArrayPublisher<T> implements Publisher<T> {

    private final T[] array;

    public ArrayPublisher(T[] array) {
        this.array = array;
    }

    @Override
    public void subscribe(Subscriber<? super T> subscriber) {
        subscriber.onSubscribe(new Subscription() {
            int index;
            long requested;
            @Override
            public void request(long n) {
                long initialRequested = requested;
                requested += n;

                if (initialRequested != 0) {
                    return;
                }

                int sent = 0;

                for (; sent < requested && index < array.length; sent++, index++) {
                    T element  = array[index];

                    if (element == null) {
                        subscriber.onError(new NullPointerException());
                        return;
                    }
                    subscriber.onNext(array[index]);
                }

                if (index == array.length) {
                    subscriber.onComplete();
                    return;
                }
            }

            @Override
            public void cancel() {

            }
        });
    }
}
