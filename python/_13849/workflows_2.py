## this file show the fix for the error mentioned in the readme

from temporalio import workflow



with workflow.unsafe.imports_passed_through():
    from my_executor import MyExecutor

@workflow.defn
class ExecutorRestrictedWorkflow:
    @workflow.run
    async def run(self, name: str) -> str:

        submit = MyExecutor().submit()

        return "Hello, " + submit