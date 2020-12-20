package me.luvletter.planechess.client.previewing;

import me.luvletter.planechess.server.LocalClient;

public class TakeOffPreviewAction extends PreviewAction {
    public TakeOffPreviewAction(LocalClient client) {
        super(PreviewType.TakeOff, client);
    }

    @Override
    public boolean apply() {
        this.client.takeOff();
        return true;
    }
}
