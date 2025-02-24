package notification

import (
	"errors"
	"go.temporal.io/sdk/client"
	"go.temporal.io/sdk/temporal"
	"go.temporal.io/sdk/workflow"
	"strconv"
	"time"
)

func NotifyCell(ctx workflow.Context, request NotifyCellRequest) (NotifyCellResponse, error) {

	//TODO validate inputs
	//TODO local activity
	ao := workflow.ActivityOptions{
		StartToCloseTimeout: 10 * time.Second,
		RetryPolicy: &temporal.RetryPolicy{
			MaximumAttempts: 5,
		},
	}
	ctx = workflow.WithActivityOptions(ctx, ao)

	logger := workflow.GetLogger(ctx)
	logger.Info("NotifyCell started", "request", request)

	var a *NotificationActivity

	var getCustomersResult []Customer
	var workflowresult = NotifyCellResponse{}

	err := workflow.ExecuteActivity(ctx, a.GetCustomersInCell, request.CustomerType).Get(ctx, &getCustomersResult)
	if err != nil {
		logger.Error("Activity failed.", "Error", err)
		return workflowresult, err
	}

	var result NotifyCustomersResponse

	cwo := workflow.ChildWorkflowOptions{
		WorkflowID: workflow.GetInfo(ctx).WorkflowExecution.ID + "/notify-customers",
	}
	ctx = workflow.WithChildOptions(ctx, cwo)

	input := NotifyCustomersRequest{
		Customers: getCustomersResult,
		Text:      request.Text,
	}
	err = workflow.ExecuteChildWorkflow(ctx, NotifyCustomers, input).Get(ctx, &result)
	if err != nil {
		logger.Error("Parent execution received NotifyCustomers execution failure.", "Error", err)
		return workflowresult, err
	}
	logger.Info("1 NotifyCell completed.", "result", workflowresult)

	workflowresult = NotifyCellResponse{
		Customers:      getCustomersResult,
		ErrorsToNotify: result.ErrorsToNotify,
	}
	logger.Info("NotifyCell completed.", "result", workflowresult)

	return workflowresult, nil
}

func NotifyCustomers(ctx workflow.Context, request NotifyCustomersRequest) (NotifyCustomersResponse, error) {

	customers := request.Customers
	response := NotifyCustomersResponse{
		Customers: customers,
	}

	logger := workflow.GetLogger(ctx)
	completed := 0

	for _, customer := range customers {

		cwo := workflow.ChildWorkflowOptions{
			WorkflowID: workflow.GetInfo(ctx).WorkflowExecution.ID + "/notify-customer[" + customer.CustomerId + "]",
		}
		ctx = workflow.WithChildOptions(ctx, cwo)
		input := NotifyCustomerRequest{
			Customer: customer,
			Text:     request.Text,
		}

		workflow.Go(ctx, func(ctx workflow.Context) {
			var result NotifyCustomerResponse
			err := workflow.ExecuteChildWorkflow(ctx, NotifyCustomer, input).Get(ctx, &result)
			if err != nil {
				logger.Error("Parent execution received NotifyCustomer execution failure.", "Error", err)
				response.ErrorsToNotify = append(response.ErrorsToNotify, NotificationError{
					Customer: customer,
					Error:    err.Error(),
				})
			}
			completed++
		})
	}

	//TODO continue as new if needed
	workflow.Await(ctx, func() bool {
		return len(customers) == completed
	})

	return response, nil
}

func NotifyCustomer(ctx workflow.Context, request NotifyCustomerRequest) (NotifyCustomerResponse, error) {

	//get notification channel

	// if slack
	// send slack notification
	// else
	// get users
	//create zendesk ticket

	//convert to integer request.Customer.CustomerId
	//if even number, simulate failure
	//else return success

	x, _ := strconv.Atoi(request.Customer.CustomerId)
	if x%2 == 0 {
		//simulate failure
		return NotifyCustomerResponse{}, errors.New("Simulated failure")
	}

	return NotifyCustomerResponse{
		Customer: request.Customer,
	}, nil
}

type NotificationActivity struct {
	WorkflowClient client.Client
}

func (a *NotificationActivity) GetCustomersInCell(request CustomerTypeFilter) ([]Customer, error) {

	customers := getDataSetCustomers(request.CellId)

	filteredCustomers := []Customer{}
	for _, customer := range customers {
		for _, filter := range request.Types {
			if customer.Type == filter {
				filteredCustomers = append(filteredCustomers, customer)
			}
		}
	}
	return filteredCustomers, nil
}

func createCustomer(type_ string, cellId string, customerId string) Customer {
	return Customer{
		Type:         type_,
		CellId:       cellId,
		CustomerName: "Customer " + customerId,
		CustomerId:   customerId,
	}
}

func getDataSetCustomers(cellId string) []Customer {
	customers := []Customer{
		createCustomer("mission-critical", cellId, "1"),
		createCustomer("mission-critical", cellId, "2"),
		createCustomer("enterprise", cellId, "3"),
		createCustomer("enterprise", cellId, "4"),
		createCustomer("essential", cellId, "5"),
	}
	return customers
}
