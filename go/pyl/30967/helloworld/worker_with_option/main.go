package main

import (
	"context"
	"fmt"
	"log"
	"reflect"
	"runtime"
	"sort"
	"unsafe"

	"github.com/temporalio/samples-go/helloworld/activities_2"
	"go.temporal.io/sdk/activity"
	"go.temporal.io/sdk/client"
	"go.temporal.io/sdk/contrib/envconfig"
	"go.temporal.io/sdk/worker"

	"github.com/temporalio/samples-go/helloworld"
)

// GreetingActivities groups related activities behind a single struct. Each
// exported method becomes its own activity. Registering the struct with
// activity.RegisterOptions{Name: "MyActivities_"} prepends that prefix to every
// method name, so these register as "MyActivities_Greet" and "MyActivities_Farewell".
type GreetingActivities struct{}

func (GreetingActivities) Greet(_ context.Context, who string) (string, error) {
	return fmt.Sprintf("Hello, %s!", who), nil
}

func (GreetingActivities) Farewell(_ context.Context, who string) (string, error) {
	return fmt.Sprintf("Goodbye, %s!", who), nil
}

// RegisteredActivity describes one entry in the worker's activity registry.
// Impl is the function's Go-qualified name (from runtime.FuncForPC), and PC is
// its program-counter address. Two registered Names with identical PC/Impl
// point at the same Go function — that is how you compare them.
type RegisteredActivity struct {
	Name string
	Impl string
	PC   uintptr
}

// listRegisteredActivities reads the worker's unexported `registry.activityFuncMap`
// via reflection + unsafe. The SDK has no public API for this, so the field names
// (`registry`, `activityFuncMap`, `fn`) are tied to an internal layout that can
// change between SDK versions — verified against go.temporal.io/sdk v1.44.0.
func listRegisteredActivities(w worker.Worker) []RegisteredActivity {
	read := func(v reflect.Value, field string) reflect.Value {
		f := v.FieldByName(field)
		return reflect.NewAt(f.Type(), unsafe.Pointer(f.UnsafeAddr())).Elem()
	}

	aw := reflect.ValueOf(w).Elem()    // *internal.AggregatedWorker -> struct
	reg := read(aw, "registry").Elem() // *registry -> struct
	m := read(reg, "activityFuncMap")  // map[string]activity (interface)

	out := make([]RegisteredActivity, 0, m.Len())
	for _, k := range m.MapKeys() {
		// map value is `activity` interface -> *activityExecutor -> activityExecutor.
		exec := m.MapIndex(k).Elem().Elem()
		fnVal := reflect.ValueOf(read(exec, "fn").Interface())

		entry := RegisteredActivity{Name: k.String(), Impl: fnVal.Type().String()}
		if fnVal.Kind() == reflect.Func {
			entry.PC = fnVal.Pointer()
			if rf := runtime.FuncForPC(entry.PC); rf != nil {
				entry.Impl = rf.Name()
			}
		}
		out = append(out, entry)
	}
	sort.Slice(out, func(i, j int) bool { return out[i].Name < out[j].Name })
	return out
}

// printActivities formats the registry contents and flags duplicate implementations.
func printActivities(acts []RegisteredActivity) {
	log.Println("Registered activities:")
	byPC := map[uintptr][]string{}
	for _, a := range acts {
		log.Printf("  %-24s -> %s  [pc=0x%x]", a.Name, a.Impl, a.PC)
		byPC[a.PC] = append(byPC[a.PC], a.Name)
	}
	for pc, names := range byPC {
		if len(names) > 1 {
			log.Printf("  ↳ same impl (pc=0x%x) registered under: %v", pc, names)
		}
	}
}

func main() {
	// The client and worker are heavyweight objects that should be created once per process.
	c, err := client.Dial(envconfig.MustLoadDefaultClientOptions())
	if err != nil {
		log.Fatalln("Unable to create client", err)
	}
	defer c.Close()

	w := worker.New(c, "hello-world", worker.Options{})

	w.RegisterWorkflow(helloworld.Workflow)
	w.RegisterActivity(helloworld.Activity)

	w.RegisterActivityWithOptions(
		activities_2.Activity,
		activity.RegisterOptions{
			Name: "Activity_alias_2",
			//DisableAlreadyRegisteredCheck: true,
		},
	)

	//Activity, Activity, Activity_registered_with_options

	// Activity, Activity_registered_with_options

	printActivities(listRegisteredActivities(w))

	err = w.Run(worker.InterruptCh())

	if err != nil {
		log.Fatalln("Unable to start worker", err)
	}
}
