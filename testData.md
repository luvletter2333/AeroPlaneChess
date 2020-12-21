
## 跳跃+stack动画

forceMoveTo 11 100

forceMoveTo 12 101

forceMoveTo 13 102

forceMove 11 2 true

restart


## 吃子动画

forceMoveTo 11 404

forceMoveTo 21 101

forceMoveTo 22 102

forceMove 11 1 true

restart
## 吃子动画2


forceMoveTo 11 404

forceMoveTo 21 101

forceMoveTo 22 307

forceMove 22 2 true

forceMoveTo 23 102

forceMove 11 1 true
restart

## 吃子动画3

forceMoveTo 11 103

forceMoveTo 12 104

forceMoveTo 13 105

forceMoveTo 31 315

forceMoveTo 32 316

forceMove 31 1 true

forceMove 11 4 true
restart
## 吃子动画4

forceMoveTo 11 103
forceMoveTo 12 104
forceMoveTo 13 105
forceMoveTo 31 315
forceMoveTo 32 316
forceMove 31 1 true
forceMoveTo 41 108
forceMoveTo 42 411
forceMove 42 1 true
forceMove 11 4 true
restart

## 移动动画

forceMoveTo 44 413

forceMove 44 2 true

forceMoveTo 41 414

forceMove 41 1 true
restart
## 移动动画 2

forceMoveTo 41 303

forceMove 41 1 true

## 终点移动动画

forceMoveTo 22 217
forceMoveTo 21 218
forceMove 22 1 true
forceMove 22 1 true
forceMoveTo 11 100 true
restart

## Skip All

skip 1
skip 2
skip 3
skip 4

## 移动

forceDice 1 1
forceMoveTo 11 101
forceMoveTo 12 211
move 11 1 true

# Battle Test File

## Test 1

forceDice 1 1
forceMoveTo 11 101
forceMoveTo 21 211
battle 11 1 true

## Test 2 一坨打一个

cheatDice 1 2 1 6


forceMoveTo 11 101
forceMoveTo 12 211
forceMove 11 1 true
forceMoveTo 21 308
skip 1
skip 2
forceDice 1 1
cheatDice 1 2 1 6
battle 12 1

## Test 2 一坨打一个

cheatDice 1 2 1 6


forceMoveTo 11 101
forceMoveTo 12 211
forceMove 11 1 true
forceMoveTo 21 308
skip 1
skip 2
forceDice 1 1
cheatDice 2 1
battle 12 1


## Test3 一坨打一坨

cheatDice 1 2 1 6

forceMoveTo 11 101
forceMoveTo 12 211
forceMove 11 1 true

forceMoveTo 21 102
forceMoveTo 22 405
forceMove 22 1 true

skip 1
skip 2
forceDice 2 1
cheatDice 1 6 5 2 3 2 
battle 11 3

