package notification

type NotifyCellRequest struct {
	CellId       string
	Text         string
	CustomerType CustomerTypeFilter
	Simulate     bool
}

type CustomerTypeFilter struct {
	CellId string
	Types  []string
}

type Customer struct {
	Type         string
	CellId       string
	CustomerName string
	CustomerId   string
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
