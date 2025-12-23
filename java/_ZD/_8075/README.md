This project shows how to set a timer, using interceptors, that runs recursively every 1s. 

We wrap the timer in a cancellable scope and cancel the scope before completing the workflow. 