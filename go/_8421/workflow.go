// @@@SNIPSTART samples-go-schedule-workflow
package _8421

import (
	"go.temporal.io/sdk/workflow"
)

type car struct {
	FieldA string
	FieldB string
}

// SampleScheduleWorkflowCar executes on the given schedule
func SampleScheduleWorkflowCar(ctx workflow.Context, car car) (result car, err error) {

	workflow.GetLogger(ctx).Info("Workflow input ", "car", car)

	return car, nil
}

type cat struct {
	FieldCatA string
	FieldCatB string
}

// SampleScheduleWorkflowCar executes on the given schedule
func SampleScheduleWorkflowCat(ctx workflow.Context, cat cat) (result cat, err error) {

	workflow.GetLogger(ctx).Info("Workflow input ", "cat", cat)

	return cat, nil
}
