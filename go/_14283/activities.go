package _14283

import (
	"log"
	"time"
)

type Activities struct {
	Name     string
	Greeting string
}

// GetName Activity.
func (a *Activities) GetName() (string, error) {
	return a.Name, nil
}

// GetGreeting Activity.
func (a *Activities) GetGreeting(i int) (string, error) {

	log.Println("GetGreeting called with:", i)

	time.Sleep(time.Duration(i) * time.Second)
	return "done", nil
	//	return "", temporal.NewCanceledError("")
}

// SayGreeting Activity.
func (a *Activities) SayGreeting() (string, error) {

	time.Sleep(3 * time.Second)
	return "result", nil
}
