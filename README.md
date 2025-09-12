# Braille Transcription Ledger

_Accessible Document Transcription Tracker for Teachers & TVIs_

---

## Table of Contents

1. [Overview](#overview)
2. [How the Program Works](#how-the-program-works)
3. [Compiling & Running the Program](#compiling--running-the-program)
    - [Using Gradle](#using-gradle)
    - [Using Maven](#using-maven)
    - [Using Ant](#using-ant)
4. [JSON Configuration Files](#json-configuration-files)
5. [Accessibility & Compliance](#accessibility--compliance)
6. [Diagrams](#diagrams)
7. [How to Contribute](#how-to-contribute)
8. [License](#license)
9. [Contact](#contact)

---

## Overview

Braille Transcription Ledger is a desktop application designed to help teachers and TVIs (Teachers of the Visually Impaired) track, manage, and report on accessible document transcription projects. It features a simple graphical interface, persistent storage, and easy reporting.

- **No coding required to use!**
- **Runs on Windows, Mac, and Linux**
- **Tracks students, teachers, TVIs, subjects, schools, and project details**
- **Generates PDF reports for billing or record-keeping**
- **Accessible and easy to use**

---

## How the Program Works

### Step-by-Step Guide

1. **Startup**
    - Double-click the application JAR file (or run from command line).
    - On first launch, the program creates its database (using H2, no setup needed) and loads dropdown options from `.json.txt` files.

2. **Project Setup Tab**
    - Create a new transcription project.
    - Select or enter:
        # Braille Transcription Ledger

        Accessible document transcription tracker for teachers and TVIs (Teachers of the Visually Impaired).

        This README focuses on practical, hands-on information you need to build, run, configure, test, and navigate the application.

        ## Quick start (build, package, run)

        Prerequisites
        - Java 17 or newer installed and available on PATH.
        - A Java build tool (Maven is the primary supported build in this repository).

        Build (Maven)

        PowerShell example (project root):

        ```powershell
        mvn -DskipTests package
        ```

        This produces the runnable shaded JAR in `target/` (artifact name includes "shaded").

        Run (PowerShell):

        ```powershell
        # run with default settings
        java -jar target/*-shaded.jar

        # increase logging verbosity
        java -DLOG_LEVEL=DEBUG -jar target/*-shaded.jar
        ```

        Run a specific DB location (overrides all other settings):

        ```powershell
        java -jar target/*-shaded.jar --dbpath=C:\\data\\myledger
        ```

        Run with environment variable (PowerShell):

        ```powershell
        $env:LEDGER_DB_PATH = 'C:\\Users\\You\\.brailleledger\\ledger'
        java -jar target/*-shaded.jar
        ```

        ## Build systems supported

        - Maven (primary): use `mvn package` / `mvn test`.
        - Gradle: a Gradle wrapper is present if you prefer `./gradlew` tasks.
        - Ant + Ivy: a fallback `build.xml` + `ivy.xml` are included for environments that require Ant-based builds. Use the `resolve` and `assemble` targets.

        If you use Ant and your environment blocks downloads, place a compatible Ivy jar in `lib/` before running the Ant resolve target.

        ## Configuration: database path, config.properties, and Preferences

        The application locates the H2 database using this precedence (highest → lowest):

        1. CLI argument: `--dbpath=<path>` (do not include `.db` suffix)
        2. Environment variable: `LEDGER_DB_PATH`
        3. `config.properties` in the program root (key: `db.path`)
        4. User home: `~/.brailleledger/ledger` (if present)
        5. `app_home/ledger` inside the application root (if present)
        6. Working directory default: `./ledger`

        Notes
        - The H2 URL used is `jdbc:h2:<base>;AUTO_SERVER=TRUE` to allow concurrent connections (useful for testing and for tools such as the H2 Console).
        - Use absolute paths to avoid ambiguity on Windows (backslash escaping in PowerShell is required).

        Runtime configuration keys in `config.properties` (also editable via the in-app Preferences dialog):

        - `db.path` — base H2 path (examples: `./app_home/ledger`, `D:/data/ledger`)
        - `window.maximized` — `true` or `false` (main window startup)
        - `log.level` — `DEBUG`, `INFO`, `WARN`, `ERROR`

        Preferences dialog
        - From the application menu, open Preferences to edit `db.path`, `window.maximized`, and `log.level`. Changes are applied immediately where possible (DB reconnect is supported when `db.path` changes).

        ## Running tests and quick checks

        Run unit + integration tests (Maven):

        ```powershell
        mvn test
        ```

        Run a single test class:

        ```powershell
        mvn -Dtest=SubmitFlowIntegrationTest test
        ```

        Notes
        - Tests are JUnit 5 based and exercise DB-resolution and submit flow behavior.
        - The codebase exposes a test hook used by the tests so the DB path resolution is deterministic during CI.

        ## Application UI — quick navigation and behavior

        Main window areas:

        - Project Setup tab: create new projects. Fill fields (Student, Teacher(s), TVI(s), School, Subject, Media Type, Notes) and press the Submit button located directly under the input form to create the project and its first details.
        - Project Status tab: add time/element tracking for an existing project (select project then add tracking entries).
        - Projects tab: view/filter/sort projects. An "Open Project" button opens the filesystem folder for the selected project (the Submit flow creates project folders in `app_home/projects/<sanitized-name>`).

        Submit flow notes
        - On Submit the app will create `app_home/projects/<sanitized-name>` under the application root (if not present). It also persists that folder path into the `Project_Name.folder_path` column.
        - During the same flow the database file is consolidated into the application `app_home` folder (moved/created as `app_home/ledger` / `app_home/ledger.db` depending on the platform). This behavior is automatic when Submit creates a new project folder.

        Status & dialogs
        - The application shows a non-modal status bar for background progress and short messages, and uses modeless dialogs where appropriate so the UI remains responsive.

        Keyboard accessibility
        - Full keyboard support for form submission, navigation, and PDF generation. See the Help menu in-app for an up-to-date list of shortcuts.

        ## JSON option files (what the app loads at startup)

        Files live in `json_files/` and are shipped with example data. The app will load those files at startup to populate dynamic dropdowns (students, teachers, tvis, schools, subjects, media types, delivery modes, proof status, project types, project elements).

        File naming
        - The repository stores them with `.json` or `.json.txt` extensions. The loader accepts both; ensure the JSON structure is an object with a single array property (see examples in the repo).

        Example (students.json):

        ```json
        {
            "students": ["Student A", "Student B"]
        }
        ```

        ## Packaging and a quick smoke test

        Build the shaded JAR (recommended for distribution):

        ```powershell
        mvn -DskipTests package
        ```

        Run a short smoke start (10–20s) to confirm startup, option loading, and DB connection:

        ```powershell
        # start the app and capture logs
        java -jar target/*-shaded.jar *> ledger-run.log 2>&1 & Start-Sleep -Seconds 10; Get-Content ledger-run.log -Tail 200
        ```

        What to look for in logs
        - "Attempting to load options from file: json_files/..." (option loads)
        - "Connecting to database at URL: jdbc:h2:..." (DB resolution)
        - No uncaught SQL exceptions on startup

        Common non-fatal warnings
        - FlatLaf / native-access warnings: these are JVM advisory messages about restricted native access and do not affect functionality. They can be ignored for normal use.

        ## Troubleshooting

        - Database locked / already in use: the app uses H2 with AUTO_SERVER; if you see lock errors, ensure no other process holds the file or use an absolute path in `--dbpath`.
        - If a JSON options file is missing or empty, dropdowns may show no entries — edit the corresponding file in `json_files/` and restart the app.
        - If the GUI logs a message that a field (e.g., Media Type) was empty on startup, confirm the `json_files/media_type.json` exists and contains a non-empty array.

        ## Contributing, testing, and code style

        - Fork and open a PR; include a short description and a changelog entry when appropriate.
        - Run `mvn test` locally before submitting a PR.
        - Follow the existing Java coding style and keep accessibility concerns in mind for UI changes.

        ## License

        This project is licensed under the Apache License 2.0 — see the `LICENSE` file.

        ## Contact

        - Open an issue in this repository for bugs or feature requests.

        ---

        If you'd like, I can now: (A) run `mvn -DskipTests package` to update the shaded JAR, (B) run the smoke start and show the tail of the log, or (C) push a branch and open a PR with these README updates. Tell me which to run next.
