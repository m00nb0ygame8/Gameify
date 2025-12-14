package com.gameify.game;

import com.gameify.player.Player;
import com.gameify.hook.GameifyHook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class GameRunner<T extends Player> {
    private final Map<String, Function<List<T>, Game<T>>> games;
    private final List<T> players;

    private State state;
    private Game<T> curGame;

    public GameRunner(GameifyHook hook) {
        this.games = new HashMap<>();
        this.players = new ArrayList<>();

        this.state = State.IDLE;
        this.curGame = null;

        hook.register("update", this::update);
    }

    public void update() {
        if(this.state == State.PLAYING) {
            this.curGame.update();
            this.curGame.postUpdate();
        }
    }

    public void registerGame(String gId, Function<List<T>, Game<T>> game) {
        this.games.put(gId, game);
    }

    public void registerGames(Map<String, Function<List<T>, Game<T>>> games) {
        this.games.putAll(games);
    }

    public void addPlayer(T plr) {
        if(this.state == State.IDLE) this.players.add(plr);
        else this.curGame.pushPlayer(plr);
    }

    /**
     * Starts a game if the runner is {@code IDLE}.
     * @param gId The game id of the game.
     */
    public void start(String gId) {
        if(this.state == State.IDLE) {
            if(!this.games.containsKey(gId)) throw new IllegalArgumentException("Unknown game id \"%s\"".formatted(gId));
            this.curGame = this.games.get(gId).apply(this.players);
            this.curGame.onStart();
            this.state = State.PLAYING;
        }
    }

    /**
     * Ends a game if the runner is {@code PLAYING}.
     */

    public void end() {
        if(this.state == State.PLAYING) {
            this.curGame.onEnd();
            this.curGame = null;
            this.state = State.IDLE;
        }
    }

    public State getState() {
        return this.state;
    }

    public enum State {
        IDLE,
        PLAYING
    }
}
