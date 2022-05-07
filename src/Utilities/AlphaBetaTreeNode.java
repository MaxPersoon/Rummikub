package Utilities;

import Main.GameState;
import Players.Player;

import java.util.ArrayList;
import java.util.List;

public class AlphaBetaTreeNode {

    private final AlphaBetaTreeNode parent;
    private final List<AlphaBetaTreeNode> children;
    private final GameState state;
    private final Player player; // The player who is allowed to make a move in this state
    private final int depth;
    private final double score;

    public AlphaBetaTreeNode(GameState state, Player player) {
        this.parent = null;
        this.children = new ArrayList<>();
        this.state = state;
        this.player = player;
        this.depth = 0;
        this.score = 0;
    }

    public AlphaBetaTreeNode(AlphaBetaTreeNode parent, GameState state, Player player) {
        this.parent = parent;
        this.parent.addChild(this);
        this.children = new ArrayList<>();
        this.state = state;
        this.player = player;
        this.depth = parent.depth + 1;
        this.score = parent.score + state.getScore();
        state.setScore(this.score);
    }

    public AlphaBetaTreeNode(double score) {
        this.parent = null;
        this.children = null;
        this.state = null;
        this.player = null;
        this.depth = -1;
        this.score = score;
    }

    public AlphaBetaTreeNode getParent() {
        return parent;
    }

    public List<AlphaBetaTreeNode> getChildren() {
        return children;
    }

    public GameState getState() {
        return state;
    }

    public Player getPlayer() {
        return player;
    }

    public int getDepth() {
        return depth;
    }

    public double getScore() {
        return score;
    }

    public void addChild(AlphaBetaTreeNode child) {
        this.children.add(child);
    }

    public List<GameState> getMoves(Player playerScore, String objectiveFunction) {
        state.setDepth(1);
        return state.getMoves(player, playerScore, objectiveFunction, 50);
    }

}
