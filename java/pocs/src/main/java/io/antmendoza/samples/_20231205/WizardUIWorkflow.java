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
    void run(WizardUIRequest request);

    @SignalMethod
    void forceMoveToScreen(ScreenID screenID);


    @QueryMethod
    ScreenID getCurrentScreen();


    @UpdateMethod
    String submitScreen(UIData uiData);


    @UpdateValidatorMethod(updateName = "submitScreen")
    void submitScreenValidator(UIData uiData);


    class WizardUIWorkflowImpl implements WizardUIWorkflow {

        //For production development consider setting different activity options for each activity
        private final Logger log = Workflow.getLogger("WizardUIWorkflowImpl");
        private final WizardUIActivity activity =
                Workflow.newActivityStub(WizardUIActivity.class,
                        ActivityOptions.newBuilder()
                                .setStartToCloseTimeout(Duration.ofSeconds(2))
                                .build());

        public List<UIData> data = new ArrayList<>();
        private ScreenID screen = null;

        @Override
        public void run(WizardUIRequest request) {

            while (!data.isEmpty() || !isLastScreen()) {

                //TODO watch eventHistoryLength and CAN if > 1000

                Workflow.await(() -> !data.isEmpty());

                UIData uiData = data.get(0);

                if (isScreen_3()) {
                    activity.activity3_1();
                    activity.activity3_2();
                    this.screen = ScreenID.END;
                }


                if (isScreen_2()) {
                    activity.activity2_1();
                    this.screen = ScreenID.SCREEN_3;
                }

                if (isScreen_1()) {
                    activity.activity1_1();
                    activity.activity1_2();
                    this.screen = ScreenID.SCREEN_2;
                }

                data.remove(uiData);

                if (isLastScreen()) {
                    return;
                }

            }
        }

        private boolean isScreen_3() {
            return getCurrentScreen() == ScreenID.SCREEN_3;
        }

        private boolean isScreen_2() {
            return getCurrentScreen() == ScreenID.SCREEN_2;
        }


        private boolean isLastScreen() {
            return getCurrentScreen().equals(ScreenID.END);
        }

        private boolean isScreen_1() {
            return getCurrentScreen() == ScreenID.SCREEN_1;
        }

        @Override
        public void forceMoveToScreen(ScreenID screenID) {
            this.screen = screenID;
        }

        @Override
        public ScreenID getCurrentScreen() {
            return this.screen == null ? ScreenID.SCREEN_1 : this.screen;
        }

        @Override
        public String submitScreen(UIData uiData) {
            Workflow.await(() -> this.data.isEmpty());
            this.data.add(uiData);

            Workflow.await(() -> !this.data.contains(uiData));
            return getCurrentScreen().toString();
        }

        @Override
        public void submitScreenValidator(UIData uiData) {
            if(uiData.equals(new UIData(null))){
                throw new NullPointerException("Can not provide null values");
            }

            //TODO or reject request if !this.data.isEmpty()

        }


    }

    class WizardUIRequest {
    }
}
