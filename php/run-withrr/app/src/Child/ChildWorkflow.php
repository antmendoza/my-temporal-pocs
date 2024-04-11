<?php

/**
 * This file is part of Temporal package.
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

declare(strict_types=1);

namespace Temporal\Samples\Child;

use Carbon\CarbonInterval;
use Psr\Log\LoggerInterface;
use Temporal\SampleUtils\Logger;
use Temporal\Workflow;
use Temporal\Workflow\WorkflowInterface;
use Temporal\Workflow\WorkflowMethod;

#[WorkflowInterface]
class ChildWorkflow
{
    private LoggerInterface $logger;

    #[WorkflowMethod("Child.greet")]
    public function greet(
        string $name
    ) {

        $this->logger = new Logger();

        $parentId = Workflow::getInfo()->parentExecution->getID();

        $this->logger->info(sprintf('Comparing  %s : %s', $name, $parentId));

        if($parentId != $name){
            throw new \Error($parentId . ' ' . $name . '!');
        }

        return 'Hello ' . $name . ' from child workflow!';
    }
}