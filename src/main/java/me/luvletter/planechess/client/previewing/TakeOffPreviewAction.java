package me.luvletter.planechess.client.previewing;

import me.luvletter.planechess.game.IGame;

public class TakeOffPreviewAction extends PreviewAction {
    private int playerID;

    public TakeOffPreviewAction(int playerID, IGame game) {
        super(PreviewType.TakeOff, game);
        this.playerID = playerID;
    }

    @Override
    public boolean apply() {
        this.game.takeOff(this.playerID);
        return true;
    }
}
