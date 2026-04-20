package update_with_start_interceptor_test

import (
	"context"
	"testing"

	"github.com/stretchr/testify/require"
	update_with_start_interceptor "github.com/temporalio/samples-go"
	"go.temporal.io/sdk/client"
	"go.temporal.io/sdk/interceptor"
)

type taskQueueOverridingInterceptor struct {
	interceptor.ClientInterceptorBase
	testTaskQueue string
}

func (t *taskQueueOverridingInterceptor) InterceptClient(
	next interceptor.ClientOutboundInterceptor,
) interceptor.ClientOutboundInterceptor {
	return &taskQueueOverridingOutboundInterceptor{
		ClientOutboundInterceptorBase: interceptor.ClientOutboundInterceptorBase{Next: next},
		testTaskQueue:                 t.testTaskQueue,
	}
}

type taskQueueOverridingOutboundInterceptor struct {
	interceptor.ClientOutboundInterceptorBase
	testTaskQueue string
}

func (o *taskQueueOverridingOutboundInterceptor) SignalWithStartWorkflow(
	ctx context.Context,
	in *interceptor.ClientSignalWithStartWorkflowInput,
) (client.WorkflowRun, error) {
	in.Options.TaskQueue = o.testTaskQueue
	return o.Next.SignalWithStartWorkflow(ctx, in)
}

func (o *taskQueueOverridingOutboundInterceptor) UpdateWithStartWorkflow(
	ctx context.Context,
	in *interceptor.ClientUpdateWithStartWorkflowInput,
) (client.WorkflowUpdateHandle, error) {
	// No-op: there is no supported way to override the task queue here.
	return o.Next.UpdateWithStartWorkflow(ctx, in)
}

type recordingInterceptor struct {
	interceptor.ClientOutboundInterceptorBase
	capturedSignalOptions *client.StartWorkflowOptions
	capturedUpdateStartOp client.WithStartWorkflowOperation
}

func (r *recordingInterceptor) SignalWithStartWorkflow(
	_ context.Context,
	in *interceptor.ClientSignalWithStartWorkflowInput,
) (client.WorkflowRun, error) {
	r.capturedSignalOptions = in.Options
	return nil, nil
}

func (r *recordingInterceptor) UpdateWithStartWorkflow(
	_ context.Context,
	in *interceptor.ClientUpdateWithStartWorkflowInput,
) (client.WorkflowUpdateHandle, error) {
	r.capturedUpdateStartOp = in.StartWorkflowOperation
	return nil, nil
}

type fakeStartWorkflowOp struct {
	TaskQueue string // inspectable by the test; not reachable via the interface
}

func (f *fakeStartWorkflowOp) Get(_ context.Context) (client.WorkflowRun, error) {
	return nil, nil
}

func TestAcceptanceTestInterceptorCannotOverrideUpdateWithStartTaskQueue(t *testing.T) {
	const prodTaskQueue = update_with_start_interceptor.TaskQueue
	const testTaskQueue = "acceptance-test-task-queue"

	recorder := &recordingInterceptor{}
	override := &taskQueueOverridingInterceptor{testTaskQueue: testTaskQueue}
	chain := override.InterceptClient(recorder)

	// ── SignalWithStartWorkflow ─────────────────────────────────────────────────
	_, _ = chain.SignalWithStartWorkflow(context.Background(), &interceptor.ClientSignalWithStartWorkflowInput{
		SignalName:   update_with_start_interceptor.DoneSignal,
		WorkflowType: "GreetingWorkflow",
		Options: &client.StartWorkflowOptions{
			ID:        "test-signal",
			TaskQueue: prodTaskQueue,
		},
	})
	require.Equal(t, testTaskQueue, recorder.capturedSignalOptions.TaskQueue,
		"interceptor should redirect SignalWithStartWorkflow to the test task queue")

	// ── UpdateWithStartWorkflow ─────────────────────────────────────────────────
	startOp := &fakeStartWorkflowOp{TaskQueue: prodTaskQueue}
	_, _ = chain.UpdateWithStartWorkflow(context.Background(), &interceptor.ClientUpdateWithStartWorkflowInput{
		UpdateOptions: &client.UpdateWorkflowOptions{
			WorkflowID: "test-update",
			UpdateName: update_with_start_interceptor.UpdateName,
		},
		StartWorkflowOperation: startOp,
	})

	// question is if there is a way to satisfy this assertion
	require.Equal(t, testTaskQueue, startOp.TaskQueue,
		"interceptor should redirect UpdateWithStartWorkflow to the test task queue")
}
