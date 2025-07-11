import concurrent


class MyExecutor(concurrent.futures.Executor):
    def submit(self):
        return "Done"

