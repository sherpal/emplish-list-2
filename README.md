# Emplish List

This is a web application to register all your recipes and possible
ingredients that you might buy.

The application then allows to generate the list of things to buy, given
your choice of recipes, for example to eat the coming week.

## Stack

The backend uses the Play framework, together with Slick to access a
postgres database.

The frontend uses the Laminar framework for UI rendering.

## Deploy

The app is hosted on Heroku. In order to deploy, after logged into
Heroku, simply type in:
```
export SBT_OPTS="-Xmx6G -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=6G -Xss2M  -Duser.timezone=GMT"
sbt clean stage backend/deployHeroku
```
