package me.luvletter.planechess.client.previewing;

import me.luvletter.planechess.game.GameClient;
import me.luvletter.planechess.game.LocalClient;

import java.util.List;

public class MovePreviewAction extends PreviewAction {
    public final int planeID;
    public final int sourcePos;
    public final List<Integer> possibleMove;

    public MovePreviewAction(GameClient client, int planeID, int sourcePos, List<Integer> possibleMove) {
        super(PreviewType.Move, client);
        this.planeID = planeID;
        this.sourcePos = sourcePos;
        this.possibleMove = possibleMove;
    }

    @Override
    public boolean apply() {
        return false;
    }

    public void apply(int step, boolean goStack) {
        this.client.move(this.planeID, step, goStack);
    }
}