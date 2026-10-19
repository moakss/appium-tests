# Appium Test Rules — My Demo App (Android + iOS)

These rules are **mandatory** for all work in this project. An exception requires explicit user
approval for the specific task and the specific exception (see §13).

## 0. Project boundaries

| Path | Role | Access |
|---|---|---|
| `~/Documents/Projects/Test/appium-homework/my-demo-app-appium` | This standalone test project | read/write |
| `~/Documents/Projects/Test/appium-homework/my-demo-app-android` | Android app under test | **read-only** |
| `~/Documents/Projects/Test/appium-homework/my-demo-app-ios` | iOS app under test | **read-only** |

- All test code, dependencies, configuration, CI files and generated artifacts live in this project only.
- Never edit files, add dependencies, build outputs or config in either app repository. Reading
  their sources to discover candidate locators is allowed.
- App binaries are referenced by path through configuration, never copied into the app repos.

## 1. Mandatory preflight

Before creating, editing, deleting or moving files:

1. Confirm the working directory is `~/Documents/Projects/Test/appium-homework/my-demo-app-appium`.
2. Confirm every change belongs to this standalone test project.
3. Treat both app repositories as read-only (§0).
4. Inspect the existing project structure, `pom.xml`, TestNG suites, configuration sources and the
   relevant pages/components before proposing changes.
5. Check Java, Maven, Appium server + drivers (UiAutomator2, XCUITest), the configured APK/.app and
   required device/simulator availability as appropriate for the task.
6. Identify which requirements can be verified in the current environment and which need a real
   device, Appium Inspector, an external service or user action.
7. Confirm the proposed changes preserve every mandatory rule in this file.

If a prerequisite is missing, a project boundary is ambiguous, or a required decision cannot be made
safely, **stop and ask the user**. Do not silently substitute tools, invent locators, modify another
repository or weaken a mandatory rule.

### 1.1 Device prerequisites (one-time, manual)

- **Android emulator:** a standard **4 KB page-size** system image (`adb shell getconf PAGE_SIZE` →
  `4096`). 16 KB (`ps16k`) images show an "App Compatibility" dialog on every app start that blocks tests.
- **iOS simulator:**
  - Language & Region **English (US)** — verified per session (§4.1).
  - **Settings → Accessibility → Keyboards & Typing → Full Keyboard Access → On.** Together with the
    `connectHardwareKeyboard` capability (`ios.properties`) this keeps the software keyboard from
    covering "To Payment"; the app itself offers no way to dismiss the keyboard. Writing the preference
    via `simctl … defaults write` does **not** take effect — it must be switched on in the Settings app.
  - **`ffmpeg`** on the Mac (`brew install ffmpeg`) for screen recordings of failed iOS tests; without it the
    recording is reported as missing (Android records on the device and needs nothing).

## 2. Technology stack

- **Language:** Java 21. **Build:** Maven (single module). No Gradle, Kotlin or other languages.
- **Appium:** Appium 3 server, `io.appium:java-client` 11.x (Selenium pinned via `selenium-bom`),
  UiAutomator2 (Android), XCUITest (iOS).
- **Runner:** TestNG (suites in `src/test/resources/suites/`). **Assertions:** AssertJ.
- **Logging:** SLF4J + Logback. **Reporting:** Allure (TestNG adapter, `allure-maven` plugin;
  report via `mvn allure:serve`). Steps via `@Step` on page methods, test case IDs via `@TmsLink`.
- **Base package:** `com.mydemoappappium`. **Maven artifactId:** `my-demo-app-appium`.
- Dependency versions are pinned in `pom.xml` `<properties>`; no version ranges, no `LATEST`.
- Run examples: `mvn test -Dplatform=android`, `mvn test -Dplatform=ios -Dsuite=smoke`.

## 3. Architecture — Page Object Component Model

```
src/main/java/com/mydemoappappium/
  config/        Config (immutable, resolved once per session), Platform enum
  driver/        DriverFactory (only place drivers are created), DriverHolder (ThreadLocal),
                 DevicePool (UDID + port allocation), AppiumServerManager (one server per run)
  core/          BasePage, BaseComponent, PlatformLocator (Android/iOS By pair), Waits (all explicit
                 waits), Gestures (low-level gesture mechanics), AppStateGuard (crash detection)
  components/    Reusable UI parts used by pages only: HeaderBar, NavigationMenu (AndroidSideMenu /
                 IosMoreMenu), SortDialog, ProductCard, CartRow, ValidationMessage, ConfirmDialog
  pages/         One class per app screen: CatalogPage, ProductDetailsPage, CartPage, LoginPage,
                 ShippingAddressPage, PaymentPage, ReviewOrderPage, CheckoutCompletePage, ...
  testdata/      Test data records + loader: User, Address, PaymentCard, TestData (reads JSON)
src/test/java/com/mydemoappappium/
  tests/         Test classes only, grouped by feature (catalog, product, cart, checkout, auth).
                 No base test class.
  assertions/    Optional assertion helpers for repeated checks (e.g. CartAssertions)
  listeners/     SessionListener (session per test method), ArtifactCollector (failure artifacts, run by SessionListener),
                 SuiteListener (server lifecycle)
src/test/resources/
  config/        default.properties, android.properties, ios.properties, devices.json
  testdata/      users.json, addresses.json, cards.json
  suites/        smoke.xml, regression.xml, parallel.xml
```

Rules:

**Test-to-UI interaction boundary**
- Test classes interact **only with page objects**. Tests never access components directly; a page
  composes its components internally and never returns a component object to a test.
- Tests contain business flows and assertions only. No `driver`, `By`, `findElement`, locators,
  waits, raw `WebElement`s or gestures in test classes.
- Pages expose intent-level methods such as `openProduct(String title)`, `addToCart()`,
  `proceedToCheckout()`. Queries return plain values (String, int, BigDecimal, List).
- Components encapsulate reusable UI behaviour (header, navigation menu, sort dialog, cart row) and
  are invoked by pages, not by tests.
- Navigation methods return the destination page object and wait for its loaded condition before
  returning (§5, §6).
- Assertions belong in tests or dedicated assertion helpers (`assertions/` in `src/test/java`), never
  in pages or components. `waitUntilLoaded()` is synchronization (it times out with a descriptive
  message), not an assertion.
- One cross-platform page class per app screen. Multi-page sequences (e.g. checkout) are composed in
  the test by chaining page methods — no separate flow layer.

**Locators and element references**
- **Locator strategy: `By` + `PlatformLocator`** (decided). Locators live only in pages and
  components as `private final By` fields, or private methods for dynamic locators. They are never
  exposed outside their class.
- Platform-specific locators are declared side by side with `PlatformLocator.of(androidBy, iosBy)`,
  resolved against the session's platform. PageFactory annotations (`@AndroidFindBy`,
  `@iOSXCUITFindBy`, `@CacheLookup`) and `PageFactory.initElements` are not used.
- Element interactions are centralized in `BasePage` / `BaseComponent` helpers (`click(By)`,
  `type(By, String)`, `textOf(By)`, `isVisible(By)`), which wait through `Waits` and look the
  element up fresh on every call. Page/component methods use these helpers and expose user intent.
- **No cached elements:** pages and components store locators and stable state only — never
  long-lived `WebElement` fields. Elements are looked up at the moment of use (through `Waits`) and
  not kept beyond a single method call.
- Where behaviour differs structurally (Android side drawer vs iOS "More" screen), define a
  component interface (e.g. `NavigationMenu`) with `AndroidSideMenu` / `IosMoreMenu` implementations
  selected by a factory. Platform branching lives only in such factories and `PlatformLocator` —
  never in tests.

**Synchronization is centralized**
- All waiting goes through `core/Waits`. Pages and components call `Waits`; they never create their
  own `WebDriverWait`/`FluentWait` or use sleeps (§5).

**Gestures**
- Swipes, scrolling, drag-and-drop, long press and similar operations are exposed only through
  intent-oriented page/component methods (e.g. `scrollToProduct(title)`, `drawSignature()`).
- Reusable low-level gesture mechanics (W3C Actions, `mobile:` gesture commands, `UiScrollable`)
  live only in the shared `core/Gestures` utility.

**Configuration is immutable per test session**
- Platform, environment, capabilities and timeouts are resolved once (system properties > platform
  properties > defaults) into an immutable `Config` and never mutated during execution.
- Per-test capability overrides (§7) are applied only when the session is created.

**Driver lifecycle belongs to infrastructure**
- Sessions are created and quit only by `SessionListener` (TestNG listener registered via `META-INF/services/org.testng.ITestNGListener`, so it applies to every run, also `-Dtest=...`),
  which delegates to `DriverFactory` / `DriverHolder` (§7). There is no base test class. Tests never create, quit or reconfigure a session.

**Test data**
- Test data (users, addresses, cards, products) comes from `testdata/*.json`, loaded into the
  records in the `testdata` package, never inline literals spread across tests. Platform differences
  in data (Android `bod@example.com` vs iOS `bob@example.com`) are resolved in the data layer.
- No static mutable state except the `ThreadLocal` driver holder and the thread-safe `DevicePool`.

**Comments**
- Comments are short and meaningful: explain *why* (a workaround, a non-obvious constraint, a
  locator status), never *what* the code already says.
- Not everything needs a comment. No comments restating method/field names, no boilerplate Javadoc,
  no section-divider comments. Required comments only: locator status blocks, `GAP-WORKAROUND`
  markers and documented workarounds (§4, §9).

## 4. Locator rules (ID-first)

Priority — use the first one that uniquely and stably identifies the element:

| # | Android (UiAutomator2) | iOS (XCUITest) |
|---|---|---|
| 1 | `AppiumBy.accessibilityId` (content-desc) | `AppiumBy.accessibilityId` (accessibilityIdentifier / name) |
| 2 | `AppiumBy.id` (full resource-id `com.saucelabs.mydemoapp.android:id/...`) | Fallback: `AppiumBy.iOSNsPredicateString` combining `type` + one verified attribute (`label`, `value` or `placeholderValue`) |
| 3 | `AppiumBy.androidUIAutomator` with `UiSelector().resourceId(..).text(..)` for scoped text matching | Fallback: `AppiumBy.iOSClassChain` scoped to a stable screen identifier, **without** positional indices |
| 4 | `UiScrollable` — only to scroll an element into view in a scrollable container | — |

Prohibited:
- **XPath** of any form.
- Positional selection: indices (`[1]`, `.get(2)`, `instance(n)`, `index(n)`), coordinates, "nth
  element of class X".
- Locators in test classes.
- Text-only locators where an ID exists; localized text as the sole selector when a stable ID exists.

Additional rules:
- Repeated IDs (Android `itemTV`, `productTV`, `paymentBtn`, `cartBt`) must be scoped: either to the
  page's verified loaded state, a parent component element (`parent.findElement(...)`), or a
  `UiSelector` combining resource-id + text.
- Dynamic locators (e.g. a product by name) are built by private methods inside the page/component.
- Verify **every new locator against the running app** before declaring it verified. Reading app
  source, resource IDs or a page-source dump identifies a *candidate* only — it is not verification.
- **Accepted verification methods** (user decision):
  - **Appium live search** — on the expected screen of the running emulator/simulator, execute the
    exact locator the page/component will use through the Appium server (`findElements`, the same
    search Appium Inspector performs), confirm it resolves to **exactly one** element, and confirm
    that element is the intended one (attributes, text, enabled/visible state).
  - **Appium Inspector** — the same checks performed manually by the user in Inspector
    (`http://127.0.0.1:4723/inspector`).
  - The user spot-checks critical-flow locators (login, cart, checkout) in Inspector.
- If neither method is possible (no server, device or reachable screen), do not claim verification;
  stop before relying on the unverified locator and ask the user.
- If an element has no suitable accessibility ID / resource ID / stable name, **report the
  testability gap to the user** instead of inventing a brittle locator.

### 4.1 iOS fallback locators (missing accessibility identifiers)

The iOS app lacks identifiers on many elements and cannot be modified. Standard (decided):

- **Prefer stable accessibility identifiers** whenever available. A fallback is used only when the
  identifier is missing and the application cannot be modified.
- **Predicate fallback:** combine the element `type` with one verified attribute — `label`, `value`
  or `placeholderValue`, e.g.
  `type == 'XCUIElementTypeTextField' AND placeholderValue == 'Full Name'`.
- **Scoped class chain:** use a class chain scoped to a stable screen identifier (e.g.
  `**/XCUIElementTypeOther[`name == 'ShippingAddress-screen'`]/**/XCUIElementTypeTextField[`placeholderValue == 'Full Name'`]`)
  when scoping materially improves uniqueness (same text appears elsewhere on the screen/hierarchy).
- **Verification:** every fallback locator must be verified (Appium live search or Inspector) and must resolve to
  **exactly one element** in the expected screen state. Zero or multiple matches → not acceptable.
- **Controlled language/locale:** the simulator is set to English (`en_US`) once, and `DriverFactory`
  verifies it on every iOS session (`mobile: deviceInfo` → `currentLocale`, expected value in
  `ios.properties`) and fails fast on a mismatch. The `language`/`locale` capabilities are **not** used:
  they restart SpringBoard on every session, which randomly kills WebDriverAgent. Mandatory because
  fallback locators depend on visible text and the app ships a German localization.
- **Encapsulation:** fallback locators stay `private final By` in pages/components like every other
  locator; elements are reacquired on every interaction (§3).
- **Marking:** every fallback locator carries the comment
  `// GAP-WORKAROUND: no accessibilityIdentifier, suggested "<id>"` directly above it.
- **No reliable locator:** if no fallback resolves to exactly one element reliably, the affected test
  is reported as **blocked or skipped with an explicit reason** (`SkipException("Blocked: no reliable locator for <element> on <screen>")`).
  Never silently weaken uniqueness requirements (no indices, no `findElements().get(n)`, no XPath).
- **Technical debt:** missing accessibility identifiers are reported to the user (element, screen,
  suggested ID), who files the request to the application owner in their tracker. No separate
  technical-debt document is kept in this project. When the identifier ships, the fallback is replaced.
- Each page/component class keeps a short locator status comment block:
  `VERIFIED (Appium live search | Inspector, date, device)` or `CANDIDATE (source only)`.
  Predicate attributes for fallbacks are taken from the live element tree, not from source.

## 5. Explicit waits and timeouts

- Explicit waits only, all through `core/Waits`. **Implicit wait stays 0** and is never changed.
- Never use `Thread.sleep()`, `TimeUnit.sleep()` or any fixed delay as synchronization.
- Timeouts come from configuration only: `timeout.defaultSeconds`, `timeout.launchSeconds`,
  `timeout.pollingMillis`. No magic numbers in tests, pages or components.
- FluentWait polling is permitted; it is not a substitute for a synchronization condition.
- Every navigation action waits for a **destination-specific** loaded condition before returning
  (e.g. iOS `Catalog-screen`, Android `productTV` with text "Products" + `productRV`). A generic
  element being visible is not sufficient unless it uniquely identifies the destination.

## 6. Page contract

- Every page implements `waitUntilLoaded()` (called by the constructor or the navigating method)
  and `isDisplayed()`.
- Navigation methods return the next page already loaded.
- After each navigation, `AppStateGuard` confirms the app is still in the foreground (§11).

## 7. Test session lifecycle and state independence

**One session per test method**
- Create a separate Appium driver session for **each test method** by default
  (`SessionListener` creates it before the test method and always quits it afterwards).
- Drivers are created and capabilities configured **only** in `DriverFactory`, stored in a
  `ThreadLocal` `DriverHolder`.
- Cleanup after every test method is guaranteed: `quit()` and `ThreadLocal.remove()` run in
  `finally` blocks, including when setup, the test or artifact collection fails.

**App state**
- `noReset=false` by default, to favour a predictable app state between independent sessions.
- Do not assume `noReset=false` alone guarantees a completely clean device or backend state
  (e.g. device settings, permissions, other apps, simulator keychain are not necessarily reset).
- Each test establishes its own preconditions and never depends on another test's execution, order
  or side effects. Tests requiring a clean state explicitly establish **and verify** their
  preconditions (e.g. assert cart badge is empty, assert user is logged out) before the actual steps.
- Tests needing preserved app data or an existing authenticated session must explicitly configure the
  required capabilities and state (e.g. a per-test capability override passed to `DriverFactory`).
  Such exceptions must be justified in the test's Javadoc.

**Parallel isolation**
- Never share a driver session between parallel test methods. Each concurrent session uses a
  dedicated driver, a unique device UDID and unique required Appium ports:
  Android `systemPort` (+ `mjpegServerPort` when recording), iOS `wdaLocalPort` (+ `mjpegServerPort`).
- If device/port allocation fails, fail clearly before the test starts. Never silently share a device
  or port. Fewer devices than workers → fail fast or use an explicitly configured lower worker count.
- The Appium server is started/stopped once per run (suite listener) or provided externally — never
  started/stopped from parallel test threads.
- `ANDROID_HOME` / `JAVA_HOME` are passed only to the Appium server process — by `AppiumServerManager`
  (`AppiumServiceBuilder.withEnvironment`) or `scripts/start-appium.sh` — from
  `config/default.properties` (`appium.env.*`). Never require changes to the user's shell profile.

**Session reuse**
- Any deliberate session reuse (e.g. per class) requires explicit justification, user approval (§13)
  and must preserve test independence.

## 8. Failure artifacts and cleanup

On every test failure, attempt each artifact **independently**:
- Screenshot (PNG)
- Screen recording (started at test start, saved only on failure)
- Page source (XML)
- Device log: Android `logcat`, iOS `syslog` (+ crash log when available)
- Appium server log for the run (captured to a file)
- Platform details (`session-info.json`): platform, device name, UDID, OS version, app ID and
  version, Appium server and driver versions, session ID, effective capabilities

Rules:
- Store under `target/artifacts/<test-name>/` with collision-safe names
  (`<test>_<platform>_<udid>_<sessionId>_<timestamp>`).
- Correlate test report, device log, recording and server log via session ID where possible; attach to Allure.
- A failure collecting one artifact must not stop the others, and must never replace or mask the
  original assertion failure or exception.
- Stop recordings and release driver resources even if collection fails.
- Report which artifacts were captured and which were not (with the reason when known).
- On passing tests discard the recording and failure-only artifacts.

## 9. Test design rules

- One behaviour per test; descriptive names: `shouldShowErrorWhenUsernameIsEmpty()`.
- Each test carries a test-case ID from `docs/TEST_CASES.md` (`@Test(description = "TC-04 ...")` and
  Allure `@TmsLink`/`@Story`).
- TestNG groups: `smoke`, `regression`, `android-only`, `ios-only`.
- Platform-specific tests are skipped (not failed) on the other platform, with the reason logged.
- Assertions use AssertJ with a descriptive message.
- **App bugs are not tested, they are reported.** No tests are written for known bugs or crashes;
  they are reported to the user, who files them in the tracker. Never assert buggy behaviour as
  correct, and never weaken a business-flow assertion to make it pass.
- A bug inferred from reading app source code is only a **suspected** bug. Reproduce it on an
  emulator/simulator/device (or get the user's confirmation) before reporting it as a bug. Never
  present a suspected bug as a known bug.
- If a known bug blocks a business flow, use a documented workaround in the test (with a code comment
  explaining why) instead of a separate bug test — e.g. sorting to keep products within the first 6
  catalog positions on Android.

## 10. Git safety

- Never run state-changing Git commands in any repository, and never trigger Git operations
  indirectly (scripts, IDE source-control actions, other tools).
- Do not stage, commit, push or alter history. **The user performs all Git operations.**
- No Git commands at all unless read-only inspection is explicitly permitted by the user.
- At completion, list changed files and summarize changes; a suggested commit message may be given as text only.

## 11. App crash handling

- Never reinterpret an unexpected crash, restart or return to a previous page as successful navigation.
- Detect unexpected termination (`queryAppState` ≠ `RUNNING_IN_FOREGROUND`, or unexpected
  package/activity on Android) and preserve crash evidence plus failure artifacts.
- Any app crash in an ordinary business-flow test is a **failure** — never worked around silently.

## 12. Verification status and completion reporting

Use separate, evidence-based statuses:
- **Implemented** — code changes are present.
- **Build-verified** — the relevant Maven build completed successfully.
- **Emulator/Simulator-verified** — the relevant tests passed on an Android emulator / iOS simulator.
- **Real-device-verified** — the critical flow passed on a real device.
- **CI-verified** — the configured CI workflow ran successfully.

Never report a level that has not actually happened. Record the command, the result and limitations.
Parallel execution must be tested with more than one worker when infrastructure allows; otherwise
explain why and do not claim it passed.

## 13. Rule exceptions

An exception to a mandatory rule requires explicit user approval for the specific task and the
specific exception. Approval for one locator or scenario is not blanket permission. Never infer
approval from silence.

## 14. Pre-completion checklist

- [ ] No XPath, no positional indices, no locators in test classes.
- [ ] No `Thread.sleep()`; implicit wait remains 0; timeouts come from config.
- [ ] New UI parts follow the Page Object Component Model; tests use pages only, never components.
- [ ] No assertions in pages/components; no `WebElement` fields or `@CacheLookup` in pages/components.
- [ ] All waits go through `Waits`; all low-level gestures go through `Gestures`.
- [ ] Config is not mutated during execution.
- [ ] Driver is ThreadLocal; one session per test method (`noReset=false`); sessions have unique device and port allocations.
- [ ] Every test establishes and verifies its own preconditions; any session exception is justified and documented.
- [ ] Parallel execution tested with > 1 worker, or the blocker is reported.
- [ ] Failure listener attempts screenshot, video, page source, device log and platform details without masking the original failure.
- [ ] Appium server log is captured for the run.
- [ ] Nothing added to or changed in the Android or iOS app repositories.
- [ ] No Git commands or indirect Git operations were run.
- [ ] Verification status (Appium live search / Inspector) reported honestly for every new locator.
- [ ] Every iOS fallback locator: type + verified attribute, resolves to exactly one element, marked `GAP-WORKAROUND`, reported to the user; simulator locale `en_US` verified per session.
- [ ] Critical flows have real-device verification before being considered fully accepted.
- [ ] Tests actually executed and results reported honestly.
- [ ] All changed files and outstanding verification gaps are listed.
