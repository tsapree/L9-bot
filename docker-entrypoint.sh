#!/bin/sh

java \
-Dspring.profiles.active=default \
-Dhttps.protocol=TLSv1.1,TLSv1.2 \
-DgamesDir=/tmp/games/ \
-DsessionsDir=/l9bot/sessions/ \
-DdownloadsDir=/tmp/downloads/ \
-DpicturesDir=/tmp/cache/ \
-jar l9bot.jar