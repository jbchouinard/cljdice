# cljdice

A command-line dice roller written in Clojure.

## Features

- Roll dice with any number of sides
- Parse and evaluate complex dice expressions like `2d6+1d4+3`
- Accurate probability modeling using dice algebra
- Normal approximation for efficient large dice rolls
- Simple command-line interface

## Prerequisites

- [Clojure CLI tools](https://clojure.org/guides/install_clojure)
- Java JDK 11 or later

## Usage

Run directly with Clojure CLI:

```bash
# Roll dice using a dice expression
clojure -M:run "3d6"
clojure -M:run "d20+5"
clojure -M:run "2d4+3d6-2"

# Show help
clojure -M:run --help
```

### Dice Expression Syntax

The dice expression syntax follows standard dice notation:

- `NdS`: Roll N dice with S sides each (e.g., `3d6` for three six-sided dice)
- `dS`: Roll one die with S sides (e.g., `d20` for one twenty-sided die)
- `+`: Add dice or constants (e.g., `d20+5`)
- `-`: Subtract dice or constants (e.g., `d6-1`)
- `d[s1,s2,...]`: Custom die with specific sides (e.g., `d[1,3,5]` for a three-sided die with faces 1, 3, and 5)
- `Nd[s1,s2,...]`: Roll N custom dice with the specified sides (e.g., `2d[1,3,5]`)

Examples:
- `3d6`: Roll three six-sided dice
- `d20+5`: Roll a twenty-sided die and add 5
- `2d4+3d6-2`: Roll two four-sided dice, add three six-sided dice, and subtract 2
- `d[1,1,2,3]`: Roll a custom die with faces 1, 1, 2, and 3 (50% chance of rolling 1)
- `2d[1,3,5]`: Roll two custom dice, each with faces 1, 3, and 5
- `d6+d[1,3,5]`: Roll a six-sided die and add a custom die

## Development

### Running Tests

```bash
clojure -X:test
```

### Using the Makefile

The project includes a Makefile with common development tasks:

```bash
# Show available targets
make help

# Run tests
make test

# Build standalone JAR
make jar

# Build native executable
make native

# Build both JAR and native executable
make all

# Install native executable to /usr/local/bin
make install

# Start a development REPL
make dev-repl

# Show version
make version
```

### Building a JAR

```bash
clojure -T:build uber
```

This will create a standalone JAR file in the `target` directory.

### Building a Native Image

To build a native executable with GraalVM:

```bash
# Make sure GraalVM and native-image are installed
# Then build the native image
clojure -T:build native-image
```

This will create a native executable called `cljdice` in the `target` directory.
The native image offers several advantages over the JAR:
- Faster startup time
- Lower memory usage
- No JVM dependency

You can run the native executable directly:

```bash
./target/cljdice "3d6+4"
```

## Normal Approximation for Dice Rolls

For efficiency, cljdice uses normal approximation for large numbers of dice:

- **Standard Dice**: Normal approximation is used for 10 or more dice of the same type (e.g., 10d6, 20d8)
- **Custom Dice**: Normal approximation is never used for custom dice with non-uniform probabilities

The normal approximation provides excellent accuracy for 10+ dice while being significantly faster for large numbers of dice. If exact calculation is required for all dice combinations, it can be forced through configuration.

### Normal Approximation Accuracy

For large numbers of dice, cljdice can use a normal approximation for better performance. The table below shows the Kolmogorov-Smirnov (KS) statistic for different dice combinations, which measures the maximum difference between the exact probability distribution and the normal approximation.

A smaller KS statistic indicates a better approximation:
- KS < 1%: Excellent approximation
- KS < 5%: Good approximation
- KS > 5%: Poor approximation

#### KS Statistics by Dice Combination

| Dice | d4    | d6    | d8    | d10   | d12   | d20   | d100  |
|------|-------|-------|-------|-------|-------|-------|-------|
| 1d   | 8.96% | 7.84% | 8.58% | 8.32% | 8.51% | 8.48% | 8.48% |
| 2d   | 2.20% | 2.31% | 2.30% | 2.28% | 2.25% | 2.22% | 2.16% |
| 3d   | 0.809%| 0.961%| 0.995%| 1.02% | 1.05% | 1.06% | 1.06% |
| 4d   | 0.306%| 0.536%| 0.635%| 0.680%| 0.704%| 0.737%| 0.754%|
| 5d   | 0.429%| 0.304%| 0.423%| 0.478%| 0.508%| 0.551%| 0.574%|
| 10d  | 0.552%| 0.221%| 0.146%| 0.176%| 0.208%| 0.255%| 0.280%|
| 20d  | 0.653%| 0.238%| 0.122%| 0.083%| 0.067%| 0.112%| 0.138%|
| 50d  | 0.728%| 0.293%| 0.146%| 0.083%| 0.054%| 0.028%| 0.054%|
| 100d | 0.754%| 0.316%| 0.167%| 0.099%| 0.063%| 0.020%| 0.027%|

## Project Structure

- `src/cljdice/core.clj`: Main entry point and CLI handling
- `src/cljdice/dice.clj`: Core dice functionality and probability calculations
- `src/cljdice/parser.clj`: Dice expression parser
- `src/cljdice/stats.clj`: Statistical utilities for dice calculations
- `dev-src/cljdice_dev/stats.clj`: Development utilities for benchmarking and comparing dice distributions
- `dev-src/cljdice_dev/profile.clj`: Performance profiling tools for development
- `test/`: Test files

### Development Code

The project separates production code from development code:

- Production code is in the `src/` directory and is included in all builds
- Development code is in the `dev-src/` directory and is only included when using the `:dev` alias

To use the development utilities:

```bash
# Run with development utilities
clojure -M:dev:run "3d6"

# Run tests with development utilities
clojure -M:dev:test

# Run profiling with development utilities
clojure -X:profile-run
```
