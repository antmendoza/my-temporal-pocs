package main

import "fmt"

func sum(a int, b int) int {
	return a + b
}

func main() {

	var name string = "Antonio"

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

}
