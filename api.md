
## Server Side Api

### ping

{"action":"ping"}

- Return: {"data":"pong!","status":200}


### list_games

{"action":"list_games"}

{"data":{},"status":200}


### create_game

{
    "action":"create_game",
    "player_ids":[1,2],
    "real_player_ids":[1],
    "room_name":"test"
}

{"uuid":"xxxxx",
"status":200}

### game

#### action:

##### move

- int plane_id

- int step

- boolean go_stack

#### skip

#### takeoff

#### battle

- int plane_id

- int takeoff


## Server -> Client:

# action: eventNames
# data : Map