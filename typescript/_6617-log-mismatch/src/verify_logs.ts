import fs from "fs";

//const allFileContents = fs.readFileSync('./test.log', 'utf-8');

const allFileContents = fs.readFileSync('./combined_.log', 'utf-8');

function checkInputAgainstWorkflowId(line: string) {

    const inputPosition = line.indexOf("input [");
    const lineFromInput = line.substring(inputPosition)
    const inputAsWorkflowId = lineFromInput.substring(
        lineFromInput.indexOf("[")+1,
        lineFromInput.indexOf("]"))


    const searchString = "workflowRunId\":\"";
    const workflowRunIdFromConsoleLogPosition = line.indexOf(searchString)+searchString.length;
    const lineSearchWorkflowRunId = line.substring(workflowRunIdFromConsoleLogPosition)
    const workflowRunId = lineSearchWorkflowRunId.substring(
        0,
        lineSearchWorkflowRunId.indexOf("\""))


    return inputAsWorkflowId != workflowRunId;

}


function run(){

    const lines = allFileContents.split(/\r?\n/);
    console.log("lines.length " + lines.length)
    const activityLogs = lines.filter((l) =>
        {
            return l.includes("activity_workflow_id")
        }
    );
    console.log("activityLogs.length " + activityLogs.length)
    activityLogs.forEach(line =>  {

        if(checkInputAgainstWorkflowId(line)){
            console.log(`log mismatch: `);
            console.log(`       ${line}`);
        }
    });


}

run();

