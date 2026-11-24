# CSC207H1F (Fall 2026) — Clean Architecture, Use Case, View & API Guides (Verbatim)

> Retrieved from UofT Quercus (`https://q.utoronto.ca/courses/444184`) on 2026-11-24.

## Clean Architecture Reflection and Checking Your Project Code

- **Source URL:** https://q.utoronto.ca/courses/444184/pages/clean-architecture-reflection-and-checking-your-project-code

### Verbatim Content

Clean Architecture Reflection and Checking Your Project Code

The Clean Architecture Reflection Quiz will become active by Friday Nov 20 — please note that the deadline has been extended until the end of classes (Tuesday Dec 8) due to our delay in making it available. The quiz will ask you about how you learned and implemented Clean Architecture. The exercise is meant to both help you to personally reflect on your learning and also provide insights which will improve future iterations of CSC207.

Past CSC207 students have been developing  CAVE (CA Visualizer for Education) as part of a CSC394/494 project since last summer. One piece of this project is the CAVE Learn website for learning CA which you may have already used, and another piece is a command-line tool which can be used to check a project's adherence to Clean Architecture.

CAVE documentation can be found here: https://ca-visualizer-for-education.github.io/Clean-Architecture-Visualizer/users/intro/

Below are instructions for installing the command-line tool if you would like to try it out on your own project code:

Download npm.

Open your terminal (this can be done in IntelliJ).

Download CAVE with: npm install -g clean-architecture-visualizer@latest

To do a quick check on your project's adherence to clean architecture, type: cave verify into your terminal (at your project's root).

To open the GUI, type: cave start (this opens a visual summary of the check and also gives access to learning mode)

IMPORTANT: For CAVE to check that your code follows Clean Architecture, you must package your code as follows. Each Use Case must have its own folder*. Below is an example of the required directory structure.

src/

├── main/

│   └── java/

│       ├── app/

│       ├── data_access/

│       ├── entity/

│       ├── interface_adapter/

│       ├── view/

│       └──  use_case/

│           ├── use_case_1/

│      ├── use_case_2/

│      └── .../

└── test/

*CAVE works by producing a dependency graph and detecting suspected violations of the dependency rule; this is done per use case so that it can visually map onto the CA Engine diagram that we have been learning about in CSC207. For this to work, CAVE needs to be able to identify your use cases; this structure helps with that. If your project has other packaging conventions, then the results of CAVE will be affected. CAVE is actively being developed, so you may encounter bugs; please make sure to verify any reported violations — or lack of violations — against your understanding of Clean Architecture.

### Extracted Links on Page

- [Clean Architecture Reflection Quiz](https://q.utoronto.ca/courses/444184/quizzes/519344)
- [CA Visualizer for Education](https://github.com/CA-Visualizer-for-Education/Clean-Architecture-Visualizer)
- [CAVE Learn website](https://ca-visualizer-for-education.github.io/cave-learn/)
- [https://ca-visualizer-for-education.github.io/Clean-Architecture-Visualizer/users/intro/](https://ca-visualizer-for-education.github.io/Clean-Architecture-Visualizer/users/intro/)
- [npm](https://docs.npmjs.com/downloading-and-installing-node-js-and-npm)
- [(this can be done in IntelliJ)](https://www.jetbrains.com/guide/java/tutorials/working-with-the-terminal/opening-the-terminal-window/)

---

## How to Implement a Use Case

- **Source URL:** https://q.utoronto.ca/courses/444184/pages/how-to-implement-a-use-case

### Verbatim Content

How to Implement a Use Case

Writing a use case in Clean Architecture can seem like a lot of work at first because there are many pieces. The good news is that the pieces fit together in a predictable way. Once you have implemented a few use cases, the process becomes much more straightforward.

We recommend treating the following as a checklist whenever your team starts implementing a new use case.

A note about the order of these steps: the order below is a common workflow that many teams find helpful, but it is not the only way to build a use case.

For example, many teams find it useful to start by sketching what the user interface should look like before and after the use case is performed. Thinking about the View early can help clarify what information the user needs to provide and what information should be displayed as a result.

Similarly, you may discover that you need additional entities, DAO methods, or tests after you have already started implementing another part of the use case. Software development is naturally iterative, so expect to move back and forth between these steps as your design evolves.

The important thing is not the exact order in which you complete the steps, but rather that all of the pieces are eventually implemented and connected together correctly.

Prerequisite: Have a completed Use Case template

First, make sure you have written up your use case as we learned in class. Taking the time to document what you are trying to implement will make the following steps easier.

Step 0: UI Sketch

Optionally, you may find it useful to draw a sketch of what the view will look like before and after the use case executes. While the view is of course a detail, this can help make things more concrete as you begin development. At any point, you can then turn the "before view" sketch into code.

Step 1: Identify the Entities You Need

Start by thinking about the information your use case needs to work with.

For example, suppose your application needs a BankAccount entity with an account number and balance. If that entity does not already exist, create it first.

As you work through your use cases, you will often discover additional entities that need to be added to your design.

Step 2: Define the Input Data and Output Data

Next, think about the action the user wants to perform.

What information must be provided to carry out the action? This information belongs in the Input Data class.

For example, if a user wants to check the balance of a bank account, they must provide an account number. The account number would therefore be stored in the Input Data object.

Now think about the result of the action.

What information needs to be returned? How should the user interface change?

If the user is checking an account balance, the balance itself might be returned in the Output Data object. Once you know what information is needed, you can create the Output Data class.

Step 3: Determine What Data Access Is Required

The Interactor typically needs to read, create, update, or delete data. Think about the operations it needs to perform before worrying about implementation details.

Using the bank account example, the Interactor may need a method such as:

BankAccount getBankAccount(String accountNumber)

This operation belongs in your Data Access Interface (DAI).

The Interactor should depend only on the interface, not on a specific DAO implementation.

Step 4: Create a Simple DAO Implementation

At this stage, you do not need persistence.

Create the simplest DAO possible that satisfies the interface. For example, you might store entities in a Map rather than reading from a file or database.

You may even choose to populate the DAO with a small amount of hard-coded test data so that the Interactor can be tested immediately.

Later, your team can replace this with a more sophisticated implementation that supports persistence.

Step 5: Write an Interactor Unit Test

Once the Input Data, Output Data, and Data Access Interface have been planned, write a unit test for the Interactor.

The goal of this test is to clearly specify the Interactor's expected behaviour before you connect it to the UI.

Follow the examples from the signup, login, and logout use cases from lab.

Don't forget to test both successful and failure scenarios.

Step 6: Implement the Interactor

Now create the Input Boundary interface and the Interactor class.

We recommend using execute as the name of the Input Boundary method whenever possible.

In many cases, the Interactor itself will be surprisingly small because most of the preparation has already been done by the Controller, Input Data, DAO, and Entities.

Once your implementation passes all Interactor tests, you have completed the core business logic for the use case.

Step 7: Connect the Use Case to the UI

Now that the Interactor works, it's time to connect it to the rest of the application.

Identify the View that should trigger the use case. Add the necessary button, menu item, or other UI element.

The View gathers information from the user and passes it to the Controller.

The Controller creates the Input Data object and calls the Interactor through the Input Boundary.

At this point, the use case can be executed from the user interface.

Step 8: Implement the Presenter

The Presenter receives Output Data from the Interactor and updates the View Model.

It is also responsible for notifying the appropriate View when updates occur.

This might involve displaying new information, showing an error message, or switching to a different screen.

Step 9: Update the View

Finally, update the View so that it responds correctly when the View Model changes.

This is often the most challenging part of a Clean Architecture implementation, especially the first few times you do it.

If you get stuck, work with your teammates and refer back to the signup, login, and logout examples from lab.

Use Case Implementation Checklist

☐ Complete the Use Case template so that you know what you are implementing.

☐ Sketch the UI before and after the use case is performed.

☐ Create any new Entities needed for the use case.

☐ Define the Input Data class.

☐ Define the Output Data class.

☐ Identify the required data-access operations.

☐ Add methods to the Data Access Interface (DAI).

☐ Create a simple DAO implementation for testing.

☐ Write unit tests for the Interactor.

☐ Implement the Input Boundary and Interactor.

☐ Add tests for failure cases and boundary conditions.

☐ Create or update the Controller.

☐ Connect the View to the Controller.

☐ Implement the Presenter.

☐ Update the View Model.

☐ Update the View to respond to View Model changes.

☐ Verify that the complete use case works end-to-end.

If you're not sure what to do next, find the first unchecked item in this list and focus on that step.

---

## Developing a Use Case: The View

- **Source URL:** https://q.utoronto.ca/courses/444184/pages/developing-a-use-case-the-view

### Verbatim Content

Developing a Use Case: The View

For each use case in your team's MVP, you will need to implement a set of classes similar to the Clean Architecture engine that we have learned about in class.

As we have learned, a typical use case will require:

a View for the user to interact with

including a listener method to handle events triggered by the user

including code to listen for changes to its ViewModel

a Controller for creating the Input Data Object and executing the Use Case Interactor

a Use Case Interactor to implement the logic of the use case

including relevant Data Access Objects and Entities

a Presenter to receive the Output Data Object from the Use Case Interactor

a ViewModel for the Presenter to update

Through our discussion of testing in lecture, we also know that when designing a Use Case it is possible to focus on the "Use Case Interactor" layer of the Clean Architecture. By designing this layer, one can implement the core logic of the Use Case without worrying about the details that exist in the outer layers of the architecture.

Given the short development timeline for our projects though, it is also important to start developing those details sooner rather than later to ensure that your MVP will be ready to demo by the end of the term. With this in mind, the concrete deliverable for this lab is to show a complete sketch of the planned UI elements for your program.

The View(s):

To specify the user-facing behaviour of each use case in your program, your team should sketch before and after drawings of what the view(s) will look like for the main flow, as well as alternative flows.

Focus on how the UI will look for the main flow, but if you have time you can include some interesting alternative flows for each use case as well.

You can sketch your UI using pencil & paper, Figma, Canva, or some other drawing tool. These sketches help you understand:

What information your user interface should display, so that you can start building your Swing UI

How your program should respond to each user interaction, without worrying (yet) about the code that supports it

For example, a use case for logging in might show the following:

Login View Before
Logged In View After
Post Conditions
Login View After

(visible)

User is logged in

(not visible)

--
--

--
--

The first row shows how the login and loggedin views should appear in the main flow: note that while the loggedin view will be the only view visible to the user, we probably want to have cleared the username and password from the login view for when we later log out of the program.

The second row shows an alternative flow where the password doesn't match.

And the third row shows an alternative flow where the username doesn't exist in the system.

Next Steps:

Once you have your view sketched, there are a number of directions development can go in. Your team is encouraged to make a plan for what you will next work on. Your team should document your plan using either GitHub Issues or a similar tool for keeping track of work to be done. For example, your team might create a set of issues, assign them to team members, and update your project's README to reflect the current status of the project development.

Below, we outline some of the natural next steps which your team may want to plan to take for each use case (based on our list above of what goes into a Use Case in the Clean Architecture):

create the initial views using Swing, including ViewModels

implement the relevant entities for the Use Case

design the full "Use Case Interactor" layer for the Use Case

this might take the form of a UML class diagram

design the logic of the Use Case Interactor's execute method

this might take the form of a UML sequence diagram

implement the above designed pieces of the Use Case

unit test the Use Case Interactor — independently of the real Controller, Presenter, and Data Access Object

implement the Controller 

implement the Presenter

implement the Data Access Object (which may require making API calls) — like the View, don't leave this too late, as this detail may require more debugging and reading of documentation!

For a given use case, it could be more natural to start working on different aspect of it, so discuss with your team and make a plan for the order you plan for development. Your team can also refer to the Project Timeline to help get a sense of where you should aim to be at the end of each week. Don't forget, you will also need time to debug and make sure you can connect all use cases together at the end, so make sure to budget time for that in any planning.

We also have another slightly more detailed page describing the steps of creating a use case which you may find useful if still unsure about where to go next with implementing your use case: How to Implement a Use Case.

Please don't hesitate to ask your teammates or someone from the course if you need guidance as you work on your project.

### Extracted Links on Page

- [Project Timeline](https://q.utoronto.ca/courses/444184/pages/project-timeline-2)
- [How to Implement a Use Case](https://q.utoronto.ca/courses/444184/pages/how-to-implement-a-use-case)

---

## Project API Exploration

- **Source URL:** https://q.utoronto.ca/courses/444184/pages/project-api-exploration

### Verbatim Content

Project API Exploration

One of the requirements of the team project is that your team must incorporate an API into your project. You can see the API resources page for some potential APIs and pointers to existing lists of APIs online.

During lab today, your team should continue to think about what domain your team wants to work in — and what user stories you want to develop this term.

The new piece today is that your team should also begin exploring potential APIs that are relevant to your domain that could be incorporated into your project plans.

By the end of lab, it would be great if your team had identified at least a couple of potential APIs — and been able to either make calls to them or have identified documentation that can help you to use the APIs with enough time.

Update your team's README file in your repo to document your progress.

### Extracted Links on Page

- [API resources](https://q.utoronto.ca/courses/444184/pages/api-resources)

---

## API Advice

- **Source URL:** https://q.utoronto.ca/courses/444184/pages/api-advice

### Verbatim Content

API Advice

Below is some advice past students have shared based on their experiences with APIs in their projects.

Some APIs require payment or have usage quotas, while other APIs are free up to a certain usage, but require you to set a quota manually to ensure you don't get charged. Please make sure to check for any restrictions when choosing an API for your project.

Note: some of the information may also be out of date since APIs change over time.

API
Domain
Rating (1–5)
Notes

 

https://www.balldontlie.io/#introduction

 

Sports (Basketball NBA)
5

Lots of tutorials on how to use the API in the documentation, with links to postman to help visualize the data.

Read all the documentation as it shows you what specific calls you can make to the API

https://metmuseum.github.io/

Museum Collection
3

Youtube videos helped with how to use it; images from search requests may load slowly.

https://www.dnd5eapi.co/
Games, D&D

4

Tutorials and documentation provided on website are helpful.

https://deckofcardsapi.com/

Card Games

5

Really clear documentation; general advice to use debugger when developing the code to parse the JSON responses.

openweather api

https://openweathermap.org/api/one-call-3

Weather data 

4,

4

The documentation provided was helpful. The API has some really useful information for weather, definitely enough to use for the project. However, I had an issue with one api call, the reverse geocoding api since it didn't follow the provided documentation.

https://lichess.org/api

 

Chess

4

https://discord.gg/lichess

Beware of rate limits on sending challenges and look into how to handle an asynchronous stream of data, since it's significantly different than basic request-response handling.

https://www.deepl.com/pro-api?cta=menu-pro-api

https://www.deepl.com/en/docs-api

 

 

Translation

5,

4

Their documentation is helped; mostly extrapolated what we learned in the course to this, and it worked. https://developers.deepl.com/docs/getting-started/readme

Great, free API for translations with 500,000 characters a month. Does not charge you extra if you go over. Struggles with certain non ASCII characters, and some symbols.

 

The Movie Database: https://developer.themoviedb.org/docs/getting-started
Movies
4
Reading the documentation helps a lot.

https://github.com/basecamp/bc3-api

 

Productivity
4
--

https://spoonacular.com/food-api

Recipes
4

https://spoonacular.com/food-api/docs

Be careful about low rate limits, especially when testing. Works best if each group member generates their own API key.

https://developer.edamam.com/edamam-docs-recipe-api

 

Recipes
4

Note: this API may have changed to be paid-only, so you will want to check the documentation.

"This is probably one of the easiest API to use, since we only need to create a request object with a url (no need to put API-specific headers or authorizations). My code with Java would look something like this: Request request = new Request.Builder() .url(requestUrl.toString()) .method(""GET"", null) .addHeader(""Content-Type"", ""application/json"") .build(); where e.g. requestUrl = ""https://api.edamam.com/api/recipes/v2?type=public&app_id=<APP_ID>&app_key=<APP_KEY>&q=chicken&diet=balanced&health=alcohol-free&health=dairy-free&cuisineType=American&cuisineType=Chinese&cuisineType=Indian&mealType=Lunch&mealType=Dinner&calories=100-1000&time=0-100"". Replace APP_ID and APP_KEY by creating an account and generate the app id and key."

Ninjas - Nutrition API

https://api-ninjas.com/api/nutrition

Recipes
4
--

TheMealDB: https://www.themealdb.com/
Recipes
4
The documentation is helpful.

USDA Food Data Central API: https://fdc.nal.usda.gov/api-guide
Nutrition and Health
5

The official documentation provided all the API specific information necessary to use the service successfully during the project.  The API spec available at https://app.swaggerhub.com/apis/fdcnal/food-data_central_api/1.0.1 was especially helpful.

I had a positive experience with the API overall. I think it is a great resource for obtaining high quality and very detailed nutrition information on a wide range of foods in a software development context. I do feel having some basic background in nutrition is helpful for being able to parse the large amount of information the API provides.

Another subset of the official documentation I found useful for working with the FDC API was the information on the data types used by the USDA in the API. The general navigation page for this information can be found at https://fdc.nal.usda.gov/data-documentation

https://docs.imagga.com/?java#introduction

Image Processing
4
The API documentation has examples.

https://clipdrop.co/

Image Processing
4
The API documentation is helpful. It looks like you have to contact the company through a form to get an API key.

https://www.file.io/developers

File storage
5
"This is used to save and download files online. We can save any number of files up to 2GB size in total. However, for the free plan, the files saved would be automatically deleted after we download. So our team decided to upload the file again after every download to keep it available in the database. The only methods we need to use are 1. POST to upload the file. The only parameter needed are a string filePath. This file path will be located starting from the src folder. 2. GET to get list of files in the database (with their link) 3. GET{link} to download the files with parameter link (obtained from GET, the previous method)"

Google Firebase (Firestore)
File storage
5

The official documentation is helpful.

The only "hard" part would be connecting the API to the program, which has lots of resources available online.

Firebase Auth and Firestore
Database and Authentication Backend Service
5

The official Firebase documentation is a decent reference for various Firebase services. Otherwise looking at tutorials on youtube is also helpful, and depending on the situation asking generative AI to explain points of confusion (without becoming reliant) is also an option.

Try to build incrementally, incorporating one service into your app at a time. Authentication and Firestore both share a lot of similarities in their setup and usage, so learning one is often helpful for the other.

Foursquare Places API: https://location.foursquare.com/products/places-api/

Maps
5

the documentation was very helpful: https://docs.foursquare.com/developer/reference/place-search?example=java.

The API key is easy enough to access and the provider, at least for the place search endpoint, as they are very generous with the amount of times you can use it without spending any money. Note that not all locations have full information, and be sure to read the documentation carefully to specify what fields you want to be returned, as not all data is returned by default.

Geoapify: https://apidocs.geoapify.com/#docs

Maps
5
Look through the documentation for inspiration, there are a lot of great use cases that can be developed.

Google Maps API
Transit, Maps

5,

5,

4

https://developers.google.com/maps/documentation

Read the documentation thoroughly and look at examples to make sure you know how to use each parameter of the endpoints.

I was not able to use the Google Maps API to display an interactive map and I ended up using the JxMapkit library for that portion of our project. If anyone in the future wants to use an interactive map for their project, I would strongly suggest using JxMapkit to display it as it was easier. The Google Maps API helps a lot in terms of finding information about a location; but you definitely shouldn't use it to generate an interactive map.

The documentation is useful, although there isn't much information on using this API specifically with Java. 

If you want to use this application, be sure implement the interactive map feature in the beginning as that's the bottleneck that was the biggest issue for us. It is possible, but I believe you should be using JavaFX, which, to be set up properly, is easiest done when you first create the project and it can be difficult migrating everything if you have already implemented the code in a regular maven project. Without it, creating an interactive map using only Java Swing likely won't work. If you have already completed majority of your project and want some functionality without migrating your project, I suggest using the static map endpoint as an alternative; it has similar capabilities like mapping out locations and routes (after first generating a polyline from the routes endpoint).

If you use the distance matrix API, do note that you can input all your locations at once to save on calls instead of two locations at a time.

Google Maps Distance Matrix API
Maps
3
Make sure to set quotas in the admin's Google Cloud Dashboard to avoid charges.

Spotify API

https://developer.spotify.com/documentation/web-api

Music

1,

3,

2,

5

The API website has everything. There are some Youtube videos for making Spotify API calls through Postman.

This API requires extra work in: 1) Client ID and client Secret 2) Refreshing access token every 90 minutes (had to write a java auto-refresher) This API has limited functions for java: 1) No access to read/modify private information. This means you can only search up something, but you cannot add them to an existing Spotify account.

The authorization process was difficult because the Spotify api works with web integration. Java is not fit for this and makes for a clunky program. Our group spent ages trying to authorize the user that we spent less time on the actual functionality.

The documentation was enough; Make sure to read the documentation as the process to set up credentials was well documented but we skipped over it so were stuck for a very long time.

Google calendar API
Productivity
1

https://developers.google.com/calendar/api/guides/overview

Even though it has very good documentation, and has very good community behind it, our team kept having troubles with compiling our code due to import issues with other modules. 

https://github.com/sendbird/sendbird-platform-sdk-java

Messaging
4
--

https://v2.jokeapi.dev/

 

Jokes
4
--

https://www.alphavantage.co/documentation/

 

Finance, Trading

3

1

Request the academic access to get more requests per day, instead of suffering through intense API call rationing, and eventually giving in and paying in the last few days of working on the project. Academic access is mentioned on this page: https://www.alphavantage.co/support/#api-key

https://finnhub.io/docs/api
Finance, Trading

4

The free API limit is 60 calls/minute, not the 30 calls/second limit as stated on their documentation. This 60 calls/minute is from the free All-in-one plan you can find on their pricing page. https://finnhub.io/pricing

https://api.random.org/dashboard
random number generation

4

Requires an API key.

### Extracted Links on Page

- [https://www.balldontlie.io/#introduction](https://www.balldontlie.io/#introduction)
- [https://metmuseum.github.io/](https://metmuseum.github.io/)
- [https://www.dnd5eapi.co/](https://www.dnd5eapi.co/)
- [https://deckofcardsapi.com/](https://deckofcardsapi.com/)
- [https://openweathermap.org/api/one-call-3](https://openweathermap.org/api/one-call-3)
- [https://lichess.org/api](https://lichess.org/api)
- [https://discord.gg/lichess](https://discord.gg/lichess)
- [https://www.deepl.com/pro-api?cta=menu-pro-api](https://www.deepl.com/pro-api?cta=menu-pro-api)
- [https://www.deepl.com/en/docs-api](https://www.deepl.com/en/docs-api)
- [https://developers.deepl.com/docs/getting-started/readme](https://developers.deepl.com/docs/getting-started/readme)
- [https://developer.themoviedb.org/docs/getting-started](https://developer.themoviedb.org/docs/getting-started)
- [https://github.com/basecamp/bc3-api](https://github.com/basecamp/bc3-api)
- [https://spoonacular.com/food-api](https://spoonacular.com/food-api)
- [https://spoonacular.com/food-api/docs](https://spoonacular.com/food-api/docs)
- [https://developer.edamam.com/edamam-docs-recipe-api](https://developer.edamam.com/edamam-docs-recipe-api)
- [https://api-ninjas.com/api/nutrition](https://api-ninjas.com/api/nutrition)
- [https://www.themealdb.com/](https://www.themealdb.com/)
- [https://fdc.nal.usda.gov/api-guide](https://fdc.nal.usda.gov/api-guide)
- [https://app.swaggerhub.com/apis/fdcnal/food-data_central_api/1.0.1](https://app.swaggerhub.com/apis/fdcnal/food-data_central_api/1.0.1)
- [https://fdc.nal.usda.gov/data-documentation](https://fdc.nal.usda.gov/data-documentation)
- [https://docs.imagga.com/?java#introduction](https://docs.imagga.com/?java#introduction)
- [https://clipdrop.co/](https://clipdrop.co/)
- [https://www.file.io/developers](https://www.file.io/developers)
- [https://location.foursquare.com/products/places-api/](https://location.foursquare.com/products/places-api/)
- [https://docs.foursquare.com/developer/reference/place-search?example=java](https://docs.foursquare.com/developer/reference/place-search?example=java)
- [https://apidocs.geoapify.com/#docs](https://apidocs.geoapify.com/#docs)
- [https://developers.google.com/maps/documentation](https://developers.google.com/maps/documentation)
- [https://developer.spotify.com/documentation/web-api](https://developer.spotify.com/documentation/web-api)
- [https://developers.google.com/calendar/api/guides/overview](https://developers.google.com/calendar/api/guides/overview)
- [https://github.com/sendbird/sendbird-platform-sdk-java](https://github.com/sendbird/sendbird-platform-sdk-java)
- [https://v2.jokeapi.dev/](https://v2.jokeapi.dev/)
- [https://www.alphavantage.co/documentation/](https://www.alphavantage.co/documentation/)
- [https://www.alphavantage.co/support/#api-key](https://www.alphavantage.co/support/#api-key)
- [https://finnhub.io/docs/api](https://finnhub.io/docs/api)
- [https://finnhub.io/pricing](https://finnhub.io/pricing)
- [https://api.random.org/dashboard](https://api.random.org/dashboard)

---

## API resources

- **Source URL:** https://q.utoronto.ca/courses/444184/pages/api-resources

### Verbatim Content

API resources

The below isn't exhaustive, but should help your team get started as you look for APIs to use in your project this term.

Some tools for exploring APIs

https://hoppscotch.io/ (no account required)

https://www.postman.com/ (requires an account)

Some APIs provide pre-populated workspaces on platforms like Postman to help you get started exploring their APIs.

For example: https://developer.twitter.com/en/docs/tutorials/postman-getting-started

Some Lists of APIs

https://github.com/public-apis/public-apis

https://apilayer.com/

https://apilist.fun/

Note: these lists are often out-of-date, so you will want to try out the APIs to see if they are still actively maintained and operational.

The course notes also have a few APIs linked at:

https://github.com/CSC207-UofT/207-course-notes/blob/main/apis-json-and-files.md#further-reading

Some other APIs:

Past students have shared their experiences working with several APIs. You can find their feedback on various APIs in API Advice.

Dog pictures: https://dog.ceo/dog-api/

Trivia: https://opentdb.com/api_config.php

Meal Database: https://www.themealdb.com/api.php

Meal Planning: https://developer.edamam.com/meal-planner-api

Random Quotes: https://zenquotes.io/api/random

Countries: https://restcountries.com/

NASA: https://api.nasa.gov/

Pokémon: https://pokeapi.co/

Wikimedia https://api.wikimedia.org/wiki/API_catalog (Getting started with Wikimedia APIs)

Reddit: https://www.reddit.com/dev/api/

Google: https://developers.google.com/apis-explorer

Microsoft https://learn.microsoft.com/en-ca/rest/api/

Figma https://www.figma.com/developers/api

Hugging Face: https://huggingface.co/docs/dataset-viewer/quick_start

Speech recognition: https://docs.speechace.com/

Translation: https://libretranslate.com/

Recipes & Nutrition: https://developer.edamam.com/edamam-docs-recipe-api (note: this API appears to be a paid service now)

Gen AI chat: https://docs.cohere.com/v2/docs/chat-api (cohere is co-founded by Nick Frosst — a former UofT student)

Connections games: A past ESC499 student developed a Connections game (you can play a sample game at https://vm006.teach.cs.toronto.edu/play/XBZQ/) for a CS Education project and has kindly shared his API which you can use to get json-formatted descriptions of Connections games: https://vm006.teach.cs.toronto.edu/backend/api/connectionsgames/

Data storage: If you want to store persistent data in a database but don't have the time to learn about how to set up a database, you can make use of the password-protected user feature of the "Grade API" from lab. We'll post a sample program later, but you can also read about how to use the API on the Postman documentation page.

You can store arbitrary JSON objects and retrieve them later. This can be used to provide persistence for a variety of applications from the Project Domains page.

If you find an API that seems interesting — or any other useful resources for exploring APIs — please share.

### Extracted Links on Page

- [https://hoppscotch.io/](https://hoppscotch.io/)
- [https://www.postman.com/](https://www.postman.com/)
- [https://developer.twitter.com/en/docs/tutorials/postman-getting-started](https://developer.twitter.com/en/docs/tutorials/postman-getting-started)
- [https://github.com/public-apis/public-apis](https://github.com/public-apis/public-apis)
- [https://apilayer.com/](https://apilayer.com/)
- [https://apilist.fun/](https://apilist.fun/)
- [https://github.com/CSC207-UofT/207-course-notes/blob/main/apis-json-and-files.md#further-reading](https://github.com/CSC207-UofT/207-course-notes/blob/main/apis-json-and-files.md#further-reading)
- [API Advice](https://q.utoronto.ca/courses/444184/pages/api-advice)
- [https://dog.ceo/dog-api/](https://dog.ceo/dog-api/)
- [https://opentdb.com/api_config.php](https://opentdb.com/api_config.php)
- [https://www.themealdb.com/api.php](https://www.themealdb.com/api.php)
- [https://developer.edamam.com/meal-planner-api](https://developer.edamam.com/meal-planner-api)
- [https://zenquotes.io/api/random](https://zenquotes.io/api/random)
- [https://restcountries.com/](https://restcountries.com/)
- [https://api.nasa.gov/](https://api.nasa.gov/)
- [https://pokeapi.co/](https://pokeapi.co/)
- [https://api.wikimedia.org/wiki/API_catalog](https://api.wikimedia.org/wiki/API_catalog)
- [Getting started with Wikimedia APIs](https://api.wikimedia.org/wiki/Getting_started_with_Wikimedia_APIs#:~:text=Wikipedia%20and%20other%20Wikimedia%20projects,to%20your%20projects%20and%20apps.)
- [https://www.reddit.com/dev/api/](https://www.reddit.com/dev/api/)
- [https://developers.google.com/apis-explorer](https://developers.google.com/apis-explorer)
- [https://learn.microsoft.com/en-ca/rest/api/](https://learn.microsoft.com/en-ca/rest/api/)
- [https://www.figma.com/developers/api](https://www.figma.com/developers/api)
- [https://huggingface.co/docs/dataset-viewer/quick_start](https://huggingface.co/docs/dataset-viewer/quick_start)
- [https://docs.speechace.com/](https://docs.speechace.com/)
- [https://libretranslate.com/](https://libretranslate.com/)
- [https://developer.edamam.com/edamam-docs-recipe-api](https://developer.edamam.com/edamam-docs-recipe-api)
- [https://docs.cohere.com/v2/docs/chat-api](https://docs.cohere.com/v2/docs/chat-api)
- [https://vm006.teach.cs.toronto.edu/play/XBZQ/](https://vm006.teach.cs.toronto.edu/play/XBZQ/)
- [https://vm006.teach.cs.toronto.edu/backend/api/connectionsgames/](https://vm006.teach.cs.toronto.edu/backend/api/connectionsgames/)
- [how to use the API on the Postman documentation page](https://www.postman.com/cloudy-astronaut-813156/csc207-apis-2026/collection/puzltp3/5-password-protected-user)
- [Project Domains](https://q.utoronto.ca/courses/444184/pages/project-domains-and-sample-user-stories)

---

