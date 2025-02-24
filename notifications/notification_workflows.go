package notification

import (
	"errors"
	"go.temporal.io/sdk/client"
	"go.temporal.io/sdk/workflow"
	"strconv"
	"time"
)

type NotifyCellRequest struct {
	CellId       string
	Text         string
	CustomerType CustomerTypeFilter
	Simulate     bool
}

type CustomerTypeFilter struct {
	Types []string
}

type Customer struct {
	Type         string
	CellId       string
	CustomerName string
	CustomerId   string
}

type NotificationActivity struct {
	WorkflowClient client.Client
}

func (a *NotificationActivity) GetCustomers(request CustomerTypeFilter) ([]Customer, error) {

	customers := getDataSetCustomers()

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

type NotifyCellResponse struct {
	Customers      []Customer
	ErrorsToNotify []NotificationError
}

type NotifyCustomerRequest struct {
	Customer Customer
	Text     string
}

type NotifyCustomerResponse struct {
	Customer Customer
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

func NotifyCustomers(ctx workflow.Context, request NotifyCustomersRequest) (NotifyCustomersResponse, error) {

	response := NotifyCustomersResponse{
		Customers: request.Customers,
	}

	logger := workflow.GetLogger(ctx)

	for _, customer := range request.Customers {

		cwo := workflow.ChildWorkflowOptions{
			WorkflowID: workflow.GetInfo(ctx).WorkflowExecution.ID + "/notify-customer[" + customer.CustomerId + "]",
		}
		ctx = workflow.WithChildOptions(ctx, cwo)

		var result NotifyCustomerResponse
		input := NotifyCustomerRequest{
			Customer: customer,
			Text:     request.Text,
		}

		//TODO run in parallel

		err := workflow.ExecuteChildWorkflow(ctx, NotifyCustomer, input).Get(ctx, &result)
		if err != nil {
			logger.Error("Parent execution received NotifyCustomer execution failure.", "Error", err)
			response.ErrorsToNotify = append(response.ErrorsToNotify, NotificationError{
				Customer: customer,
				Error:    err.Error(),
			})
			//TODO track failed notification
			//			return workflowresult, err
		}

		workflow.GetLogger(ctx).Info("Notifying customer", "customer", customer)

		//TODO continue as new if needed
	}

	return response, nil
}

type NotifyCustomersRequest struct {
	Customers []Customer
	Text      string
}

type NotificationError struct {
	Customer Customer
	Error    string
}

type NotifyCustomersResponse struct {
	Customers      []Customer
	ErrorsToNotify []NotificationError
}

func NotifyCell(ctx workflow.Context, request NotifyCellRequest) (NotifyCellResponse, error) {

	//TODO validate inputs
	//TODO local activity
	ao := workflow.ActivityOptions{
		StartToCloseTimeout: 10 * time.Second,
	}
	ctx = workflow.WithActivityOptions(ctx, ao)

	logger := workflow.GetLogger(ctx)
	logger.Info("NotifyCell started", "request", request)

	var a *NotificationActivity

	var getCustomersResult []Customer
	var workflowresult = NotifyCellResponse{}

	err := workflow.ExecuteActivity(ctx, a.GetCustomers, request.CustomerType).Get(ctx, &getCustomersResult)
	if err != nil {
		logger.Error("Activity failed.", "Error", err)
		return workflowresult, err
	}

	var result NotifyCustomersResponse
	if !request.Simulate {

		cwo := workflow.ChildWorkflowOptions{
			WorkflowID: workflow.GetInfo(ctx).WorkflowExecution.ID + "/notify-customers",
		}
		ctx = workflow.WithChildOptions(ctx, cwo)

		input := NotifyCustomersRequest{
			Customers: getCustomersResult,
			Text:      request.Text,
		}
		err := workflow.ExecuteChildWorkflow(ctx, NotifyCustomers, input).Get(ctx, &result)
		if err != nil {
			logger.Error("Parent execution received NotifyCustomers execution failure.", "Error", err)
			return workflowresult, err
		}
	}
	logger.Info("1 NotifyCell completed.", "result", workflowresult)

	workflowresult = NotifyCellResponse{
		Customers:      getCustomersResult,
		ErrorsToNotify: result.ErrorsToNotify,
	}
	logger.Info("NotifyCell completed.", "result", workflowresult)

	return workflowresult, nil
}

func getDataSetCustomers() []Customer {

	customers := []Customer{
		createCustomer("mission-critical", "1", "1"),
		createCustomer("mission-critical", "1", "2"),
		createCustomer("enterprise", "1", "3"),
		createCustomer("enterprise", "1", "4"),
		createCustomer("essential", "1", "5"),
	}

	return customers

}

func createCustomer(type_ string, cellId string, customerId string) Customer {
	return Customer{
		Type:         type_,
		CellId:       cellId,
		CustomerName: "Customer " + customerId,
		CustomerId:   customerId,
	}
}
