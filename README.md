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

Examples:
- `3d6`: Roll three six-sided dice
- `d20+5`: Roll a twenty-sided die and add 5
- `2d4+3d6-2`: Roll two four-sided dice, add three six-sided dice, and subtract 2

## Development

### Running Tests

```bash
clojure -X:test
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

## Project Structure

- `src/cljdice/core.clj`: Main entry point and CLI handling
- `src/cljdice/dice.clj`: Core dice functionality and probability calculations
- `src/cljdice/parser.clj`: Dice expression parser
- `src/cljdice/random.clj`: Random number generation utilities including uniform and normal distributions
- `src/cljdice/profile.clj`: Performance profiling tools for development
- `test/`: Test files
