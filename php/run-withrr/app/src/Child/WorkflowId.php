<?php

/**
 * This file is part of Temporal package.
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

declare(strict_types=1);

namespace Temporal\Samples\Child;

use Temporal\Internal\Assert;
use Temporal\Workflow;

final readonly class WorkflowId
{
    /**
     * @param string $workflowId This is public to allow the Temporal SDK to marshal/unmarshal the object.
     */
    private function __construct(public string $workflowId)
    {
//        Assert::notEmpty($workflowId, 'Workflow ID cannot be an empty string.');
    }

    public static function fromString(string $workflowId): self
    {
        return new self($workflowId);
    }

    public static function fromTypeAndOrganizationToken(
        string $organizationToken,
    ): self
    {
        return new self("child-of-" . $organizationToken);
    }

    public function toString(): string
    {
        return $this->workflowId;
    }
}