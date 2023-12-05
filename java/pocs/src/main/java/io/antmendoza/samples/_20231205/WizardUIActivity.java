package io.antmendoza.samples._20231205;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface WizardUIActivity {

    void activity1_1();

    void activity1_2();

    void activity2_1();

    void activity3_1();

    void activity3_2();


    class WizardUIActivityImpl implements WizardUIActivity {

        @Override
        public void activity1_1() {
            //Do nothing
        }

        @Override
        public void activity1_2() {
            //Do nothing
        }

        @Override
        public void activity2_1() {
            //Do nothing
        }

        @Override
        public void activity3_1() {
            //Do nothing
        }

        @Override
        public void activity3_2() {
            //Do nothing
        }
    }
}
