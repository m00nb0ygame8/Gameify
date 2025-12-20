package com.gameify.game.adaptive;

import com.gameify.game.Game;
import com.gameify.game.GameRunner;
import com.gameify.hook.GameifyHook;
import com.gameify.player.Player;
import com.gameify.player.PlayerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class AdaptiveGameRunner {
    private final List<GenericGameRunner<? extends Player>> gameRunners;
    private final List<TransitionPair<? extends Player>> transitionPairs;
    private final Map<String, GenericGameRunner<? extends Player>> ids;

    private List<Player> curPlayers;
    private State state;
    private GameRunner<Player> curRunner;
    private Class<? extends Player> curPlayerType;

    public AdaptiveGameRunner(GameifyHook hook) {
        this.curPlayers = new ArrayList<>();
        this.gameRunners = new ArrayList<>();
        this.transitionPairs = new ArrayList<>();
        this.ids = new HashMap<>();

        this.state = State.IDLE;

        hook.register("update", this::update);
    }


    public <T extends Player> void registerRunnerType(Class<T> type, GameRunner<T> runner, Function<Player, T> adapter) {
        this.gameRunners.add(new GenericGameRunner<>(type, runner));
        this.transitionPairs.add(new TransitionPair<>(type, adapter));
    }

    public <T extends Player> void registerGame(Class<T> type, String gId, Function<List<T>, Game<T>> game) {
        //noinspection unchecked
        GenericGameRunner<T> runner = this.gameRunners.stream()
                .filter(ggr -> ggr.type().isAssignableFrom(type))
                .map(ggr -> (GenericGameRunner<T>) ggr)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No game runner registered for player type \"%s!\"".formatted(type.getSimpleName())));
        runner.runner().registerGame(gId, game);
        this.ids.put(gId, runner);
    }

    @SuppressWarnings("unchecked")
    public <T extends Player> void start(String gId, Class<T> gamePlrType) {
        GenericGameRunner<?> gRunner = this.ids.get(gId);
        if(gRunner == null || !gRunner.type().isAssignableFrom(gamePlrType)) throw new IllegalArgumentException("No game with gId \"%s\" and player type \"%s\" exists!".formatted(gId, gamePlrType.getSimpleName()));
        GenericGameRunner<T> runner = (GenericGameRunner<T>) gRunner;
        TransitionPair<T> transitionPair = this.transitionPairs
                .stream().filter(tPair -> tPair.postType().isAssignableFrom(gamePlrType))
                .map(tPair -> (TransitionPair<T>) tPair)
                .findFirst().orElseThrow(() -> new IllegalStateException("No transition pair resulting in \"%s\" exists!".formatted(gamePlrType.getSimpleName())));
        this.curPlayerType = gamePlrType;
        this.curRunner = (GameRunner<Player>) runner.runner();
        this.curPlayers = new ArrayList<>(this.curPlayers.stream().map(transitionPair::adapt).toList());
        this.state = State.PLAYING;
        ((GameRunner<T>)this.curRunner).addPlayers((List<T>) this.curPlayers);
        this.curRunner.start(gId);
    }


    public void update() {
        if(this.state == State.PLAYING) this.curRunner.update();
    }

    public <T extends Player> void addPlayer(Class<T> plrType, T plr) {
        if (this.state == State.IDLE) {
            if (!this.curPlayerType.isAssignableFrom(plrType)) {
                throw new IllegalArgumentException(
                        "Player type %s not compatible with %s"
                                .formatted(plrType.getSimpleName(),
                                        curPlayerType.getSimpleName())
                );
            }
            this.curPlayers.add(plr);
            return;
        }

        @SuppressWarnings("unchecked")
        TransitionPair<T> transitionPair = this.transitionPairs.stream()
                .filter(tp -> tp.postType().isAssignableFrom(this.curPlayerType))
                .map(tp -> (TransitionPair<T>) tp)
                .findFirst()
                .orElseThrow();

        T adapted = transitionPair.adapt(plr);

        @SuppressWarnings("unchecked")
        GameRunner<T> runner = (GameRunner<T>) this.curRunner;

        runner.addPlayer(adapted);
    }
    public <T extends Player> void addPlayers(Class<T> plrType, List<T> players) {
        if (this.state == State.IDLE) {
            if (!this.curPlayerType.isAssignableFrom(plrType)) {
                throw new IllegalArgumentException(
                        "Player type %s not compatible with %s"
                                .formatted(plrType.getSimpleName(),
                                        curPlayerType.getSimpleName())
                );
            }
            this.curPlayers.addAll(players);
            return;
        }

        @SuppressWarnings("unchecked")
        TransitionPair<T> transitionPair = this.transitionPairs.stream()
                .filter(tp -> tp.postType().isAssignableFrom(this.curPlayerType))
                .map(tp -> (TransitionPair<T>) tp)
                .findFirst()
                .orElseThrow();

        List<T> adapted = players.stream()
                .map(transitionPair::adapt)
                .toList();

        @SuppressWarnings("unchecked")
        GameRunner<T> runner = (GameRunner<T>) this.curRunner;

        runner.addPlayers(adapted);
    }

    public void kickPlayer(Player plr) {
        if(this.state == State.PLAYING) this.curRunner.kickPlayer(plr);
        this.curPlayers.remove(plr);
    }

    public void end() {
        if(this.state == State.PLAYING) {
            this.curRunner.end();
            this.state = State.IDLE;
        }
    }

    private record TransitionPair<Q extends Player>(Class<Q> postType, Function<Player, Q> adapter) {
        public Q adapt(Player plr) {
             PlayerAdapter<Player, Q> plrAdapter = new PlayerAdapter<>(adapter);
             return plrAdapter.adapt(plr);
        }
    }

    private record GenericGameRunner<T extends Player>(Class<T> type, GameRunner<T> runner) {}

    public State getState() {
        return this.state;
    }

    public enum State {
        IDLE,
        PLAYING
    }
}
