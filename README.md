# LeagueApp
Simple League of Legends champ data viewer.

## Overview
This was designed to quickly bring up combat-related champion details for the MOBA
videogame League of Legends.
It was also designed to keep up to date by updating the local data whenever there was
a patch update and available internet.
Because data is stored locally, it was usable without an internet connection after
the initual champ data download.

### Champ filters and comparison tools
I wanted an app that was fast and could be used offline, but more importantly i wanted
an app that could compare champs by stats.
For example:
Say you wanted to try Braum top, but wanted to see how he compared against other top laners.
Or say, you wanted to see which tank benefits the most from Sunfire Cape.

## Features
Features successfully implemented are marked with an o. <br/>
Partial implements are marked with a -. <br/>
Unimplemented features are marked with an x. <br/>

o Local storage <br/>
o Full champion roster with icons <br/>
o All basic stats <br/>
o Stat view by individual level as a range <br/>
\- Corrected data (out of date by now) <br/>
\- Skill descriptions <br/>
x Auto updating <br/>
x Skill icons <br/>
x Champ filters

## Discontinuation
This was discontinued sometime in 2016.  This was mostly due to my increasing struggles with Data Dragon,
the json League data delivery service.

Data Dragon was a way for people to examine and copy the jsons used in the
game that provided game data.  I wont go into much detail, but these jsons were not accurate; they were used
for tooltips in the game and were often overridden by other components inaccessible to the public.
This is even reflected in Riot's own champion and item data public websites, which often show values of 0
for damage or weird hardcoded strings that had no business being hardcoded.

To compensate for the data inaccuracies i had to
correct this myself with massive blocks of code for exceptions.  I successfully did this for the most recent data
for the time by using LeagueWikia, and then gave up when i realized i'd have to infinitely maintain such exception code
unless i found a way to scrape the data off of LeagueWikia, or, if Riot would finally release their long awaited redo of
Data Dragon.

If you want to install this despite these flaws, go ahead.  It may still work.  But even with the most up-to-date data,
there will be inaccuracies.  If you have a modern phone, LeagueWikia has their own app from what i've heard; i'd recommend that.
