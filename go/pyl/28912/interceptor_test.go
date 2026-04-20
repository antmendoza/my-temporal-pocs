package update_with_start_interceptor_test

import (
	"context"
	"testing"

	"github.com/stretchr/testify/require"
	update_with_start_interceptor "github.com/temporalio/samples-go"
	"go.temporal.io/sdk/client"
	"go.temporal.io/sdk/interceptor"
)

// taskQueueOverridingInterceptor simulates what an automated acceptance test
// system does: redirect all workflow starts to a test-specific task queue so
// that workers under test handle the workflows instead of production workers.
//
// This pattern works for SignalWithStartWorkflow but breaks for
// UpdateWithStartWorkflow because the start options are inaccessible inside the
// opaque WithStartWorkflowOperation interface.
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

// SignalWithStartWorkflow can override the task queue because
// ClientSignalWithStartWorkflowInput exposes Options *client.StartWorkflowOptions.
func (o *taskQueueOverridingOutboundInterceptor) SignalWithStartWorkflow(
	ctx context.Context,
	in *interceptor.ClientSignalWithStartWorkflowInput,
) (client.WorkflowRun, error) {
	in.Options.TaskQueue = o.testTaskQueue
	return o.Next.SignalWithStartWorkflow(ctx, in)
}

// UpdateWithStartWorkflow cannot override the task queue because
// ClientUpdateWithStartWorkflowInput only provides a
// client.WithStartWorkflowOperation interface value. That interface has no
// exported method to read or mutate the underlying StartWorkflowOptions, so
// the workflow will start on whatever task queue was set at the call site —
// bypassing the test system's override entirely.
func (o *taskQueueOverridingOutboundInterceptor) UpdateWithStartWorkflow(
	ctx context.Context,
	in *interceptor.ClientUpdateWithStartWorkflowInput,
) (client.WorkflowUpdateHandle, error) {
	// No-op: there is no supported way to override the task queue here.
	return o.Next.UpdateWithStartWorkflow(ctx, in)
}

// recordingInterceptor sits at the end of the chain and captures inputs for
// assertion, returning zero values so no real RPC is made.
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

// fakeStartWorkflowOp is a test-only implementation of
// client.WithStartWorkflowOperation that stores its options in an exported
// field so tests can inspect them after the interceptor chain has run.
//
// In production code the only concrete implementation is the SDK's unexported
// internal.withStartWorkflowOperationImpl, so this backdoor does not exist.
type fakeStartWorkflowOp struct {
	TaskQueue string // inspectable by the test; not reachable via the interface
}

func (f *fakeStartWorkflowOp) Get(_ context.Context) (client.WorkflowRun, error) {
	return nil, nil
}

// TestAcceptanceTestInterceptorCannotOverrideUpdateWithStartTaskQueue is the
// core reproduction. It demonstrates that a ClientOutboundInterceptor which
// works correctly for SignalWithStartWorkflow is unable to perform the same
// task-queue override for UpdateWithStartWorkflow.
//
// An automated acceptance test system that intercepts workflow-with-start calls
// to redirect task queues (or inject metadata, check auth, etc.) will silently
// fail to apply those modifications when the caller uses UpdateWithStartWorkflow.
func TestAcceptanceTestInterceptorCannotOverrideUpdateWithStartTaskQueue(t *testing.T) {
	const prodTaskQueue = update_with_start_interceptor.TaskQueue
	const testTaskQueue = "acceptance-test-task-queue"

	recorder := &recordingInterceptor{}
	override := &taskQueueOverridingInterceptor{testTaskQueue: testTaskQueue}
	chain := override.InterceptClient(recorder)

	// ── SignalWithStartWorkflow ─────────────────────────────────────────────────
	// The interceptor CAN override the task queue because in.Options is a plain
	// *client.StartWorkflowOptions that is directly mutable.
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
	// The interceptor CANNOT override the task queue. The start options live
	// inside an opaque client.WithStartWorkflowOperation interface that exposes
	// only Get(ctx) (WorkflowRun, error). There is no supported way to read or
	// modify the TaskQueue (or any other StartWorkflowOptions field) from an
	// interceptor.
	//
	// Note: fakeStartWorkflowOp is used here to avoid a real server connection.
	// In production, client.NewWithStartWorkflowOperation returns the SDK's own
	// unexported type, which is even less accessible than fakeStartWorkflowOp.
	startOp := &fakeStartWorkflowOp{TaskQueue: prodTaskQueue}
	_, _ = chain.UpdateWithStartWorkflow(context.Background(), &interceptor.ClientUpdateWithStartWorkflowInput{
		UpdateOptions: &client.UpdateWorkflowOptions{
			WorkflowID: "test-update",
			UpdateName: update_with_start_interceptor.UpdateName,
		},
		StartWorkflowOperation: startOp,
	})

	// BUG: the task queue was not overridden. The assertion below mirrors the one
	// for SignalWithStartWorkflow and expresses the expected behavior. It fails
	// because the interceptor has no way to reach the StartWorkflowOptions
	// inside the opaque WithStartWorkflowOperation.
	require.Equal(t, testTaskQueue, startOp.TaskQueue,
		"interceptor should redirect UpdateWithStartWorkflow to the test task queue")
}
