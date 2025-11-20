How to play

Game uses an advanced command language interpreter (called a 'parser') that understands both simple one or two word commands and complex multiple command sentences. This chapter is split into sections describing ways in which to communicate with the program.

1. Movement
   To move around the land, use the following commands:

NORTH (N), NORTHEAST (NE), EAST (E), SOUTHEAST (SE), SOUTH (S), SOUTHWEST (SW), WEST (W), NORTHWEST (NW), UP (U), DOWN (D), INSIDE (IN), OUTSIDE (OUT), CLIMB, JUMP, CROSS

The EXITS command will list any likely exits.

2. Actions

The majority of commands that you will use are actions. Such as picking up objects, opening doors, lighting lamps, etc. Here are some examples of the most common action commands.

Command - Meaning:

GET THE SPANNER - Pick up the spanner from here.  
DROP THE BLACK PISTOL - Leave the black pistol in this room.  
GIVE THE DRINK TO THE ROBOT - Refresh the robot with my drink.  
PUSH THE RED BUTTON - Press only the button that is red.  
WEAR HELMET - Put on the helmet that I'm carrying.  
OPEN THE DOOR - Open the door  
CUT THE TUBE WITH THE SCALPEL - Sever the tube using my scalpel.  
EXAMINE THE COFFIN - Take a good look at the coffin.  
INVENTORY (INV) - What am I carrying?  
SCORE - How well am I doing?  
QUIT - Abandon your quest.  
AGAIN (A) - Repeat the last command entered.  

You can use punctuation, or the word AND to string together multiple commands, for example...

OPEN THE DOOR. GO SOUTH AND CLOSE THE DOOR. GO EAST AND PULL THE GREEN LEVER.

The parser also understands the words ALL and EVERYTHING to mean everything moveable that it can see. This is an extremely useful time-saving feature. In most other adventures, to pick up a number of objects, you would have to do the following...

GET RATCHET  
GET BOLT  
GET CLOTH

Without using the ALL command, game would allow you to use...

GET RATCHET, BOLT AND CLOTH

But this can be abbreviated even more simply to...

GET ALL

Another useful feature is the ability to refer to the last used item/object as IT, for example...

EXAMINE THE BLUE SWITCH AND PRESS IT  
or...  
GET THE GREEN FLASK AND FILL IT WITH WATER.  

Exceptions are also understood by the parser, such as...

TAKE EVERYTHING BUT THE COMPASS  
or...  
EXAMINE ALL EXCEPT THE WATCH

Using all of these command structures allows you to type in near-English sentences of great complexity, such as...

EXAMINE ALL BUT THE WATCH, SPANNER AND TORCH AND GO EAST.  
DROP EVERYTHING BUT THE FLASK. OPEN IT AND GIVE IT TO THE ROBOT.  
GET THE KEYS. OPEN THE SLIDING DOOR AND RUN NORTHWEST THEN INVENTORY. WHAT IS MY SCORE?  

3. Special commands

There are a few commands that are neither movement or actions. Two of these affect the way the adventure is presented to you; they are...

WORDS Turn off the pictures.  
PICTURES Turn them back on again.  

A HELP feature has been included, to give you a clue at certain points within the games. The HELP command is generally useful in the locations around the start of each adventure, to get you on your way.

The other commands are concerned with saving and restoring your game position. Full instructions will be displayed on the screen where necessary.

SAVE Stores game position to your filing system.  
RESTORE Loads a saved game position.  
RAM SAVE Stores game position in the computer's memory.  
RAM RESTORE Loads a RAM SAVEd position from the computer's memory.  
OOPS Restore position as it was before you last moved. OOPS is a very useful command, and versions on larger machines let you use it several times in succession to go back a long way in time.  
Naturally, you can use OOPS, RESTORE or RAM RESTORE, even when you have just been killed, so that you can return to your position before your fatal accident!