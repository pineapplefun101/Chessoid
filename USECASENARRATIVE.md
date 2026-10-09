# Use Case Narratives - CPSC 49200

##Team and Project

 - **Chessoid**
 - **Team Members**
   - Jeffrey Elsenbach, jeffreyelsenbach@lewisu.edu, skilled in programming, low-level, networking, and optimization

## Use Case Narratives

We now present the features of the minimum viable product (MVP) version
of this project as a set of use case narratives. Each use case narrative 
starts with a title that idenntifies the user and how they wil use the 
system. Then the narrative will describe the details of that interation, 
including exceptional circumstances that may arise. If two different kinds 
of roles exercise a particular feature differently, those are presented 
here as two use case narratives.

We will list these use case narratives as a set of checkboxes, since 
they are features we will check off as we complete their implementation.

 - [ ] 1. **The user is on vacation and wants to play a chess match**
The user is entirely offline and away from any network as they are travelling, 
they launch the application on their android phone and is greeted with a minimal,
easy to read UI with an option for playing a bot match. The user can choose the 
ELO of this bot and start a game on either side, during the match they will see 
a live history of moves in chess notation, an undo button to move backwards through
the match to undo mistakes, and a resign button to end the match and start a new one. 
The match will be cached and will be able to be resumed at a later time if the user 
decides to put down their phone or do another task in the middle of a match. 

 - [ ] 2. **The user understands the basics, but wants to learn the essentials, and engages with a lesson**
This user already has an understanding of chess, but does not understand opennings, mid-game, or 
late game moves, or how to start thinking about the best possible moves and things to look 
out for. They don't want to take the time to grab a chess book and read move by move and then 
try to replicate those moves on a chess board. They will open the app and see lessons 
on possible opennings and mid game problems. The app will teach step by step by having the 
user interact with the board while going through what would be taught in a chess book.
The user will go step by step until the lesson is done and the user understands the stregnths 
and weaknesses of these moves. The lessons will be caches and will be able to be resumed at a later 
time if the user decides to stop in the middle of a lesson. 

 - [ ] 3. **The user wants to go back and see all historical lessons and games**
The user has played a good ammount of bot matches, and has completed some lessons, but
would like to go back and look at previous matches and lessons to see what moves were 
made, and study to see if they would do something different now. All matches are listed 
historically by date and time, and saved in an easy readable chess notation, which also 
translates to the board in game, so the user can choose a move on chess notation, and 
the board will visibly reflect the notation. 

 - [ ] 4. **The system caches paused games, lessons, and historical data**
The system recognizes when the user is no longer engaging with the application and the 
application was closed/put into cached memory on the device. The system will then enter a 
low power mode that quickly takes notation of all the activity the user engaged in and 
the most recent activity and store it into a SQL database/cached storage locally. When the 
returns the system digs up all this information so the user does not notice that this 
was cached to begin with and the user engages with the system seemlessly. 

 - [ ] 5. **The user wants to practice what was learned from the lessons**
The user completes a lesson, but does that really mean that the user has learned the 
material? The user can then engage in bot matches inside of the lesson practicing the 
opennings or the moves along with a bot guide that shows when the moves SHOULD be made, 
and when those moves are not ideal. Through these rounds of practice, the user will have
understood the situations on opennings, and how to use and play against them. 

 - [ ] 6. **The user wants to engage in some quick puzzles**
Chess puzzles are a fun little way to learn chess and get better without having to play
a full match. Whenever the user feels like engaging on a puzzle, the user will open the application 
and see an option on the UI for puzzles. From there the user is dropped right into a random 
puzzle that when the user completes it goes to the next, and if the user makes the wrong move 
the puzzle resets. 

 - [ ] 7. **The user queries a local LLM for a live breakdown of the chess game being played**
(This feature may be out of scope for the project and may or may not be implemented due to time, 
but I will see if I can get this feature implemented before the project due date). 
This user has a few ideas as to the next move they can make in a chess match, and decides to query a
locallized model hosted entirely on their phone. Most Android phones have about 16GB of unified memory now, 
so a local 1B model would not only work on a phone locally and offline, but will be perfectly optimized 
as it won't take up much memory and cause phone performance issues. Once queried the model will go through 
the chess notation, and then suggest possible moves. The user cannot type directly to the model, as the model 
can only read chess notation and then respond breifly with possible moves and reasons why they are good moves.  
