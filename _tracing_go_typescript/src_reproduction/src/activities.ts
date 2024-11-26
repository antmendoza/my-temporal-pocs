import {sleep} from "@temporalio/activity";

export const createActivities = () => ({


    async activity1(msg: string): Promise<string> {
        console.log("Run activity 1")
        await sleep(2000)
        return `${msg}`;
    },


    async activity2(msg: string): Promise<string> {
        console.log("Run activity 2")
        await sleep(3)
        return `${msg}`;
    },




});



