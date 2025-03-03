# Makefile for cljdice
# 
# Common targets:
#   make clean      - Clean build artifacts
#   make test       - Run tests
#   make jar        - Build standalone jar
#   make native     - Build native executable
#   make install    - Install native executable to /usr/local/bin
#   make all        - Build both jar and native executable

# Configuration
CLOJURE = clojure
JAR_NAME = target/cljdice-standalone.jar
NATIVE_NAME = target/cljdice
PREFIX = /usr/local

.PHONY: all clean test jar native install dev-repl

# Default target
all: jar native

# Clean build artifacts
clean:
	@echo "Cleaning build artifacts..."
	@rm -rf target
	@echo "Clean complete."

# Run tests
test:
	@echo "Running tests..."
	@$(CLOJURE) -M:test
	@echo "Tests complete."

# Run tests with development utilities
test-dev:
	@echo "Running tests with development utilities..."
	@$(CLOJURE) -M:dev:test
	@echo "Tests complete."

# Build standalone jar
jar:
	@echo "Building standalone jar..."
	@$(CLOJURE) -T:build uber
	@echo "Jar build complete: $(JAR_NAME)"

# Build native executable
native:
	@echo "Building native executable..."
	@$(CLOJURE) -T:build native-image
	@echo "Native build complete: $(NATIVE_NAME)"

# Install native executable
install: native
	@echo "Installing to $(PREFIX)/bin..."
	@install -d $(PREFIX)/bin
	@install -m 755 $(NATIVE_NAME) $(PREFIX)/bin
	@echo "Installation complete."

# Start a REPL with development utilities
dev-repl:
	@echo "Starting development REPL..."
	@$(CLOJURE) -M:dev:repl

# Run profiling
profile:
	@echo "Running profiler..."
	@$(CLOJURE) -X:profile-run
	@echo "Profiling complete."

# Show version
version:
	@$(CLOJURE) -M:run --version

# Help target
help:
	@echo "cljdice Makefile"
	@echo ""
	@echo "Available targets:"
	@echo "  all        - Build both jar and native executable (default)"
	@echo "  clean      - Clean build artifacts"
	@echo "  test       - Run tests"
	@echo "  test-dev   - Run tests with development utilities"
	@echo "  jar        - Build standalone jar"
	@echo "  native     - Build native executable"
	@echo "  install    - Install native executable to $(PREFIX)/bin"
	@echo "  dev-repl   - Start a REPL with development utilities"
	@echo "  profile    - Run profiling"
	@echo "  version    - Show version"
	@echo "  help       - Show this help message"
