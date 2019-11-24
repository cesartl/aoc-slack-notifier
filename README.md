# Advent of Code Slack Notifier

## What?

This project is aimed at those participating in https://adventofcode.com/ and who are member of one ore
more private leaderboards.

This application will poll the AOC API, store the data in a DynamoDB and notify a Slack channel of your choice
if there has been a change in the leaderboard. 


## TODO

* write more tests
* compute hash of AOC response and don't save new dynamo DB event if there was no change
* handle multiple (year,leaderboardId) pairs
* Ability to customise Slack message with env variable
* Use block kit to write slack message
* Could add more complex logic in Slack by getting info from the
* Process dynamoDB data to draw interesting graph of ranks/stars in time 