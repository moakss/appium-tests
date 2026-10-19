# My Demo App – Appium tests

Automated UI tests for the Sauce Labs **My Demo App** on Android and iOS, written once and run on both platforms.

**Stack:** Java 21, Maven, Appium 3 (UiAutomator2 / XCUITest), TestNG, AssertJ, Allure

## What is covered

The main business flows that work the same way on both platforms: the catalog and sorting, product details, the cart,
login and logout, and the full checkout with form validation. There are 16 test cases, split into `smoke` and `regression`.
Each one is described in [`docs/TEST_CASES.md`](docs/TEST_CASES.md).

## Prerequisites

- Java 21 and Maven
- Appium 3 with drivers: `appium driver install uiautomator2`, `appium driver install xcuitest`
- **Android:** a running emulator with a standard 4 KB page-size system image (16 KB images show a
  compatibility dialog on every app start), or a connected device
- **iOS:** a booted simulator with the language set to English (US).
  Turn on *Settings → Accessibility → Keyboards & Typing → Full Keyboard Access*. Optionally install `ffmpeg` to get
  screen recordings of failed tests.
- The My Demo App installed on the device, or a build passed with `APP=...` (see below)

## Running

Nothing needs to be configured. The tests find the running emulator or booted simulator themselves.
They also start an Appium server, or reuse one that is already running.

```bash
make list                          # all test cases with their test methods
make smoke                         # smoke suite on Android (default)
make regression PLATFORM=ios       # regression suite on iOS
make tc TC=TC-04                   # one test case by ID
make tc TC='TC-10 TC-12' PLATFORM=ios
make test TEST=CartTest            # one test class (or Class#method)
make smoke APP=~/builds/app-debug.apk   # install this build instead of using the installed app
make report                        # Allure report of the last run
make appium                        # Appium server + Inspector at http://127.0.0.1:4723/inspector
```

Without `make`: `mvn test -Dplatform=android -Dsuite=smoke`. Add `-Dtest=CartTest` to run one class, or `-Dapp.path=...` to install a build.

### Optional settings

All settings live in `src/test/resources/config/` and can be overridden with `-D<key>=<value>`:

- `default.properties`: Appium host/port, timeouts, `ANDROID_HOME` / `JAVA_HOME` if they are not detected automatically
- `devices.file=config/devices.json`: use fixed devices and ports instead of auto-detection

## Approach

- **Page objects:** each screen has a page class. Tests contain only the business flow and the assertions, with no
  locators or waits.
- **Platform differences** are handled inside the pages and components (e.g. the Android side menu vs the iOS "More"
  tab), so tests read the same for both platforms.
- **One fresh app session per test**, so tests don't depend on each other or on the order they run in.
- **Explicit waits only**, all in one place (`core/Waits`), with no fixed sleeps.
- **Test data** comes from JSON files in `src/test/resources/testdata`.
- **When a test fails**, it saves a screenshot, a screen recording, the page source, device and Appium logs, and
  session info to `target/artifacts/<test>/`. These are also attached to the Allure report.

**Selectors:** IDs and accessibility IDs are used wherever the app has them. Many iOS elements have none, so those
locators fall back to the element type plus its visible text, and are marked `GAP-WORKAROUND` in the code. In a real
project these would be replaced by proper IDs added to the app.

## Project structure

```
src/main/java/com/mydemoappappium/
  config/      configuration (platform, timeouts)
  driver/      session creation, device detection and pool, Appium server
  core/        base page/component, waits, gestures, platform locators
  components/  reusable UI parts (navigation, validation messages, cart line, ...)
  pages/       one class per app screen
  testdata/    test data records and JSON loader
src/test/java/com/mydemoappappium/
  tests/       test classes by feature
  listeners/   session lifecycle and failure artifacts
src/test/resources/
  config/  suites/  testdata/
docs/TEST_CASES.md   test scenarios
```
