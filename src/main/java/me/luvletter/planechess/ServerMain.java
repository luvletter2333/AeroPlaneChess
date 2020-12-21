package me.luvletter.planechess;

import me.luvletter.planechess.server.Server;

public class ServerMain {
    public static void main(String[] args) {
        Server server = new Server("127.0.0.1", 11451); //1145141919810 哼哼 啊啊啊啊啊啊啊啊啊
        server.start();

    }
}
