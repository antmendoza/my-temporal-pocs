import {proxyActivities, proxyLocalActivities} from "@temporalio/workflow";
import type * as activities from "./activities";

const {greet, sleepRandom} = proxyActivities<typeof activities>({
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


    await Promise.all(
        [1, 3].map(async (offset) => {
            await Promise.all(
                ["a", "b", "c", "d", "e"].map(async (c) => {

                    console.log(`sleepRandomLocal start ${offset} ${c}`);


                    printActivities();
                    //schedule local activity
                    await sleepRandomLocal(1, 100, `${offset} ${c}`);


                    console.log(`sleepRandom start ${offset} ${c}`);
                    printActivities();
                    //schedule activity
                    await sleepRandom(offset * 1000, offset * 1000, `${offset} ${c}`);
                })
            );

            console.log(`greet start ${offset}`);
            printActivities();
            //schedule activity
            await greet(offset.toString());
        })
    );


    printActivities();

    // Throw an error to force replay
    throw new Error("Something went wrong");
}
