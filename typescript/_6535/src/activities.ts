// @@@SNIPSTART typescript-hello-activity
import * as fs from 'fs';
import {Context} from "@temporalio/activity";

export async function greet(name: string): Promise<string> {



    try {

        Context.current().cancelled.catch(e => {
            console.log("Inside cancelled activity > " + e)
            throw e;
        })

        for (let i = 0; i < 10; i++) {
            await new Promise(f => setTimeout(f, 1000));

            Context.current().heartbeat("");

        }


        return `Hello, ${name}!`;
    } catch (e) {
        console.log("Inside the activity >>> " + e)
        throw  e;
    }


}

// @@@SNIPEND
