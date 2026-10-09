# Project Plan

## Title & Tagline

Chessoid
 - No Account, No internet, Just Chess

## 1. Problem - What problem are we solving? 

Current options to play and learn chess are entirely online and require an internet 
conneciton to play. Chess is an offline game and would be great to enjoy on mobile 
devices while disconnected and unable to reach internet services. These chess 
application options also do not provide the best teaching experience. Finding and 
leanring on these applications are difficult and a chore, and reading chess books 
are complicated and hard to learn. A localized chess coach that goes through openings, 
mid-game, and logic would excel chess beginners. 

## 2. Users - Who is this for? 

People who engage in chess as a hobby, and know chess but cannot grow in experience. 
This is also for people who would like an offline option to practice chess without a 
reliable internet connection. This is NOT for chess beginners who are freshly learning 
chess, only those that know the game but have early ELO. 

## 3. Team members - Who will work on this program? 

So far only me, I will doing the programming, UI/UX, devlopment, and implementation,
all until I find someone else if anyone would like to assist on this project as well.

## 4. Features

 - An interactable chess baord through touch screen and mouse controls, with ability to 
   play through an entire game of chess
 - Bots at different elos to play games of chess against. 
 - Different modes of chess including 5 minute, 10 minute, and bullet games. 
 - A coach bot that will go through openings with the player step by step and 
   create games and scenarios where these openings and moves can be used vs when
   they are not to be used. 
 - Entirely localized so it can be downloaded once and played offline forever

Possible Future features if time and scope allows: 
 - a localized LLM at low B that runs entirely on unified memory, this LLM 
   will allow live questions and possible move suggestions and why those moves
   are important
 - An early chess coach, to teach people who are brand new to chess and would 
   like to start learning 

## 5. Data - What are we storing? 

Since the application is localized, all data will be stored and cached locally. 
Data such as what has been learned/compeleted, how the player plays, and what bots 
the player has been playing against. 

## 6. Tech - What stack are we using? 

 - Python 3.14
 - `python-chess` is a chess library that can handle all the basic move generation 
   and validation in python. 
 - `StockFish` is a free to use open source chess engine that can be download and used
   locally, it is used for both Lichess and Chess.com
 - SQLite will be for offline data storing, such as historical games and stats

## 7. Related Work - Similar projects that already exist

 - Chess.com is a popular online site to play agaisnt players and bots online while 
   practicing puzzles and learning lessons in a DuoLingo esque way. 
 - Lichess is a free online open source that is driven by the chess community and 
   has many and more features like Chess.com 
 - Offline Chess Coach & Trainer is the closests thing I can find to similarity to 
   what this application is for, which is hosted on the GooglePlay Store with 100+ 
   downloads and seems like a rather sketchy download. 

## 8. Monetize - How will this make money? 

Not applicable. This is local and entirely free. Anything with advertising or locked out 
premium features would push away possible users. Chess is for all. If this application 
WERE to make money in the future, while keeping it free, either non-disruptive banner ads 
or added premium features such as cosmetic themes or deeper LLM access would be the most without 
pushing the larger demographic. 

## 9. UI/UX - How should this look and feel? 

Simplistic minimal chess board with easy to view colors and text on top. 
- Easy to view and understand chess board to accurately identify all pieces without strain. 
- Easy to access coaching elements in UI, choosing from a variety to then learn and engage. 
- Text in bubbles over chess board that accurately identify moves and teach, while also being 
  easy to read and non-disruptive. 
- Statistical view, showing all history of games and moves made that is easy to identify and read,
  having stats and chess notation being color coded (green for user move, red for bot move, blue number for space moved)
  will help with this since chess notation is notoriously hard to read when first introduced. 

## 10. Deployment - Where and how will this ship? 

Deployment will be done through Github and possible minimal self hosted website for 
broader end user access if they are daunted by github. This can also be deployed for 
free through Google Play Store and Apple App Store (Google Play most likely since it is
a onetime 25$ payment to deploy, as opposed to Apple App store which requires 100$ annually
and requires to be deployed on proprietary devices that I do not own.) Once download and 
installed on device, the application runs entirely local.   


