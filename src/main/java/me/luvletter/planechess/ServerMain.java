package me.luvletter.planechess;

import me.luvletter.planechess.server.Server;

import java.util.ArrayDeque;
import java.util.Scanner;

public class ServerMain {
    public static void main(String[] args) {
        Server server = new Server("127.0.0.1", 11451, "野兽先辈的飞行棋"); //1145141919810 哼哼 啊啊啊啊啊啊啊啊啊
        server.start();

        String str;
        var input = new Scanner(System.in);

        while (true) {
            str = input.nextLine();
            try {
                var cmds = str.split(" ");
                switch (cmds[0]) {
                    case "status" -> System.out.println(server);
                    case "quit" ->{
                        server.stop(1000);
                        System.exit(0);
                    }
                }
                //System.out.println("[Server] run debug command: " + str);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
