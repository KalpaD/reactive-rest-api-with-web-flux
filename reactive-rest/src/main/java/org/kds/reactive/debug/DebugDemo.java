package org.kds.reactive.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;

import java.util.Arrays;
import java.util.List;

public class DebugDemo {

    private static Logger log = LoggerFactory.getLogger(DebugDemo.class);

    public static void main(String[] args) {
        debuggingUsingLogOperator();
        //debuggingUsingCheckpoints();
        //debuggingUsingHook();
    }

    private static void debuggingUsingLogOperator() {
        List<String> nameList = Arrays.asList("Rochel", "April", "Hong");

        Flux<String> stringFlux = Flux.fromIterable(nameList)
                .distinct()
                .map(name -> name.substring(0, 3))
                .map(String::toUpperCase)
                .log();

        stringFlux.subscribe(log::info);
    }

    private static void debuggingUsingHook() {
        Hooks.onOperatorDebug();

        List<String> nameList = Arrays.asList("Rochel", "April", "Hong");

        Flux<String> stringFlux = Flux.fromIterable(nameList)
                .distinct()
                .map(name -> name.substring(0, 3))
                .map(String::toUpperCase)
                .map(name -> {
                    if (name.equals("HON")) {
                        throw new RuntimeException("Boom!");
                    } else {
                        return name;
                    }
                });

        stringFlux.subscribe(log::info);
    }

    private static void debuggingUsingCheckpoints() {
        List<String> nameList = Arrays.asList("Rochel", "April", "Hong");

        Flux<String> stringFlux = Flux.fromIterable(nameList)
                .distinct()
                .checkpoint("after distinct()")
                .map(name -> name.substring(0, 3))
                .checkpoint("after substring")
                .map(String::toUpperCase)
                .checkpoint("after toUpperCase")
                .map(name -> {
                    if (name.equals("HON")) {
                        throw new RuntimeException("Boom!");
                    } else {
                        return name;
                    }
                })
                .checkpoint("after gate map");

        stringFlux.subscribe(log::info);
    }
}
