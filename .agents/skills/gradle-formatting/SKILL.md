---
name: Gradle Formatting
description: Instructions for running Gradle and Spotless formatting in the project
---
# Gradle Formatting Instructions

When working on this project, adhere to the following rules:

- **Always** run `./gradlew` using the existing daemon. Do NOT use the `--no-daemon` argument.
- **Always** run code formatting after making any changes to the project files by executing `./gradlew spotlessApply`.
