import {proxyActivities, proxyLocalActivities} from "@temporalio/workflow";
import type * as activities from "./activities";

const {sleepRandom_2, greet, sleepRandom} = proxyActivities<typeof activities>({
    startToCloseTimeout: "10 seconds",
});

const {sleepRandomLocal} = proxyLocalActivities<
    typeof activities
>({
    startToCloseTimeout: "10 seconds",
});

function printActivities() {

    if (!globalThis.activities) {
        globalThis.activities = [];
    }

    console.log(globalThis.activities.join(", "));
}

export async function ExampleWorkflow(): Promise<void> {


    let number = 0
    await Promise.all(
        [1, 3].map(async (offset) => {
            await Promise.all(
                ["a", "b", "c", "d", "e"].map(async (c) => {

                    console.log(`sleepRandomLocal start ${offset} ${c}`);


                    printActivities();
                    await sleepRandomLocal(1, 100, `${offset} ${c}`);

                    console.log(`sleepRandom start ${offset} ${c}`);
                    printActivities();
                    await sleepRandom(offset * 1000, offset * 1000, `${offset} ${c}`);
                })
            );

            console.log(`greet start ${offset}`);
            printActivities();
            await greet(offset.toString());
        })
    );


    console.log(globalThis.activities.join(", "));


    console.log(globalThis.activities.join(", "));
    // Throw an error to trigger the replay of the workflow in the same run.
    // Delete this line and replay the history would also throw non-deterministic error.
    throw new Error("Something went wrong");
}
