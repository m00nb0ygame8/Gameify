package com.gameify.player;

import java.util.function.Consumer;
import java.util.function.Function;

public class PlayerAdapter<T extends Player, Q extends Player> {
    private final Function<T, Q> adapter;
    private Consumer<T> inProcessor = _ -> {};
    private Consumer<Q> outProcessor = _ -> {};

    public PlayerAdapter(Function<T, Q> adapter) {
        this.adapter = adapter;
    }

    public PlayerAdapter<T, Q> inProcessor(Consumer<T> processor) {
        this.inProcessor = processor;
        return this;
    }

    public PlayerAdapter<T, Q> outProcessor(Consumer<Q> processor) {
        this.outProcessor = processor;
        return this;
    }

    public Q adapt(T plr) {
        inProcessor.accept(plr);
        Q outPlr = adapter.apply(plr);
        outProcessor.accept(outPlr);
        return outPlr;
    }
}
