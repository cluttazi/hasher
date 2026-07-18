# hasher

A minimal HTTP service built on [Apache Pekko](https://pekko.apache.org/)
(Pekko HTTP + a classic actor). `POST /hash` accepts a JSON body and
answers with the original value and its "hash" (currently an echo —
see `HashActor`).

## Requirements

- JDK 21
- sbt 1.11+ (`./sbt` delegates to the sbt on your PATH)

## Build & test

```bash
sbt test            # compile + run the route spec
sbt scalafmtCheckAll  # formatting gate (scalafmt)
sbt run             # start the server on http://localhost:8080
```

## Try it

```bash
curl -H "Content-type: application/json" -X POST \
  -d '{"body":"cool"}' http://localhost:8080/hash
# -> 201 Created  {"original":"cool","hashed":"cool"}
```
