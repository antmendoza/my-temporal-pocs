<?php
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: temporal/api/workflowservice/v1/request_response.proto

namespace Temporal\Api\Workflowservice\V1;

use Google\Protobuf\Internal\GPBType;
use Google\Protobuf\Internal\RepeatedField;
use Google\Protobuf\Internal\GPBUtil;

/**
 * Generated from protobuf message <code>temporal.api.workflowservice.v1.PollWorkflowExecutionUpdateResponse</code>
 */
class PollWorkflowExecutionUpdateResponse extends \Google\Protobuf\Internal\Message
{
    /**
     * The outcome of the update if and only if the update has completed. If
     * this response is being returned before the update has completed (e.g. due
     * to the specification of a wait policy that only waits on
     * UPDATE_WORKFLOW_EXECUTION_LIFECYCLE_STAGE_ACCEPTED) then this field will
     * not be set.
     *
     * Generated from protobuf field <code>.temporal.api.update.v1.Outcome outcome = 1;</code>
     */
    protected $outcome = null;
    /**
     * The most advanced lifecycle stage that the Update is known to have
     * reached, where lifecycle stages are ordered
     * UPDATE_WORKFLOW_EXECUTION_LIFECYCLE_STAGE_UNSPECIFIED <
     * UPDATE_WORKFLOW_EXECUTION_LIFECYCLE_STAGE_ADMITTED <
     * UPDATE_WORKFLOW_EXECUTION_LIFECYCLE_STAGE_ACCEPTED <
     * UPDATE_WORKFLOW_EXECUTION_LIFECYCLE_STAGE_COMPLETED.
     * UNSPECIFIED will be returned if and only if the server's maximum wait
     * time was reached before the Update reached the stage specified in the
     * request WaitPolicy, and before the context deadline expired; clients may
     * may then retry the call as needed.
     *
     * Generated from protobuf field <code>.temporal.api.enums.v1.UpdateWorkflowExecutionLifecycleStage stage = 2;</code>
     */
    protected $stage = 0;
    /**
     * Sufficient information to address this update.
     *
     * Generated from protobuf field <code>.temporal.api.update.v1.UpdateRef update_ref = 3;</code>
     */
    protected $update_ref = null;

    /**
     * Constructor.
     *
     * @param array $data {
     *     Optional. Data for populating the Message object.
     *
     *     @type \Temporal\Api\Update\V1\Outcome $outcome
     *           The outcome of the update if and only if the update has completed. If
     *           this response is being returned before the update has completed (e.g. due
     *           to the specification of a wait policy that only waits on
     *           UPDATE_WORKFLOW_EXECUTION_LIFECYCLE_STAGE_ACCEPTED) then this field will
     *           not be set.
     *     @type int $stage
     *           The most advanced lifecycle stage that the Update is known to have
     *           reached, where lifecycle stages are ordered
     *           UPDATE_WORKFLOW_EXECUTION_LIFECYCLE_STAGE_UNSPECIFIED <
     *           UPDATE_WORKFLOW_EXECUTION_LIFECYCLE_STAGE_ADMITTED <
     *           UPDATE_WORKFLOW_EXECUTION_LIFECYCLE_STAGE_ACCEPTED <
     *           UPDATE_WORKFLOW_EXECUTION_LIFECYCLE_STAGE_COMPLETED.
     *           UNSPECIFIED will be returned if and only if the server's maximum wait
     *           time was reached before the Update reached the stage specified in the
     *           request WaitPolicy, and before the context deadline expired; clients may
     *           may then retry the call as needed.
     *     @type \Temporal\Api\Update\V1\UpdateRef $update_ref
     *           Sufficient information to address this update.
     * }
     */
    public function __construct($data = NULL) {
        \GPBMetadata\Temporal\Api\Workflowservice\V1\RequestResponse::initOnce();
        parent::__construct($data);
    }

    /**
     * The outcome of the update if and only if the update has completed. If
     * this response is being returned before the update has completed (e.g. due
     * to the specification of a wait policy that only waits on
     * UPDATE_WORKFLOW_EXECUTION_LIFECYCLE_STAGE_ACCEPTED) then this field will
     * not be set.
     *
     * Generated from protobuf field <code>.temporal.api.update.v1.Outcome outcome = 1;</code>
     * @return \Temporal\Api\Update\V1\Outcome|null
     */
    public function getOutcome()
    {
        return $this->outcome;
    }

    public function hasOutcome()
    {
        return isset($this->outcome);
    }

    public function clearOutcome()
    {
        unset($this->outcome);
    }

    /**
     * The outcome of the update if and only if the update has completed. If
     * this response is being returned before the update has completed (e.g. due
     * to the specification of a wait policy that only waits on
     * UPDATE_WORKFLOW_EXECUTION_LIFECYCLE_STAGE_ACCEPTED) then this field will
     * not be set.
     *
     * Generated from protobuf field <code>.temporal.api.update.v1.Outcome outcome = 1;</code>
     * @param \Temporal\Api\Update\V1\Outcome $var
     * @return $this
     */
    public function setOutcome($var)
    {
        GPBUtil::checkMessage($var, \Temporal\Api\Update\V1\Outcome::class);
        $this->outcome = $var;

        return $this;
    }

    /**
     * The most advanced lifecycle stage that the Update is known to have
     * reached, where lifecycle stages are ordered
     * UPDATE_WORKFLOW_EXECUTION_LIFECYCLE_STAGE_UNSPECIFIED <
     * UPDATE_WORKFLOW_EXECUTION_LIFECYCLE_STAGE_ADMITTED <
     * UPDATE_WORKFLOW_EXECUTION_LIFECYCLE_STAGE_ACCEPTED <
     * UPDATE_WORKFLOW_EXECUTION_LIFECYCLE_STAGE_COMPLETED.
     * UNSPECIFIED will be returned if and only if the server's maximum wait
     * time was reached before the Update reached the stage specified in the
     * request WaitPolicy, and before the context deadline expired; clients may
     * may then retry the call as needed.
     *
     * Generated from protobuf field <code>.temporal.api.enums.v1.UpdateWorkflowExecutionLifecycleStage stage = 2;</code>
     * @return int
     */
    public function getStage()
    {
        return $this->stage;
    }

    /**
     * The most advanced lifecycle stage that the Update is known to have
     * reached, where lifecycle stages are ordered
     * UPDATE_WORKFLOW_EXECUTION_LIFECYCLE_STAGE_UNSPECIFIED <
     * UPDATE_WORKFLOW_EXECUTION_LIFECYCLE_STAGE_ADMITTED <
     * UPDATE_WORKFLOW_EXECUTION_LIFECYCLE_STAGE_ACCEPTED <
     * UPDATE_WORKFLOW_EXECUTION_LIFECYCLE_STAGE_COMPLETED.
     * UNSPECIFIED will be returned if and only if the server's maximum wait
     * time was reached before the Update reached the stage specified in the
     * request WaitPolicy, and before the context deadline expired; clients may
     * may then retry the call as needed.
     *
     * Generated from protobuf field <code>.temporal.api.enums.v1.UpdateWorkflowExecutionLifecycleStage stage = 2;</code>
     * @param int $var
     * @return $this
     */
    public function setStage($var)
    {
        GPBUtil::checkEnum($var, \Temporal\Api\Enums\V1\UpdateWorkflowExecutionLifecycleStage::class);
        $this->stage = $var;

        return $this;
    }

    /**
     * Sufficient information to address this update.
     *
     * Generated from protobuf field <code>.temporal.api.update.v1.UpdateRef update_ref = 3;</code>
     * @return \Temporal\Api\Update\V1\UpdateRef|null
     */
    public function getUpdateRef()
    {
        return $this->update_ref;
    }

    public function hasUpdateRef()
    {
        return isset($this->update_ref);
    }

    public function clearUpdateRef()
    {
        unset($this->update_ref);
    }

    /**
     * Sufficient information to address this update.
     *
     * Generated from protobuf field <code>.temporal.api.update.v1.UpdateRef update_ref = 3;</code>
     * @param \Temporal\Api\Update\V1\UpdateRef $var
     * @return $this
     */
    public function setUpdateRef($var)
    {
        GPBUtil::checkMessage($var, \Temporal\Api\Update\V1\UpdateRef::class);
        $this->update_ref = $var;

        return $this;
    }

}
