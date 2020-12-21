package me.luvletter.planechess.client.previewing;

import me.luvletter.planechess.game.LocalClient;

public abstract class PreviewAction {

    /**
     * true -> move preview, false ->
     * */
    public final PreviewType previewType;
    protected final LocalClient client;

    public PreviewAction(PreviewType previewType, LocalClient client) {
        this.previewType = previewType;
        this.client = client;
    }

    public abstract boolean apply();

}