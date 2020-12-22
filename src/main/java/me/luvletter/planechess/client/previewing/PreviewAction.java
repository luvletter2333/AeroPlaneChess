package me.luvletter.planechess.client.previewing;

import me.luvletter.planechess.game.GameClient;
import me.luvletter.planechess.game.IGame;

public abstract class PreviewAction {

    /**
     * true -> move preview, false ->
     * */
    public final PreviewType previewType;
    protected final IGame game;

    public PreviewAction(PreviewType previewType, IGame game) {
        this.previewType = previewType;
        this.game = game;
    }

    public abstract boolean apply();

}
