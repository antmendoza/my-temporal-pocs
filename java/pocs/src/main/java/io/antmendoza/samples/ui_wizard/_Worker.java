package io.antmendoza.samples.ui_wizard;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

import static io.antmendoza.samples.ui_wizard._Client.TASK_QUEUE;


public class _Worker {


    public static void main(String[] args) {

        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

        WorkflowClient client = WorkflowClient.newInstance(service);

        WorkerFactory factory = WorkerFactory.newInstance(client);

        Worker worker = factory.newWorker(TASK_QUEUE);

        worker.registerWorkflowImplementationTypes(WizardUIWorkflow.WizardUIWorkflowImplBufferRequests.class);

        worker.registerActivitiesImplementations(new WizardUIActivity.WizardUIActivityImpl());

        factory.start();

    }

}