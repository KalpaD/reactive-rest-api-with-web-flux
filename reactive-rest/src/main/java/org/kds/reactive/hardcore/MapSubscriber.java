package org.kds.reactive.hardcore;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.function.Function;

public class MapSubscriber<IN, OUT> implements Subscriber<IN> {

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
