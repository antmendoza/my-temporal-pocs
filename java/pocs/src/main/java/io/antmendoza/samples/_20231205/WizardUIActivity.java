package io.antmendoza.samples._20231205;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface WizardUIActivity {

    void activity1_1();

    void activity1_2();

    void activity2_1();

    void activity3_1();

    void activity3_2();

    void sendNotification();


    class WizardUIActivityImpl implements WizardUIActivity {

        @Override
        public void activity1_1() {
            randomSleep();
            //Do nothing
        }

        private static void randomSleep() {
            try {
                int millis = (int) (Math.random() * 1000);
                //System.out.println("Sleeping " +  millis);
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void activity1_2() {
            randomSleep();
            //Do nothing
        }

        @Override
        public void activity2_1() {
            randomSleep();
            //Do nothing
        }

        @Override
        public void activity3_1() {
            randomSleep();
            //Do nothing
        }

        @Override
        public void activity3_2() {
            randomSleep();
            //Do nothing
        }

        @Override
        public void sendNotification() {
            randomSleep();
            //Do nothing
        }
    }
}
