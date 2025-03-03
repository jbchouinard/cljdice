# Development Source Code

This directory contains code that is only used for development, benchmarking, and documentation purposes. It is not included in the production builds (uberjar or native image).

## Namespaces

- `cljdice-dev.stats` - Contains statistical utilities for benchmarking and comparing dice distributions.
- `cljdice-dev.profile` - Contains profiling utilities for performance analysis.

## Usage

To use these development utilities, include the `:dev` alias when running Clojure:

```bash
clojure -M:dev
```

For example, to run the REPL with development utilities:

```bash
clojure -M:dev:repl
```

Or to run tests with development utilities:

```bash
clojure -M:dev:test
```
