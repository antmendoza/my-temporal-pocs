# Use case

We want to monitor the migration of 60 gitlab projects from the owner teams
The migration is completed when the repository is marked as archived

We want to check every project, every hour on weekdays for it's archival status (Gitlab API).
If the project is archived then send a slack message to our operations channel and consider it migrated.
Every week-day send a slack message with the number of projects that are still active.
