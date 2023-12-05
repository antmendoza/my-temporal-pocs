package io.antmendoza.samples._20231205;

import io.antmendoza.samples._20231006.StageB.StageBRequest;
import io.antmendoza.samples._20231006.StageB.StageBResult;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.failure.CanceledFailure;
import io.temporal.failure.ChildWorkflowFailure;
import io.temporal.workflow.*;
import org.slf4j.Logger;

@WorkflowInterface
public interface WizardUIWorkflow {

  @WorkflowMethod
  void run(WizardUIRequest request);

  @SignalMethod
  void forceMoveToScreen(ScreenID screenID);



  @QueryMethod
  ScreenID getNextScreen();


  @UpdateMethod
  String submitScreen(UIData uiData);


  @UpdateValidatorMethod(updateName="submitScreen")
  void submitScreenValidator(UIData uiData);


  class WizardUIWorkflowImpl implements WizardUIWorkflow {

    private final Logger log = Workflow.getLogger("WizardUIWorkflowImpl");
    private boolean continueNextScreen = false;
    private ScreenID nextScreen = null;


    private static String getWorkflowId() {
      return Workflow.getInfo().getWorkflowId();
    }


    @Override
    public void run(WizardUIRequest request) {



      boolean iterate = true;
      while (iterate) {

        Workflow.await(() -> continueNextScreen);
        this.continueNextScreen = false;
        System.out.println("Unlock...");

        if(isScreen_1()){

          return;
        }


      }
    }

    private boolean isScreen_1() {
      return getNextScreen() == ScreenID.SCREEN_1;
    }

    @Override
    public void forceMoveToScreen(ScreenID screenID) {
        this.nextScreen = screenID;
    }

    @Override
    public ScreenID getNextScreen() {
      return this.nextScreen == null ? ScreenID.SCREEN_1: this.nextScreen;
    }

    @Override
    public String submitScreen(UIData uiData) {
      this.continueNextScreen = true;
      return null;
    }

    @Override
    public void submitScreenValidator(UIData uiData) {

    }


  }

  class WizardUIRequest {}
}
