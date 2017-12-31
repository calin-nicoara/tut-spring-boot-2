package com.cni.tutspringboot2.sandbox;

import java.util.Comparator;

import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;

public class FluxEx {

    public void fluxTry() {
        Flux.just("alpha", "bravo", "charlie")
                .map(String::toUpperCase)
                .flatMap(s -> Flux.fromArray(s.split("")))
                .groupBy(String::toString)
                .sort(Comparator.comparing(GroupedFlux::key))
                .map(group -> Mono.just(group.key()).and(group.count()))
                .subscribe(System.out::println);

    }
}
