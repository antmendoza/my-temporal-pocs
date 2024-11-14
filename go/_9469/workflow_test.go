package activity_schedule_to_close

import (
	"context"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/require"
	"github.com/stretchr/testify/suite"
	"go.temporal.io/sdk/activity"
	"go.temporal.io/sdk/client"
	"go.temporal.io/sdk/testsuite"
	"go.temporal.io/sdk/worker"
	"testing"
	"time"
)

type DelayUnitTestSuite struct {
	suite.Suite
	testsuite.WorkflowTestSuite
}

func TestUnitTestSuite(t *testing.T) {
	suite.Run(t, new(DelayUnitTestSuite))
}

func (s *DelayUnitTestSuite) Test_SampleGreetingsWorkflow() {
	env := s.NewTestWorkflowEnvironment()
	env.SetTestTimeout(time.Second * 6)

	var a *Activities
	//env.RegisterActivity(a)

	env.OnActivity(a.GetGreeting).Return("Hello", nil)

	// If I uncomment this line and comment out the replacement below, the test passes
	//env.OnActivity(a.GetName).Return("World", nil)
	env.OnActivity(a.GetName, mock.Anything).Return(func(ctx context.Context) (string, error) {

		if activity.GetInfo(ctx).Attempt == 1 {
			time.Sleep(4 * time.Second)
		}

		return "World", nil

		//fmt.Printf("pause pr check: %v\n", pausePrCheck)
		//if pausePrCheck {
		//	time.Sleep(4 * time.Second)
		//}
		//pausePrCheck = false
		//return "World", nil
	})

	env.OnActivity(a.SayGreeting, "Hello", "World").Return("Hello World!", nil)

	env.ExecuteWorkflow(GreetingSample)

	s.True(env.IsWorkflowCompleted())
	s.NoError(env.GetWorkflowError())

	env.AssertExpectations(s.T())
}

//https://github.com/stretchr/testify#mock-package ++
//https://github.com/golang/mock

////----

func Test_Using_DevServer(t *testing.T) {
	//"" will let use a random port in local env
	hostPort := ""
	server, err := testsuite.StartDevServer(context.Background(), testsuite.DevServerOptions{ClientOptions: &client.Options{HostPort: hostPort}})
	require.NoError(t, err)
	require.NotNil(t, server)

	var (
		c       client.Client
		w       worker.Worker
		wInChan <-chan interface{}
	)

	taskQ := "hello-world"

	ch := make(chan interface{})
	go func() {
		c = server.Client()
		w = worker.New(c, taskQ, worker.Options{})
		wInChan = worker.InterruptCh()

		ch <- struct{}{}

		_ = w.Run(wInChan)
	}()

	<-ch

	require.NotNil(t, c)
	require.NotNil(t, w)
	require.NotNil(t, wInChan)

	// register activity and workflow
	w.RegisterWorkflow(GreetingSample)
	activities := &ActivitiesMocked{Name: "Temporal", Greeting: "Hello"}
	w.RegisterActivity(activities)

	// run the workflow application (equivalent to starter/main.go)
	workflowOptions := client.StartWorkflowOptions{
		ID:        "hello_world_workflowID",
		TaskQueue: taskQ,
	}

	we, err := c.ExecuteWorkflow(context.Background(), workflowOptions, GreetingSample)
	require.NoError(t, err)
	require.NotNil(t, we)

	// Synchronously wait for the workflow completion.
	var result string
	err = we.Get(context.Background(), &result)
	require.NoError(t, err)
	require.Equal(t, "Greeting: Hello mocked_activity!\n", result)
	//TODO check workflow history to verify number or attends in activityTaskStarted

	// stop worker
	w.Stop()

	// stop server
	err = server.Stop()
	require.NoError(t, err)
}
