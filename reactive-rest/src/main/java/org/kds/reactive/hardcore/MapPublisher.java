package org.kds.reactive.hardcore;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.function.Function;

public class MapPublisher<IN, OUT> implements Publisher<OUT> {

    final Publisher<IN> parent;
    final Function<IN, OUT> mapper;

    public MapPublisher(Publisher<IN> parent, Function<IN, OUT> mapper) {
        this.parent = parent;
        this.mapper = mapper;
    }

    @Override
    public void subscribe(Subscriber<? super OUT> actual) {
        // this is what happens at the assembly time
        parent.subscribe(new MapSubscriber<>(mapper, actual));
    }

    static class MapSubscriber<IN, OUT> implements Subscriber<IN> {

        final Function<IN, OUT> mapper;
        final Subscriber<? super OUT> actualSubscriber;

        public MapSubscriber(Function<IN, OUT> mapper,
                             Subscriber<? super OUT> actualSubscriber) {
            this.mapper = mapper;
            this.actualSubscriber = actualSubscriber;
        }

        @Override
        public void onSubscribe(Subscription s) {
            actualSubscriber.onSubscribe(s);
        }

        @Override
        public void onNext(IN inputValue) {
            OUT outputValue = mapper.apply(inputValue);
            actualSubscriber.onNext(outputValue);
        }

        @Override
        public void onError(Throwable t) {
            actualSubscriber.onError(t);
        }

        @Override
        public void onComplete() {
            actualSubscriber.onComplete();
        }
    }
}
