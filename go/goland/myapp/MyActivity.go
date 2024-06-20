package myapp

import "context"

type YourActivityParam struct {
	ActivityParamX string
	ActivityParamY int
}

type YourActivityObject struct {
}

type YourActivityResultObject struct {
}

// ...
func (a *YourActivityObject) YourActivityDefinition(ctx context.Context, param YourActivityParam) (*YourActivityResultObject, error) {
	// ...
	return nil, nil
}
