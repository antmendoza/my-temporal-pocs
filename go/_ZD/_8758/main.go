package test

import (
	"context"
	"fmt"
	"go.temporal.io/api/enums/v1"
	"go.temporal.io/sdk/activity"
	"time"
)

import (
	"go.temporal.io/sdk/client"
	"go.temporal.io/sdk/temporal"
	"go.temporal.io/sdk/workflow"
)

// Our worker:
// w := worker.New(
//      c,
//      cfg.TemporalQueue,
//      worker.Options{
//          Identity:                                cfg.TemporalWorkerName,
//          WorkflowPanicPolicy:                     worker.FailWorkflow,
//          MaxConcurrentWorkflowTaskPollers:        128,
//          MaxConcurrentActivityTaskPollers:        256,
//          MaxConcurrentWorkflowTaskExecutionSize:  2000,
//          MaxConcurrentActivityExecutionSize:      2000,
//          MaxConcurrentLocalActivityExecutionSize: 2000,
//      },
// )

type WorkflowManagerConfig struct {
	InitialInterval    time.Duration
	BackoffCoefficient float64
	MaximumInterval    time.Duration
	MaximumAttempts    int32
	ActivityTimeout    time.Duration
}

func (c *WorkflowManagerConfig) GetChildWorkflowOptions() workflow.ChildWorkflowOptions {
	return workflow.ChildWorkflowOptions{
		RetryPolicy: c.GetRetryPolicy(),
	}
}

func (c *WorkflowManagerConfig) GetActivityOptions() workflow.ActivityOptions {
	return workflow.ActivityOptions{
		StartToCloseTimeout: 1 * time.Minute,
		RetryPolicy:         c.GetRetryPolicy(),
	}
}

func (c *WorkflowManagerConfig) GetRetryPolicy() *temporal.RetryPolicy {
	return &temporal.RetryPolicy{
		InitialInterval:    1 * time.Second,
		BackoffCoefficient: 2.0,
		MaximumInterval:    30 * time.Second,
		MaximumAttempts:    5,
	}
}

type WorkflowManager struct {
	config WorkflowManagerConfig
	client client.Client
}

type Users struct {
}

type SendParams struct {
	TextpickID     interface{}
	DiscordRoleIDs interface{}
}

func (m WorkflowManager) Send(ctx workflow.Context, params SendParams) error {
	logger := workflow.GetLogger(ctx)
	logger.Info("Sending play", "textpick_id", params.TextpickID, "discord_roles", params.DiscordRoleIDs)

	ctx = workflow.WithActivityOptions(ctx, m.config.GetActivityOptions())

	// Fetch users
	var users []Users
	err := workflow.ExecuteActivity(ctx, activities.GetUsers).Get(ctx, &users)
	if err != nil {
		return fmt.Errorf("getting users: %v", err)
	}

	// Our initial approach - spin up a child workflow for each user
	// Horrible performance, visibly improved by batching, but even then it doesn't scale very well
	// ctx = workflow.WithChildOptions(ctx, m.config.GetChildWorkflowOptions())
	// selector := workflow.NewSelector(ctx)
	// for _, u := range users {
	//  selector.AddFuture(
	//      workflow.ExecuteChildWorkflow(ctx, m.SendToUser, u),
	//      func(f workflow.Future) {
	//          if err := f.Get(ctx, nil); err != nil {
	//              logger.Error("Error sending to user", "user", u, "error", err)
	//          }
	//      },
	//  )
	// }
	// for range len(users) {
	//  selector.Select(ctx)
	// }

	// This is what we tried after your suggestion - activity that schedules a workflow
	// It might be slightly better, but still starts to choke when there are more than, let's say, 200 users
	// Sleeping after 'some' number of execs helps a little bit but it doesnt scale too good - we need to execute as many concurretly as possible
	// Batching has pretty much the same effect as sleeping, and with growing number of users its getting way worse
	selector := workflow.NewSelector(ctx)

	//Changed antonio
	//for _, u := range users {
	selector.AddFuture(
		//pass all the users to the activity
		workflow.ExecuteActivity(ctx, m.DispatchSend, users),
		func(f workflow.Future) {
			if err := f.Get(ctx, nil); err != nil {
				logger.Error("Error sending to user", "user", u, "error", err)
			}
		},
	)
	//}

	for range len(users) {
		selector.Select(ctx)
	}

	logger.Info("Finished sending play", "textpick_id", params.TextpickID, "subs_count", len(subs))
	return nil
}

type User struct {
}

func (m *WorkflowManager) SendToUser(ctx workflow.Context, user User) error {
	// This is executing two activities, both of them are simple and fast
	return nil
}

// Changed antonio
func (m *WorkflowManager) DispatchSend(ctx context.Context, users []Users) error {

	startIndex := 0
	// Check if this activity has previous heartbeat to retrieve progress from it
	if activity.HasHeartbeatDetails(ctx) {
		var finishedIndex int
		if err := activity.GetHeartbeatDetails(ctx, &finishedIndex); err == nil {
			// we have finished progress
			startIndex = finishedIndex + 1 // start from next one.
		}
	}

	for index := startIndex; index < len(users); index++ {
		u := users[index]

		// Start the workflow execution and don't wait for it to complete here in this activity
		_, err := m.client.ExecuteWorkflow(context.Background(), client.StartWorkflowOptions{
			TaskQueue: "somequeue",

			//set a meaningful workflow ID to deduplicate requests if the activity retries
			ID: activity.GetInfo(ctx).WorkflowExecution.ID + "/" + string(index), //or + userId?

			//YOu might want to set here WORKFLOW_ID_REUSE_POLICY_ALLOW_DUPLICATE_FAILED_ONLY or WORKFLOW_ID_REUSE_POLICY_REJECT_DUPLICATE
			// it depends on the use-case.
			//Default policy is WORKFLOW_ID_REUSE_POLICY_ALLOW_DUPLICATE that might now work in your case if the activity retries and by that time the workflow
			// is already completed (you don't want to start a new one for the same user in the batch)
			WorkflowIDReusePolicy: enums.WORKFLOW_ID_REUSE_POLICY_ALLOW_DUPLICATE_FAILED_ONLY,
		}, m.SendToUser, u)

		//If the error is workflowAlreadyExist you can ignore it. This can happen if the activity retries. Please test this.
		if err != nil {
			return fmt.Errorf("executing workflow: %w", err)
		}

		//This will block until the workflow completes.
		//If you need to record the result in the initiator workflow you can signal from the workflows that start here, before each one complete.
		//we can work on this if this is the case but take the workflow limits into account.
		// - 	https://docs.temporal.io/cloud/limits#per-workflow-execution-concurrency-limits
		// -	https://docs.temporal.io/cloud/limits#per-workflow-execution-signal-limit
		// - 	https://docs.temporal.io/cloud/limits#workflow-execution-event-history-limits

		//		err = run.Get(context.Background(), nil)
		//		if err != nil {
		//			return fmt.Errorf("getting workflow result: %w", err)
		//		}

		//spread RPS
		if index%200 == 0 {
			time.Sleep(time.Millisecond * 200)

			//he can heartbeat here, to track process, if the activity retries we can continue from the last heartbeat info
			// sent to the server.
			// https://docs.temporal.io/encyclopedia/detecting-activity-failures#throttling
			activity.RecordHeartbeat(ctx, index)

		}

	}

	return nil

}
