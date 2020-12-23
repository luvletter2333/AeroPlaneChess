# Aeroplane Chess
Final Project for CS102-F20


## Features

- C/S分离，Game与UI都有各自独立的线程，之间通信使用事件队列处理 
- UI更新/动画均由UIthread处理，而不是Timer，可以轻松处理动画时的产生C/S事件
- Game游戏逻辑由单独线程维护
- Websocket based server-side, 支持多Game Room, Socket断线后自动接入DummyAI
- C/S通信以及存读档使用阿里的fastjson
- 非常弱智的动画 ~~演示的时候还出bug了~~

## Requirements

- **每人roll两次** 
- roll到6才能起飞
- 顺时针移动
- 移动结果为两次结果的四则运算（一定要产生整数，int(4/3)并不是一个valid的移动）
- 当飞机降落在对手的飞机上，对手飞机返回机库
- 降落到同一颜色的格子上时，立即跳至下一个同颜色块, 在这些颜色块上的其他飞机立刻滚回机场

- License

MIT License
