package Players;

import Main.Game;
import Main.GameState;
import Utilities.AlphaBetaTreeNode;
import Utilities.Mergesort;

import java.util.Collections;
import java.util.List;

public class AlphaBetaPlayer implements Player {

    private final static int maxDepth = 3;

    private final int ID;
    private final String objectiveFunction;
    private boolean stuck;

    public AlphaBetaPlayer(int id, String objectiveFunction) {
        this.ID = id;
        this.objectiveFunction = objectiveFunction;
        this.stuck = false;
    }

    public String getName() {
        return "AlphaBeta";
    }

    public int getID() {
        return ID;
    }

    public String getObjectiveFunction() {
        return objectiveFunction;
    }

    public boolean isStuck() {
        return stuck;
    }

    public void stuck() {
        this.stuck = true;
    }

    public void unstuck() {
        this.stuck = false;
    }

    public GameState makeMove(GameState currentState) {
        AlphaBetaTreeNode root = new AlphaBetaTreeNode(currentState, this);

        AlphaBetaTreeNode bestNode = buildAndSearchTree(root, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        // If necessary, backtrack the best node through the tree until a depth of 1 is reached
        if (bestNode.getDepth() > 0) {
            while (bestNode.getDepth() > 1) {
                bestNode = bestNode.getParent();
            }
        }

        return bestNode.getState();
    }

    private void buildTree(AlphaBetaTreeNode node) {
        if (node.getDepth() < maxDepth) {
            List<GameState> moves = node.getMoves(this, objectiveFunction);

            Player nextPlayer = Game.nextPlayer(node.getPlayer());
            for (GameState move : moves) {
                AlphaBetaTreeNode child = new AlphaBetaTreeNode(node, move, nextPlayer);
                buildTree(child);
            }
        }
    }

    private AlphaBetaTreeNode searchTree(AlphaBetaTreeNode node, double alpha, double beta) {
        if (node.getChildren().size() == 0) {
            return node;
        }
        AlphaBetaTreeNode valueNode;
        if (node.getPlayer() == this) {
            // Maximizing player
            valueNode = new AlphaBetaTreeNode(Double.NEGATIVE_INFINITY);

            for (AlphaBetaTreeNode child : node.getChildren()) {
                AlphaBetaTreeNode recursiveNode = searchTree(child, alpha, beta);
                if (recursiveNode.getScore() > valueNode.getScore()) {
                    valueNode = recursiveNode;
                }

                if (valueNode.getScore() >= beta) {
                    break;
                }

                alpha = Math.max(alpha, valueNode.getScore());
            }
        } else {
            // Minimizing player
            valueNode = new AlphaBetaTreeNode(Double.POSITIVE_INFINITY);

            for (AlphaBetaTreeNode child : node.getChildren()) {
                AlphaBetaTreeNode recursiveNode = searchTree(child, alpha, beta);
                if (recursiveNode.getScore() < valueNode.getScore()) {
                    valueNode = recursiveNode;
                }

                if (valueNode.getScore() <= alpha) {
                    break;
                }

                beta = Math.min(beta, valueNode.getScore());
            }
        }
        return valueNode;
    }

    private AlphaBetaTreeNode buildAndSearchTree(AlphaBetaTreeNode node, double alpha, double beta) {
        if (node.getDepth() == maxDepth) {
            return node;
        }

        List<GameState> moves = node.getMoves(this, objectiveFunction);
        if (moves.size() == 0) {
            return node;
        }

        Player nextPlayer = Game.nextPlayer(node.getPlayer());
        for (GameState move : moves) {
            new AlphaBetaTreeNode(node, move, nextPlayer);
        }
        List<AlphaBetaTreeNode> sortedChildren = Mergesort.mergesort(node.getChildren());

        AlphaBetaTreeNode valueNode;
        if (node.getPlayer() == this) {
            // Maximizing player
            valueNode = new AlphaBetaTreeNode(Double.NEGATIVE_INFINITY);

            Collections.reverse(sortedChildren);
            for (AlphaBetaTreeNode child : sortedChildren) {
                AlphaBetaTreeNode recursiveNode = buildAndSearchTree(child, alpha, beta);
                if (recursiveNode.getScore() > valueNode.getScore()) {
                    valueNode = recursiveNode;
                }

                if (valueNode.getScore() >= beta) {
                    break;
                }

                alpha = Math.max(alpha, valueNode.getScore());
            }
        } else {
            // Minimizing player
            valueNode = new AlphaBetaTreeNode(Double.POSITIVE_INFINITY);

            for (AlphaBetaTreeNode child : sortedChildren) {
                AlphaBetaTreeNode recursiveNode = buildAndSearchTree(child, alpha, beta);
                if (recursiveNode.getScore() < valueNode.getScore()) {
                    valueNode = recursiveNode;
                }

                if (valueNode.getScore() <= alpha) {
                    break;
                }

                beta = Math.min(beta, valueNode.getScore());
            }
        }
        return valueNode;
    }

    public boolean checkWin(GameState state) {
        return state.getRACKS().get(this).size() == 0;
    }

}
