package main

import (
	"fmt"
	"time"
)

func sum(a int, b int) int {
	return a + b
}

type Employee struct {
	FirstName  string
	SecondName string
	Age        int
}

func (e Employee) LeavesRemaining() {

	fmt.Printf("%s %s has %d ages\n", e.FirstName, e.SecondName, e.Age)
}

func New(firstName string, secondName string, age int) Employee {
	e := Employee{firstName, secondName, age}
	return e
}

func say(s string) {
	for i := 0; i < 2; i++ {
		fmt.Println(s)
		time.Sleep(3000 * time.Millisecond)
	}
}

func main() {

	var name = "Antonio"

	var name2 = "Antonio2"
	fmt.Println("Hello, world!!" + name + name2)

	result := sum(1, 5)

	if result > 5 {
		fmt.Printf("bigger than 5 %d\n", result)

	} else {
		fmt.Printf("Not bigger than 5 %d\n", result)
	}

	//arr
	var arr [5]int
	arr[0] = 1
	fmt.Println(arr)

	//slice
	var slice_ []int
	slice_ = append(slice_, 3)
	fmt.Println(slice_)

	//maps
	person := make(map[string]string)
	person["name"] = "antonio"
	fmt.Println(person)
	fmt.Println(person["name"])

	emp := Employee{
		FirstName:  "Antonio",
		SecondName: "Mendoza",
		Age:        3,
	}
	emp.LeavesRemaining()

	emp = New("1", "3", 3)
	emp.LeavesRemaining()

	//Goroutines

	//goroutine()

	// channels
	//chanel()

	//bufferedChannels()

	//concurrency()

	//syncGoroutine()

	selectStatement()
}

func sendMessage(timeToSleep time.Duration, message string, c chan int) {
	time.Sleep(timeToSleep * time.Second)
	c <- 1
}

func selectStatement() {

}

func syncGoroutine() {
	c := make(chan string, 1)

	go processSomething(c)

	fmt.Println(<-c)
}

func processSomething(c chan string) {

	time.Sleep(3 * time.Second)

	c <- "done"
}

func worker(id int, jobs <-chan int, results chan<- int) {
	for j := range jobs {
		fmt.Printf("Worker %d processing job %d\n", id, j)
		time.Sleep(time.Second) // Simulate work
		results <- j * 2        // Send result
	}
}
func concurrency() {
	jobs := make(chan int, 5)
	results := make(chan int, 5)

	// Start 3 workers
	for w := 1; w <= 3; w++ {
		go worker(w, jobs, results)
	}

	// Send 5 jobs and close the jobs channel
	go funcName(jobs)

	// Collect all results
	for a := 1; a <= 5; a++ {
		fmt.Println("Result:", <-results)
	}
}

func funcName(jobs chan int) {
	for j := 1; j <= 5; j++ {
		time.Sleep(time.Second) // Simulate work

		fmt.Println("Sending :", j)

		jobs <- j
	}
	close(jobs)
}

func goroutine() {
	go say("Hello")
	say("World")

	fmt.Println("end...")
}

func bufferedChannels() {

	// Create a buffered channel with capacity 2
	c := make(chan int, 2)

	// Send two values to the channel
	c <- 1
	c <- 2

	go fmt.Println(<-c)

	// Now the channel is full; sending another value would block
	c <- 3 // This would block

	// Receive the values
	fmt.Println(<-c)
	fmt.Println(<-c)

	time.Sleep(1000 * time.Millisecond)
	// Now the channel is empty; receiving another value would block
	// fmt.Println(<-c) // This would block

}

func sumChannel(a []int, c chan int) {
	total := 0
	for _, v := range a {
		total += v
	}

	time.Sleep(1000 * time.Millisecond)
	fmt.Println("Before ")

	c <- total // Send total to c

	time.Sleep(1000 * time.Millisecond)
	fmt.Println("After ")

}

func chanel() {
	a := []int{1, 2, 3, 4, 5}
	c := make(chan int)

	go sumChannel(a, c) // Start sum goroutine
	result2 := <-c      // Receive result from c

	fmt.Println("sumResult " + fmt.Sprintf("%d", result2))

}
