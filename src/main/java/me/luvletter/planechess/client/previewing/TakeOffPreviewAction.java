package me.luvletter.planechess.client.previewing;

import me.luvletter.planechess.game.GameClient;
import me.luvletter.planechess.game.LocalClient;

public class TakeOffPreviewAction extends PreviewAction {
    public TakeOffPreviewAction(GameClient client) {
        super(PreviewType.TakeOff, client);
    }

    @Override
    public boolean apply() {
        this.client.takeOff();
        return true;
    }
}
