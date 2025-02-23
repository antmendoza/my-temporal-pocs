package customer

import (
	"fmt"
	"os"
)

func main() {
	if len(os.Args) < 2 {
		fmt.Println("Usage: <command> [arguments]")
		os.Exit(1)
	}

	command := os.Args[1]

	switch command {
	case "notify-customer":
		startWorker()
	case "notify-cell":
		startExample()
	default:
		fmt.Printf("Unknown command: %s\n", command)
		os.Exit(1)
	}
}

func startWorker() {
	fmt.Println("Starting worker...")
	// Add your worker start logic here

}

func startExample() {
	fmt.Println("Starting example...")
	// Add your example start logic here
}
