package io.temporal.samples.hello;

import io.temporal.activity.Activity;

public class ActivityImpl implements TestActivity2<MyRequest2, MyRequest2> {
    @Override
    public MyRequest2 activity1(MyRequest2 input) {
        final String hello = Activity.getExecutionContext().getInfo().getActivityType() + "-" + input;

        for (int i = 0; i < 2; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            System.out.println(">>>>>>>>>> " + hello);

            String heartbeat = i + 1 + "";
            Activity.getExecutionContext().heartbeat(heartbeat);
        }

        // return myresponse
        return input;


    }
}
