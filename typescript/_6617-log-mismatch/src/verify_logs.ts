import fs from "fs";

const allFileContents = fs.readFileSync('./test.log', 'utf-8');

function checkInputAgainstWorkflowId(line: string) {

    const inputPosition = line.indexOf("input [");
    const lineFromInput = line.substring(inputPosition)
    const inputAsWorkflowId = lineFromInput.substring(
        lineFromInput.indexOf("[")+1,
        lineFromInput.indexOf("]"))


    const searchString = "workflowId\":\"";
    const workflowIdFromConsoleLogPosition = line.indexOf(searchString)+searchString.length;
    const lineSearchWorkflowId = line.substring(workflowIdFromConsoleLogPosition)
    const workflowId = lineSearchWorkflowId.substring(
        0,
        lineSearchWorkflowId.indexOf("\""))


    return inputAsWorkflowId != workflowId;

}

allFileContents.split(/\r?\n/).filter((l) =>
    {
        return l.includes("activity_workflow_id")
    }
).forEach(line =>  {

    if(checkInputAgainstWorkflowId(line)){

        console.log(`log mismatch: `);
        console.log(`       ${line}`);


    }

});





