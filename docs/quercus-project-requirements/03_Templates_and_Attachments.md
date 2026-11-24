# CSC207H1F (Fall 2026) — Project Templates & Attachments (Verbatim)

> Retrieved from UofT Quercus (`https://q.utoronto.ca/courses/444184`) on 2026-11-24.

---

## 1. Official Project README Template (`project-readme-template-2026.md`, File ID `45644698`)

- **Quercus File URL:** https://q.utoronto.ca/courses/444184/files/45644698?wrap=1
- **Standalone Local Copy:** [project-readme-template-2026.md](./project-readme-template-2026.md)

````markdown
# Project Name
## Team Information
### Team Members
- Name
- Name
- Name
- Name
- Name
- Name
---
## Project Description
### Domain
[Project description]
---
## User Stories
### User Story 1
### User Story 2
### User Story 3
(Add additional user stories as needed.)
---
## Use Cases
### Use Case 1
### Use Case 2
### Use Case 3
(Add additional use cases as needed.)
---
## Minimal Viable Product (MVP)
| Team Member | MVP Use Case |
|-------------|--------------|
| Name | Use Case |
| Name | Use Case |
| Name | Use Case |
---
## Entities
### Entity 1
### Entity 2
(Add additional entities as needed.)
---
## API Usage
**API Name:**
**Documentation:** [Link]
**How it is used in the program:**
---
## Design Diagrams
### UML Class Diagram
### Sequence Diagrams
---
## How to Run
[Instructions for running the program.]
---
## Notes
[Any additional project information.]
````

---

## 2. Final Project Presentation Template (`Presentation Template 2026.pptx`, File ID `46149397`, 16 Slides)

- **Quercus File URL:** https://q.utoronto.ca/courses/444184/files/46149397?wrap=1

### Verbatim Slide Text (Slides 1–16)

````text
Page
 of 16
CSC207 Final Project
Presentation Template
Fall 2026
Group Number ###
Reminder 1: Make sure to rehearse and time your presentation to ensure you can get through it within
the allotted time!
Reminder 2: Your presentation needs to provide convincing evidence that you have satisfied the rubric elements.
Reminder 3: Every member of the team must speak during the presentation.
On each slide,
please indicate
which group
member will be
presenting.
User Stories (i.e., what functionality is in your
team's MVP?)
• Explain what functionality your program provides.
• You can use whatever format you want — just make sure to clearly
and concisely convey this information.
• Advice: avoid too many words here and don’t plan to read a full paragraph of
text on the slide when presenting — just highlight the key points.
• Focus on the WHAT and not the HOW!
• Recommended time limit: [1 minute]
API usage
• What API(s) did your team use?
• Make sure to show appropriate information (briefly).
• Recommended time limit: [30 seconds]
Data Persistence
• What data is persistent in your program?
• Either here or in one of the use case demonstrations of functionality,
data persistence should be demonstrated.
• Recommended time limit: [30 seconds]
Design
• How does your program adhere to SOLID?
• Should aim to talk about two specific examples present in your project; touching on at least
two principles.
• How does your program adhere to the Clean Architecture?
• This can be very short, or most likely skipped entirely, depending on what each member
talked about in the previous use case part.
• What is the best example of a design pattern used in your program?
• Aim to talk about one design pattern that your team introduced — don't talk about a pattern
that was already implemented in the starter code to earn full marks here.
• As appropriate, your team should make use of diagrams and other visuals to
convey this information.
• Almost always, diagrams will be more effective than showing the details of the
code!
• Roughly, your team might aim for 2–6 slides in total about design, but the exact
number can vary depending on how you are presenting the information.
• Recommended time limit: [6 minutes]
Use case walkthrough [recommended for one use
case that helps demonstrate the project’s CA
adherence]
This is one recommended way to spend some of your team’s time talking
about design.
• Briefly state a user story and which associated use case you will focus on
• Show the before and after views for when the use case executes
• Show a UML class diagram for the use case; it should be clear from the
diagram that your code adheres to CA!
• Show the code for the Use Case Interactor class.
• Discuss the flow of control when the use case executes.
• [this should be rehearsed so that it is around 1-2 minutes to talk through a
use case (contributes to the recommended 5 minutes from the previous
slide]
• [see slides at the end for an example of how this might look]
Example of a use case walkthrough
Functionality Demonstration
• Make sure to time things out so that your team can demonstrate at least
the core functionality of your MVP.
• Feel free to demo live or show recordings, but you should have a working
version of the program available to demo specific functionality live
immediately after the presentation if there are questions.
• Recommended time limit: [6 minutes]
• Make sure to especially rehearse the demo, as it can be easy to spend too much time
here!
• Focus on the most interesting parts of the program (e.g., don't spend time on things
like signing up a user, changing password, or similar unless it is central to the
program)
• NOTE: depending on how your team decides to present, it might be
appropriate to wait until the very end to do your demo.
Code Quality
• Briefly explain how your team was able to maintain code quality.
o You might discuss how you used Checkstyle or similar tools.
o Reminder: If your team is looking for a high mark here, you need to provide clear
evidence of your use of such a tool.
o You might discuss your approach to pull requests and code reviews.
 For the exceptional level, reminder that your team may want to explicitly draw attention
to a representative pull request demonstrating your team's approach to code quality.
• Recommended time limit: [1 minute]
The End
• Time permitting, your team can highlight anything else about the
project that you want to share.
• You’ll likely fill the time with covering the required rubric elements, so
only say more if you have extra time to fill.
• The recommended time limits would put your presentation around
15 minutes, so your team most likely won't have time for anything
else.
Sample Use Case
Walkthrough
Overview
• This is intended to give you a rough sense of how you might present
your user story and associated use case during your presentation.
• Note: this should be viewed as what would be acceptable — not
what would earn full marks, as there would be room for
improvement in terms of the presentation of the content.
Use Case (minimally)
• The user should be able to log into the program.
Before and After
Use Case Interactor Code
````

### Visual Walkthrough Slides (Slides 13–16 Screenshots & Transcriptions)

- **Slide 13 (`Use Case (minimally)`):** [presentation_template_slide13_use_case.png](./assets/presentation_template_slide13_use_case.png)
  - Bullet: `The user should be able to log into the program.`
- **Slide 14 (`Before and After`):** [presentation_template_slide14_before_after_ui.png](./assets/presentation_template_slide14_before_after_ui.png)
  - Shows three UI window screenshots for `Login Example`: `Login Screen` (with Username `jon` and masked Password), `Logged In Screen` (`Currently logged in: jon`, Password field, `Log Out` / `Change Password` buttons), and blank `Login Screen`.
- **Slide 15 (Sample Clean Architecture UML Class Diagram for Login Use Case):** [presentation_template_slide15_uml_class_diagram.png](./assets/presentation_template_slide15_uml_class_diagram.png)
  - Highlights Clean Architecture components in orange boxes:
    - Controller: `LoginController` -> `LoginInputBoundary` / `LoginInputData`
    - Interactor: `LoginInteractor` (implements `LoginInputBoundary`, depends on `LoginOutputBoundary` and `LoginUserDataAccessInterface`, creates `LoginOutputData`, interacts with `User` / `CommonUser` / `UserFactory` / `CommonUserFactory`, implemented by `DBUserDataAccessObject`)
    - Presenter & ViewModels: `LoginPresenter` (implements `LoginOutputBoundary`), `ViewManagerModel`, `ViewManager`, `LoginState`, `LoginViewModel`, `LoggedInState`, `LoggedInViewModel`
    - Views: `LoginView`, `LoggedInView`
- **Slide 16 (`Use Case Interactor Code`):** [presentation_template_slide16_interactor_code.png](./assets/presentation_template_slide16_interactor_code.png)
  - Code shown on slide (with orange callout boxes around fail/success branches):

```java
@Override
public void execute(LoginInputData loginInputData) {
    final String username = loginInputData.getUsername();
    final String password = loginInputData.getPassword();
    if (!userDataAccessObject.existsByName(username)) {
        loginPresenter.prepareFailView(username + ": Account does not exist.");
    }
    else {
        final String pwd = userDataAccessObject.get(username).getPassword();
        if (!password.equals(pwd)) {
            loginPresenter.prepareFailView("Incorrect password for \"" + username + "\".");
        }
        else {
            final User user = userDataAccessObject.get(loginInputData.getUsername());

            userDataAccessObject.setCurrentUsername(user.getName());
            final LoginOutputData loginOutputData = new LoginOutputData(user.getName(), false);
            loginPresenter.prepareSuccessView(loginOutputData);
        }
    }
}
```

---

## 3. Official Attached Files Summary

| File ID | Display Name | Content Type | Size (Bytes) | Last Updated (UTC) | Canonical Quercus Link |
| :---: | :--- | :--- | :---: | :---: | :--- |
| `46148571` | `Project Rubric 2026 [Nov 15].pdf` | `application/pdf` | 61,196 | `2026-11-15T20:08:52Z` | https://q.utoronto.ca/courses/444184/files/46148571?wrap=1 |
| `45993099` | `Project Rubric 2026.pdf` | `application/pdf` | 56,965 | `2026-11-13T16:56:08Z` | https://q.utoronto.ca/courses/444184/files/45993099?wrap=1 |
| `45644698` | `project-readme-template-2026.md` | `text/x-markdown` | 913 | `2026-10-23T21:20:39Z` | https://q.utoronto.ca/courses/444184/files/45644698?wrap=1 |
| `46149397` | `Presentation Template 2026.pptx` | `application/vnd.openxmlformats-officedocument.presentationml.presentation` | 809,193 | `2026-11-15T21:55:56Z` | https://q.utoronto.ca/courses/444184/files/46149397?wrap=1 |
| `46149081` | `CSC207 Project Rubrics — Lab Summary 2026.pdf` | `application/pdf` | 604,891 | `2026-11-15T22:25:49Z` | https://q.utoronto.ca/courses/444184/files/46149081?wrap=1 |
