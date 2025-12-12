package com.gameify.game;

import com.gameify.player.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class Game<T extends Player> {
    protected final List<T> players;
    private final List<T> queuedPlayers;

    public Game(List<T> players) {
        onPreStart();
        this.players = players;
        this.players.forEach(this::onJoin);
        this.queuedPlayers = new ArrayList<>();
    }

    /**
     * Called on instantiation of the game (first use).
     * @implNote {@code players} will be null on call
     */
    public abstract void onPreStart();
    /**
     * Called on the start of the game.
     */
    public abstract void onStart();
    /**
     * Called on joining of a player.
     */
    public abstract void onJoin(T p);
    /**
     * Called on late joining of a player.
     */
    public abstract void onLateJoin(T p);
    /**
     * Called on every update to the {@code GameRunner}.
     */
    public abstract void update();
    /**
     * Called on {@code GameRunner.end()}.
     */
    public abstract void onEnd();
    /**
     * Pushes a player into the late list.
     * @param plr player to add
     */
    public void pushPlayer(T plr) {
        this.queuedPlayers.add(plr);
    }
    /**
     * Called by the {@code GameRunner} on the end of an update.
     * @apiNote Include a super call if adding custom logic.
     */
    public void postUpdate() {
        if(!queuedPlayers.isEmpty()) {
            queuedPlayers.forEach(this::onLateJoin);
            queuedPlayers.clear();
        }
    }
    /**
     * Retrieves the players currently playing the game.
     * @return A list of players playing
     */
    public List<T> getPlayers() {
        return new ArrayList<>(this.players);
    }
}
