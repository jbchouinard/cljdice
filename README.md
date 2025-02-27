# CLJDice

A simple command-line dice roller written in Clojure.

## Features

- Roll any number of dice with any number of sides
- Simple, intuitive command-line interface
- Native executable support via GraalVM

## Prerequisites

- [Clojure CLI tools](https://clojure.org/guides/install_clojure)
- Java JDK 11 or later
- [GraalVM](https://www.graalvm.org/downloads/) (optional, for native image compilation)

## Usage

Run directly with Clojure CLI:

```bash
# Roll a single 6-sided die (default)
clojure -M:run

# Roll 3 20-sided dice
clojure -M:run --dice 3 --sides 20

# Show help
clojure -M:run --help
```

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

### Building a Native Executable

Requires GraalVM with `native-image` installed.

```bash
clojure -T:build native-image
```

This will create a native executable in the `target` directory.

## Project Structure

- `src/cljdice/core.clj` - Main application code
- `test/cljdice/core_test.clj` - Tests
- `deps.edn` - Project dependencies and configuration
- `build.clj` - Build tasks

## License

Copyright © 2025

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
