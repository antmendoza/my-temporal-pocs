package customer

import (
	"go.temporal.io/sdk/client"
	"go.temporal.io/sdk/workflow"
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
	Customers []Customer
}

type NotifyCustomerRequest struct {
	Customer Customer
	Text     string
}

type NotifyCustomerResponse struct {
	Customer Customer
}

func NotifyCustomer(ctx workflow.Context, request NotifyCustomerRequest) (NotifyCustomerResponse, error) {

	return NotifyCustomerResponse{
		Customer: request.Customer,
	}, nil
}

func NotifyCustomers(ctx workflow.Context, request NotifyCustomersRequest) (NotifyCustomersResponse, error) {

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
		err := workflow.ExecuteChildWorkflow(ctx, NotifyCustomer, input).Get(ctx, &result)
		if err != nil {
			logger.Error("Parent execution received child execution failure.", "Error", err)
			//TODO track failed notification
			//			return workflowresult, err
		}

		workflow.GetLogger(ctx).Info("Notifying customer", "customer", customer)
	}

	return NotifyCustomersResponse{
		Customers: request.Customers,
	}, nil
}

type NotifyCustomersRequest struct {
	Customers []Customer
	Text      string
}

type NotifyCustomersResponse struct {
	Customers []Customer
}

func NotifyCell(ctx workflow.Context, request NotifyCellRequest) (NotifyCellResponse, error) {

	//TODO validate inputs

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

	if !request.Simulate {

		cwo := workflow.ChildWorkflowOptions{
			WorkflowID: workflow.GetInfo(ctx).WorkflowExecution.ID + "/notify-customers",
		}
		ctx = workflow.WithChildOptions(ctx, cwo)

		var result NotifyCustomersResponse
		input := NotifyCustomersRequest{
			Customers: getCustomersResult,
			Text:      request.Text,
		}
		err := workflow.ExecuteChildWorkflow(ctx, NotifyCustomers, input).Get(ctx, &result)
		if err != nil {
			logger.Error("Parent execution received child execution failure.", "Error", err)
			return workflowresult, err
		}

	}

	workflowresult = NotifyCellResponse{
		Customers: getCustomersResult,
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
