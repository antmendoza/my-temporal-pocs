# Use case

We want to monitor the migration of 60 gitlab projects from the owner teams
The migration is completed when the repository is marked as archived

We want to check every project, every hour on weekdays for it's archival status (Gitlab API).
If the project is archived then send a slack message to our operations channel and consider it migrated.
Every week-day send a slack message with the number of projects that are still active.


---

My two cents
have a parent workflow that spawns x child workflows in parallel (in abandon mode) one for each gitlab project.
- each child workflow
  - has an activity that query the state of the gitlab repository, we have an example here for infrequent polling https://github.com/temporalio/samples-java/tree/main/core/src/main/java/io/temporal/samples/polling/infrequent
  - followed by another activity to sent the slack notification (project archived)
  - before completing the child workflow signal back to the parent , where you can have a counter with the number of active childworkflows
  
- in the parent workflow:
  - have an activity that sent the slack message with the number of active projects
  - if the number of active child workflows (counter) is > 0,
  - sleep until the next weekday
  - then “continueAsNew”