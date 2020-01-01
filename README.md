# Play with Slinky

This is a prototype of a project using [play framework](https://www.playframework.com/)
as backend and [Slinky](https://slinky.dev/) (react) as frontend.

Both projects share code in common in the `shared` project.

## How to use

The `run` sbt tasks launches the backend, and `dev` launches the frontend.

The backend runs on `localhost:9000` and the frontend runs on `localhost:8080`.

### Backend proxy

There's a proxy for redirecting `localhost:8080/play` to `localhost:9000` 
(see `frontend/webpack/webpack-fastopt.config.js`). You can see
an example usage in the `MakeCallButton` component (we use roshttp to make
the call but obviously you can use any library that you like).

### Projects settings

We put the various settings into dedicated object within the `project` folder.
For example, frontend settings are in `project/FontendSettings.scala`.


*This project is (roughly) the result of creating the seed for play in backend,
the seed for Slinky in frontend and creating the proxy.*

