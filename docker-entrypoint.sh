#!/bin/sh

java \
-Dspring.profiles.active=default \
-Dhttps.protocol=TLSv1.1,TLSv1.2 \
-DsessionsDir=/l9bot/sessions/ \
-DgamesDir=/l9bot/tmp/games/ \
-DdownloadsDir=/l9bot/tmp/downloads/ \
-DpicturesDir=/l9bot/tmp/cache/ \
-jar l9bot.jar