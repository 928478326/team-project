# CampusBuddy

CampusBuddy is a Java Swing app for University of Toronto students to find people to study with or join for campus activities.

## Track A MVP — student accounts

**Use case lead:** Gracie (@graceliu369).

**User story:** As a student sharing a computer, I want to create my own account, sign out when I leave, and return to the same account on my next visit.

| Use case | What the student does | Result |
| --- | --- | --- |
| Sign up | From the account screen, enter a new username and the same password twice. | A new account opens with the username as its display name. A taken name, a blank or short password, or mismatched entries leave the form open. |
| Log out | From the signed-in screen, select **Logout**. | The account screen returns. The next student must enter their own credentials. |
| Log in | From the account screen, enter an existing username and password. | Valid credentials open that student's account. Incorrect credentials leave the student signed out. |

## Run the account demo

Use Java 11 and Maven. From an IDE, run `app.Main` with the program argument `--sqlite`. With Maven, run:

```sh
mvn compile org.codehaus.mojo:exec-maven-plugin:3.5.0:java -Dexec.mainClass=app.Main -Dexec.args=--sqlite
```

Register a new account, log out, close the app, and run it again from the same directory. Log in with the original password. Accounts are saved in `data/campusbuddy.db`. Launching without arguments uses the team's in-memory demo data.

Run `mvn clean verify` for the tests, including the architecture dependency check. The coverage report is generated at `target/site/jacoco/index.html`.

## Post-meetup buddy ratings

**MVP lead** — Gracie (@graceliu369).

As a student, I want to record how a meetup went and whether I would meet that buddy again, so my past experience can inform future buddy matches.

| Use case | What the student sees |
| --- | --- |
| Find a buddy to rate | **Ratings** lists other students who signed up for the same event. A row becomes available when the event starts. Select someone you actually met. |
| Rate or update a meetup | Choose 1–5 stars, indicate whether you would meet again, and optionally add a comment of up to 300 characters. Saving again updates your feedback for that buddy at that event. Each student sees only the ratings they wrote. |

### Try the ratings demo

For a quick walkthrough, run `app.Main --dev-user=gracie` and open **Ratings**. The in-memory demo includes the Post-Midterm Hotpot Hangout.

To demonstrate saving across launches, use a demo database in a separate working directory:

1. Launch `app.Main --sqlite` there and register the demo accounts `gracie` and `henry` with passwords of your choice.
2. From that directory, load the [prepared meetup](demo/rating-meetup.sql) with `sqlite3 data/campusbuddy.db < /path/to/team-project/demo/rating-meetup.sql`. This adds one shared study meetup for those accounts.
3. Log in as Gracie, open **Ratings**, select Henry at **Robarts study meetup** and save feedback. Edit the rating and save again.
4. Restart with `--sqlite` from the same directory and log back in as Gracie. The saved values return. Log in as Henry to see his own feedback form for Gracie.

### Rating walkthrough

The same student can reopen a meetup and edit their feedback.

| Before saving | After saving |
| --- | --- |
| ![Unrated buddy at the Robarts study meetup](docs/images/buddy-rating-before.png) | ![Saved five-star rating with a comment and willingness to meet again](docs/images/buddy-rating-saved.png) |

```mermaid
sequenceDiagram
    actor Student
    participant V as BuddyRatingView
    participant C as RateBuddyController
    participant I as RateBuddyInteractor
    participant D as Rating data access
    participant P as RateBuddyPresenter
    Student->>V: Choose a buddy, stars and feedback
    V->>C: Save rating
    C->>I: Submit rating input
    I->>I: Create and validate BuddyRating
    I->>D: Save if both joined and the event has started
    D-->>I: Saved or ineligible
    I->>P: Present outcome
    P-->>V: Update the view model
    V-->>Student: Show confirmation or explain the error
```
