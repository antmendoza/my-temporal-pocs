# Makefile for building and running a Go CLI application

APP_NAME = mycli
SRC_DIR = client
OUTPUT_DIR = bin
BIN_PATH = $(OUTPUT_DIR)/$(APP_NAME)

.PHONY: all build run clean

all: build run

# Build the Go CLI application
build:
	mkdir -p $(OUTPUT_DIR)
	go build -o $(BIN_PATH) $(SRC_DIR)/main.go

# Run the compiled binary
run:
	$(BIN_PATH)

# Remove compiled binaries
clean:
	rm -rf $(OUTPUT_DIR)