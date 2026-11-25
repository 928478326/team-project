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
