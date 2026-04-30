from datetime import timedelta

from temporalio import workflow


from temporalio import workflow
with workflow.unsafe.imports_passed_through():
    import concurrent.futures

@workflow.defn
class ExecutorRestrictedWorkflow:
    @workflow.run
    async def run(self, name: str) -> str:
        class MyExecutor(concurrent.futures.Executor):
            pass


        return "Hello, " + name
