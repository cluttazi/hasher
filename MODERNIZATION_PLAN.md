# Modernization plan — hasher

Audit date: 2026-07-18 · Branch: `claude/repo-modernization-audit-6se2sn`

## What this repo is

A tiny Akka HTTP quickstart-style service (~130 LOC Scala): `POST /hash`
with a JSON body `{"body": "..."}` is answered by an actor with
`{"original": ..., "hashed": ...}` (201 Created). One route trait, one
classic actor, one spray-json trait, one server bootstrap, one route spec.

## Audit findings

### Toolchain (all end-of-life)

| Component | Current | Target | Notes |
|---|---|---|---|
| sbt | 0.13.16 (2017) + a **bundled launcher committed in `sbt-dist/`** | 1.11.7 | 0.13 artifacts no longer resolvable (typesafe ivy repo 403s); 0.13 cannot run on Java 21 |
| Scala | 2.12.4 (2017) | 2.13.18 | 2.12.4 cannot compile on JDK 21 |
| JDK (CI) | 8 | 21 (Temurin LTS) | local environment only has Java 21 |
| Launcher script `./sbt` | delegates to `sbt-dist/bin/sbt` (0.13 jar) | delegate to system sbt; delete `sbt-dist/` | binary jar checked into git |

### Dependencies

| Library | Current | Target | Notes |
|---|---|---|---|
| akka-actor/stream | 2.5.11 (2018, EOL) | **Apache Pekko 1.6.0** | Akka ≥2.7 is BSL-licensed; Pekko is the Apache-2.0 continuation of Akka 2.6, supports JDK 21 |
| akka-http (+ spray-json) | 10.0.11 (2017, EOL) | pekko-http 1.3.0 | `bindAndHandle`/`ActorMaterializer` are gone/deprecated → `Http().newServerAt(...).bind` |
| akka-http-xml | 10.0.11 | **remove** | completely unused |
| scalatest | 3.0.1 | 3.2.20 | `WordSpec`/`Matchers` moved (`AnyWordSpec`, `matchers.should.Matchers`) |
| sbt-scalariform | 1.6.0 (dead project, sbt 0.13-only) | replace with sbt-scalafmt 2.6.2 + scalafmt 3.11.4 | scalariform is unmaintained |
| sbt-revolver | 0.9.1 | 0.10.0 | sbt 1.x compatible |

### Build/test/lint status (before)

- `./sbt about` — **FAILS**: bundled 0.13 launcher cannot resolve
  `org.scala-sbt#sbt;0.13.16` (typesafe repo returns 403 / repo retired).
- `sbt test` — **won't run** for the same reason; even if the launcher
  resolved, sbt 0.13 and Scala 2.12.4 do not support Java 21.
- No formatter/linter runnable (scalariform plugin is sbt 0.13-only).
- CI (`.github/workflows/ci.yml`): action majors are current
  (checkout@v4, setup-java@v4, sbt/setup-sbt@v1) but pins **JDK 8**.

### Code issues found by reading

1. `HashActor` never hashes — it echoes the input
   (`ActionPerformed(s"$string", s"$string")`). The test asserts this
   echo behavior, so it is the *documented* behavior of the service.
   Treated as a product decision, **deferred** (see below), not silently
   changed.
2. `Props[HashActor]` auto-application, un-annotated implicits
   (`timeout`, json formats) — deprecated patterns in 2.13; fix.
3. `ActorMaterializer` + `Http().bindAndHandle` — removed APIs; migrate
   to the 1.x server bootstrap.
4. `.gitignore` missing `.bsp/`, `.metals/`, `.bloop/`; README does not
   explain how to build/run/test.

## Prioritized checklist

1. [x] Write this audit (this file).
2. [x] Toolchain: sbt 1.11.7 in `project/build.properties`; delete
       `sbt-dist/`; make `./sbt` delegate to system sbt; replace
       scalariform plugin with scalafmt, bump sbt-revolver.
3. [x] Migrate Scala 2.12.4 → 2.13.18 and Akka 2.5/10.0 →
       Pekko 1.6.0 / pekko-http 1.3.0; drop unused akka-http-xml;
       replace removed APIs; port test to ScalaTest 3.2 style;
       `sbt test` must pass on Java 21.
4. [x] Formatting: `.scalafmt.conf`, run `scalafmtAll`, gate with
       `scalafmtCheckAll` in CI. (sbt-scalafmt 2.5.5, not 2.6.x —
       2.6.x requires sbt ≥ 1.12.9.)
5. [x] Housekeeping: `.gitignore`, README (build/run/test instructions).
6. [x] CI: JDK 8 → 21; add a format check; keep sbt caching.
7. [x] Re-run full build + tests; update this file with Done vs Deferred.

## Result — Done vs Deferred

### Done

- **Toolchain**: sbt 0.13.16 → 1.11.7; deleted the committed `sbt-dist/`
  0.13 launcher (432 lines / one binary jar removed); `./sbt` now
  delegates to the sbt on PATH; JDK target 8 → 21 (Temurin LTS).
- **Runtime stack**: Akka 2.5.11 / akka-http 10.0.11 (EOL, pre-BSL) →
  Apache Pekko 1.6.0 / pekko-http 1.3.0 (Apache-2.0, JDK 21 supported);
  removed the unused akka-http-xml dependency.
- **Language**: Scala 2.12.4 → 2.13.18; compile is warning-free under
  `-deprecation -feature -unchecked -Xlint`.
- **API replacements**: `ActorMaterializer` + `Http().bindAndHandle` →
  `Http().newServerAt(...).bind`; `Props[T]` auto-application →
  `Props[T]()`; explicit types on all implicits; spray-json formats pin
  their field names.
- **Tests**: ScalaTest 3.0.1 → 3.2.20 (`AnyWordSpec`,
  `matchers.should.Matchers`). The response-body assertion now compares
  parsed JSON instead of a raw string: spray-json's `JsObject` member
  order flipped with Scala 2.13's `ListMap` semantics, and member order
  is not part of the JSON contract (RFC 8259).
- **Formatting**: scalariform (dead) → scalafmt 3.11.4 via
  sbt-scalafmt 2.5.5, all sources formatted, CI-gated.
- **CI**: JDK 21, `sbt scalafmtCheckAll scalafmtSbtCheck` then
  `sbt test`; action majors already current (checkout@v4, setup-java@v4
  with sbt cache, sbt/setup-sbt@v1) and kept.
- **Docs**: README rewritten with real build/run/test instructions;
  `.gitignore` covers `.bsp/`, `.metals/`, `.bloop/`, editor dirs.
- **Verification**: `sbt test` green on Java 21.0.10; live smoke test —
  `POST /hash` with `{"body":"cool"}` returns `201 Created` and
  `{"original":"cool","hashed":"cool"}` on pekko-http 1.3.0.

### Deferred (unchanged from audit)

- **Real hashing in `HashActor`** — behavioral/API change; needs owner
  decision on algorithm and payload shape.
- **Scala 3 / Pekko 2.0.0-Mx / ScalaTest 3.3 pre-releases** — not
  stable yet; revisit when GA.
- **Typed actors** — style migration, no modernization payoff here.

## PR-style summary

**Modernize hasher: sbt 1.11 / Scala 2.13 / Apache Pekko / JDK 21**

Every part of the 2017-era toolchain was end-of-life and the build could
not run at all (sbt 0.13 unresolvable, incompatible with modern JDKs).
This branch makes the project build, test, format-check, and run on
Java 21 with maintained, Apache-2.0-licensed dependencies, while
preserving observable behavior (verified by the route spec and a live
smoke test).

Changes: sbt 1.11.7 (bundled 0.13 launcher deleted) · Scala 2.13.18 ·
Akka 2.5/10.0 → Pekko 1.6.0/pekko-http 1.3.0 · ScalaTest 3.2.20 ·
scalariform → scalafmt (CI-gated) · sbt-revolver 0.10.0 · unused
akka-http-xml removed · removed/deprecated APIs replaced · README and
.gitignore updated · CI on JDK 21.

Risk areas: JSON object member order in responses changed
(`{"hashed":...,"original":...}`) — semantically identical, but a
client doing exact string comparison would notice; server bootstrap now
uses the Pekko HTTP 1.x API (same host/port/behavior).

Deliberately untouched: the echo "hash" placeholder behavior, actor
model style (classic), route structure, endpoint contract, license.


## Deferred (explicitly not done, with rationale)

- **Actually hashing in `HashActor`**: changing the response payload is a
  behavioral/API change the current test explicitly pins; needs an owner
  decision on the algorithm (and whether `original` should still be
  echoed). Trivial to implement once decided.
- **Scala 3 / Pekko 2.0.0-M / scalatest 3.3 pre-releases**: not stable;
  conservative mandate says stay on latest *stable* (2.13.18 / 1.6.0 /
  3.2.20). Scala 3 migration is mechanical for this codebase but brings
  no benefit until dependencies settle.
- **Typed actors (`pekko.actor.typed`)**: classic actors remain fully
  supported; rewriting to typed is a style migration, not a modernization
  requirement, and would churn the whole codebase.
