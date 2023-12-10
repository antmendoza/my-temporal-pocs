package io.antmendoza.samples._20231205;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.*;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@WorkflowInterface
public interface WizardUIWorkflow {

    @WorkflowMethod
    void run();

    @UpdateMethod
    String navigateTo(ScreenID screenID);


    @QueryMethod
    ScreenID getCurrentScreen();


    @UpdateMethod
    String submitScreen(UIRequest uiRequest);


    @UpdateValidatorMethod(updateName = "submitScreen")
    void submitScreenValidator(UIRequest uiRequest);


    class WizardUIWorkflowImplBufferRequests implements WizardUIWorkflow {

        private final Logger log = Workflow.getLogger("WizardUIWorkflowImpl");

        //For production development consider setting different activity options for each activity
        // and maybe different interfaces
        private final WizardUIActivity activity =
                Workflow.newActivityStub(WizardUIActivity.class,
                        ActivityOptions.newBuilder()
                                .setStartToCloseTimeout(Duration.ofSeconds(2))
                                .build());

        public List<UIRequest> data = new ArrayList<>();
        private ScreenID screen = null;

        @Override
        public void run() {

            while (!isLastScreen()) {

                CancellationScope cancellableTimer =
                        Workflow.newCancellationScope(
                                () -> {
                                    //for test purpose we use seconds, but it can be minutes, hours, days...
                                    final Duration duration = Duration.ofSeconds(3);
                                    Workflow.newTimer(duration).thenApply(t -> {
                                        activity.sendNotification();
                                        return t;
                                    });
                                });
                cancellableTimer.run();


                //TODO watch eventHistoryLength and CAN if > 1000
                Workflow.await(() -> !data.isEmpty()); //#1# See README.md
                cancellableTimer.cancel();

                //Process the data one by one
                UIRequest uiRequest = data.get(0);

                //this is business specific
                if (isScreen_3(uiRequest)) {
                    activity.activity3_1();
                    activity.activity3_2();
                    //and base on some "rules" we decide what is the next page
                    this.defineNavigation(uiRequest);
                }


                if (isScreen_2(uiRequest)) {
                    activity.activity2_1();
                    this.defineNavigation(uiRequest);
                }

                if (isScreen_1(uiRequest)) {
                    activity.activity1_1();
                    activity.activity1_2();
                    this.defineNavigation(uiRequest);
                }

                data.remove(uiRequest);

            }
        }

        @Override
        public String navigateTo(ScreenID screenID) {
            this.screen = screenID;
            return getCurrentScreen().toString();
        }

        @Override
        public ScreenID getCurrentScreen() {
            return this.screen == null ? ScreenID.SCREEN_1 : this.screen;
        }

        @Override
        public String submitScreen(UIRequest uiRequest) {

            Workflow.await(() -> this.data.isEmpty()); // #2#

            this.data.add(uiRequest);

            Workflow.await(() -> !this.data.contains(uiRequest)); // #3#

            return getCurrentScreen().toString();

        }

        @Override
        public void submitScreenValidator(UIRequest uiRequest) {
            if (uiRequest.isScreenId(null)) {
                throw new NullPointerException("Can not provide null values");
            }

            //TODO or reject request if !this.data.isEmpty()?

        }


        private void defineNavigation(UIRequest uiRequest) {

            if (isScreen_1(uiRequest)) {
                this.screen = ScreenID.SCREEN_2;
                return;
            }

            if (isScreen_2(uiRequest)) {
                this.screen = ScreenID.SCREEN_3;
                return;
            }

            if (isScreen_3(uiRequest)) {
                this.screen = ScreenID.END;
                return;
            }

            throw new RuntimeException("Navigation not defined for " + uiRequest);


        }

        private boolean isScreen_3(UIRequest uiRequest) {
            return uiRequest.isScreenId("3");
        }

        private boolean isScreen_2(UIRequest uiRequest) {
            return uiRequest.isScreenId("2");
        }


        private boolean isLastScreen() {
            return getCurrentScreen().equals(ScreenID.END);
        }

        private boolean isScreen_1(UIRequest uiRequest) {
            return uiRequest.isScreenId("1");
        }


    }

    class WizardUIRequest {
    }
}
