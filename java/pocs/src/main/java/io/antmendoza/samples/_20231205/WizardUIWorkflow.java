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
    String forceNavigateToScreen(ScreenID screenID);


    @QueryMethod
    ScreenID getCurrentScreen();


    @UpdateMethod
    String submitScreen(UIRequest uiRequest);


    @UpdateValidatorMethod(updateName = "submitScreen")
    void submitScreenValidator(UIRequest uiRequest);


    class WizardUIWorkflowImplBufferRequests implements WizardUIWorkflow {

        //For production development consider setting different activity options for each activity
        private final Logger log = Workflow.getLogger("WizardUIWorkflowImpl");
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

                //TODO watch eventHistoryLength and CAN if > 1000

                // block the main workflow thread until !data.isEmpty()
                // note that we are populating the array in submitScreen, once the user has submitted the data coming from the UI
                Workflow.await(() -> !data.isEmpty()); //#1#

                //We process the data one by one
                UIRequest uiRequest = data.get(0);

                //this is business specific, depending on what each screen has to submit/persist
                if (isScreen_3(uiRequest)) {
                    activity.activity3_1();
                    activity.activity3_2();

                    //and base on some "rules" we decide what is the next page
                    this.screen = this.defineNavigation(uiRequest);
                }


                if (isScreen_2(uiRequest)) {
                    activity.activity2_1();
                    this.screen = this.defineNavigation(uiRequest);
                }

                if (isScreen_1(uiRequest)) {
                    activity.activity1_1();
                    activity.activity1_2();
                    this.screen = this.defineNavigation(uiRequest);
                }

                data.remove(uiRequest);// #2#

            }
        }

        @Override
        //We use update workflow feature to set the value of the page we want to navigate to, so it is fully persisted
        // in the workflow
        public String forceNavigateToScreen(ScreenID screenID) {
            this.screen = screenID;
            return getCurrentScreen().toString();
        }

        @Override
        public ScreenID getCurrentScreen() {
            //This is just a query method that return the current page the customer is/has to navigate to
            return this.screen == null ? ScreenID.SCREEN_1 : this.screen;
        }

        @Override
        public String submitScreen(UIRequest uiRequest) {

            //this method uses update workflow feature to send the data and have the client (making the call) waiting
            // until this method completes

            //This method will block any incoming data/request until we have processed the previous data submitted
            // to prevent race condition. Basically we are buffering the requests and processing them one by one

            //Another implementation would be to have logic in submitScreenValidator to validate and reject the
            // request if we are processing a previous request.
            Workflow.await(() -> this.data.isEmpty());


            //we add the request to the array, that will unblock the condition we have in the main thread (search in this
            // code for #1#)
            this.data.add(uiRequest);


            //we block this thread until the element added to the array ^^ has been processed (we remove the
            // element from the array once it is processed, search in this code for #2#)

            //This will unblock incoming request as well, any request awaiting in the first line of this method

            Workflow.await(() -> !this.data.contains(uiRequest));

            return getCurrentScreen().toString();

        }

        @Override
        public void submitScreenValidator(UIRequest uiRequest) {
            if (uiRequest.isScreenId(null)) {
                throw new NullPointerException("Can not provide null values");
            }

            //TODO or reject request if !this.data.isEmpty()

        }


        private ScreenID defineNavigation(UIRequest uiRequest) {


            if (isScreen_1(uiRequest)) {
                //TODO are more logic based on uiRequest data, and maybe return a different value
                return ScreenID.SCREEN_2;
            }

            if (isScreen_2(uiRequest)) {
                //TODO are more logic based on uiRequest data, and maybe return a different value
                return ScreenID.SCREEN_3;
            }

            if (isScreen_3(uiRequest)) {
                //TODO are more logic based on uiRequest data, and maybe return a different value
                return ScreenID.END;
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
