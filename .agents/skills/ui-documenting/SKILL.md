---
name: UI Documentation
description: Instructions for exploring and documenting the UI of the app
---

# UI Documentation

perform a thorough exploration of the app  using the phone MCP server to access all its functionalities and features. Then, create a product specification document for the app. The document should be a .md file in a separate folder that includes screenshots and requirements for each functionality this app offers. When there product requirements are not clear from using the app, you may look into the code to extract it. It is important that the document does not include any code. Requirements should be written at the product-level. Any gen-AI related feature should be out of scope for this documentation so remove any that you added previously and ignore the "suggestions" and "trip preferences" for now.

In order to successfully document the app you MUST:
- Create a trip
- name the trip
- Add one of the following for at least 2 different cities:
  - flight to the city from the previous one, or the origin
  - hotel booking
  - restaurant reservation
  - place visit 
- Add a return flight.

### some things to keep in mind:
* Do not try to debug if you run into issues. You are analyzing the app, not debugging it. All network requests work
* Auto complete fields require you to wait for results to load before you interact with then. Don't try to batch input actions in a single command.
* The MCP server doesn't provide accurate descriptions of the screen. Use `adb exec-out uiautomator dump /dev/tty` to get an accurate description of the screen. The command will print the XML content to stdout.
* When looking for tappable elements, always try parent elements in case the the element found is not tappable.
* Always check if text fields are empty before typing in them. 
* DO NOT attempt to create a flexible section (the compass icon) as it might cause the app to crash.

Verify if the actions succeeded before moving on to the next. For example, don't start adding plans before you have confirmed you have successfully changed the trip name. Don't move on to add a new plan before you've confirmed the one you just attempted to add has appeared in the itinerary

This document will be used by engineers on other platforms to port this app, as well as by other cross-functional stakeholders to understand it.