import {
  WorkflowInterceptorsFactory,
  ActivityInput, LocalActivityInput,
} from "@temporalio/workflow";


declare global {
  var activities: string[];
}





export const interceptors: WorkflowInterceptorsFactory = () => ({
  outbound: [
    {
      scheduleActivity: async (
        input: ActivityInput,
        next: (input: ActivityInput) => Promise<unknown>
      ) => {

        if(!globalThis.activities) {
            globalThis.activities = [];
        }



        globalThis.activities.push(input.activityType+"-"+input.args[2])



        return await next(input);
      },

      scheduleLocalActivity: async (
          input: LocalActivityInput,
          next: (input: LocalActivityInput) => Promise<unknown>
      ) => {


        if(!globalThis.activities) {
          globalThis.activities = [];
        }

        globalThis.activities.push(input.activityType+"-"+input.args[2])


        return await next(input);
      },
    },
  ],
});
