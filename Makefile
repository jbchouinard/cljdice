# Makefile for cljdice
# 
# Common targets:
#   make clean      - Clean build artifacts
#   make test       - Run tests
#   make jar        - Build standalone jar
#   make target/cljdice - Build native executable
#   make install    - Install native executable to /usr/local/bin
#   make all        - Build both jar and native executable

# Configuration
CLOJURE = clj
NATIVE_NAME = target/cljdice
PREFIX = /usr/local

.PHONY: all clean test test-dev jar install dev-repl profile version help

# Default target
all: jar $(NATIVE_NAME)

# Clean build artifacts
clean:
	@rm -rf target

# Run tests
test:
	@$(CLOJURE) -M:test

# Run tests with development utilities
test-dev:
	@$(CLOJURE) -M:dev:test

# Build standalone jar - this is a phony target since the actual file name includes the version
jar:
	@$(CLOJURE) -T:build uber

# Build native executable
$(NATIVE_NAME):
	@$(CLOJURE) -T:build native-image

# Install native executable
install: $(NATIVE_NAME)
	@echo "Installing to $(PREFIX)/bin..."
	@install -d $(PREFIX)/bin
	@install -m 755 $(NATIVE_NAME) $(PREFIX)/bin
	@echo "Installation complete."

# Start a REPL with development utilities
dev-repl:
	@echo "Starting development REPL..."
	@$(CLOJURE) -M:dev

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
	@echo "  all           - Build both jar and native executable (default)"
	@echo "  clean         - Clean build artifacts"
	@echo "  test          - Run tests"
	@echo "  test-dev      - Run tests with development utilities"
	@echo "  jar           - Build standalone jar"
	@echo "  $(NATIVE_NAME)- Build native executable"
	@echo "  install       - Install native executable to $(PREFIX)/bin"
	@echo "  dev-repl      - Start a REPL with development utilities"
	@echo "  profile       - Run profiling"
	@echo "  version       - Show version"
	@echo "  help          - Show this help message"
