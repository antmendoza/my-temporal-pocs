// @@@SNIPSTART typescript-hello-workflow
import {proxyActivities} from '@temporalio/workflow';
// Only import the activity types
import type * as activities from './activities';

const {greet} = proxyActivities<typeof activities>({
    startToCloseTimeout: '1 minute',
});

/** A workflow that simply calls an activity */
export async function example(name: string): Promise<string> {

    // For big files, we only use search/replace feedback
    const strategies = [{name: 'ab', fn: ab},{name: 'greet', fn: greet}];

    let arg = () => {
        console.log("print something")
    };

    const responses = await Promise.allSettled(
        strategies.map(async ({name, fn}) => {
            console.log(`Trying fix strategy ${name}`);
            const result = await fn("arg");
            console.log(`Fix strategy ${name} returned`, {result});
            return result;
        }),
    );

    return await greet("arg");
}

// @@@SNIPEND


export async function ab(a: string) {
   // callback();
    return "";
}


