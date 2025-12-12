package com.gameify.game;

import com.gameify.player.Player;

import java.util.*;
import java.util.function.Function;

public class GamePlaylist<T extends Player> {
    private final Map<String, Function<List<T>, Game<T>>> playlist;

    public GamePlaylist() {
        this.playlist = new HashMap<>();
    }

    public GamePlaylist<T> withGame(String gId, Function<List<T>, Game<T>> game) {
        this.playlist.put(gId, game);
        return this;
    }

    public GamePlaylist<T> withAllGames(Map<String, Function<List<T>, Game<T>>> games) {
        this.playlist.putAll(games);
        return this;
    }

    /**
     * Samples games randomly from the playlist and returns them.
     * @param sampleSize The number of games to sample
     * @return Randomly sampled games
     */
    public Map<String, Function<List<T>, Game<T>>> sample(int sampleSize) {
        if(this.playlist.size() <= sampleSize) return compileAll();

        List<String> gIds = new ArrayList<>(this.playlist.keySet());
        Collections.shuffle(gIds);
        Map<String, Function<List<T>, Game<T>>> out = new HashMap<>();
        gIds.subList(0, sampleSize).forEach(s -> out.put(s, this.playlist.get(s)));
        return out;
    }

    /**
     * Compiles all the games in the playlist.
     * @return Games in the playlist
     */
    public Map<String, Function<List<T>, Game<T>>> compileAll() {
        return new HashMap<>(this.playlist);
    }
}
