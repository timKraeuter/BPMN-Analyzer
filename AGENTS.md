# AGENTS.md

This file provides guidance for AI coding agents operating in this repository.

## Project Overview

Monorepo with three sub-projects for analyzing BPMN models via graph transformation:

| Sub-project | Path | Language | Framework |
|---|---|---|---|
| **generation-ui** | `generation-ui/` | TypeScript 5.9 | Angular 21 (standalone components), Angular Material |
| **generator** | `generator/` | Java 25 | Plain Java library (Camunda BPMN Model API, Gradle) |
| **server** | `server/` | Java 25 | Spring Boot 3.5, Gradle |

The server includes the generator as a composite build (`settings.gradle` has `includeBuild "../generator"`).
The UI build output goes to `server/src/main/resources/public/`.

## Build Commands

### generation-ui (run from `generation-ui/`)
```
npm ci                 # Install dependencies
npm start              # Dev server on port 4200
npm run build          # Production build
npm run check          # Prettier check (CI uses this)
npm run format         # Prettier auto-fix
```

### generator (run from `generator/`)
```
./gradlew build        # Compile + test + checks
./gradlew check        # Run all checks (test + spotless + errorprone)
./gradlew spotlessApply  # Auto-format Java (Google Java Format)
./gradlew spotlessCheck  # Verify formatting
```

### server (run from `server/`)
```
./gradlew build        # Compile + test + checks
./gradlew bootJar      # Build Spring Boot fat JAR
./gradlew check        # Run all checks
./gradlew spotlessApply  # Auto-format Java
```

## Test Commands

### generation-ui - Unit Tests (Karma + Jasmine)
```
npm test                                          # Run all (launches Chrome)
npm test -- --watch=false --browsers=ChromeHeadless  # CI/headless mode
npx ng test --include=**/analysis.component.spec.ts  # Single test file
```
Use `fdescribe`/`fit` in spec files to focus individual tests during development.

### generation-ui - E2E Tests (Playwright)
```
npm run e2e                                        # Run all (auto-starts dev server)
npm run e2e:ui                                     # Run with Playwright UI
npx playwright test e2e/analysis.spec.ts           # Single test file
npx playwright test -g "test name substring"       # Single test by name
```

### generator - Java Tests (JUnit 5)
```
./gradlew test                                                    # Run all
./gradlew test --tests "no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTaskTest"  # Single class
./gradlew test --tests "*.BPMNToGrooveTaskTest.testSequentialTasks"                   # Single method
```

### server - Java Tests (JUnit 5 + Spring Boot Test)
```
./gradlew test                                                           # Run all
./gradlew test --tests "no.tk.rulegenerator.server.RuleGeneratorControllerTests"     # Single class
./gradlew test --tests "*.RuleGeneratorControllerTests.testCheckCTL"                 # Single method
```

## Code Style — TypeScript / Angular (generation-ui)

### Formatting
- **Prettier** is the only formatter (no ESLint). Config in `.prettierrc.json`:
  - Single quotes, tab width 4, LF line endings.
- Run `npm run format` before committing.

### Imports (order)
1. Angular core (`@angular/core`, `@angular/cdk/...`)
2. Angular Material (`@angular/material/...`)
3. Third-party libraries (`bpmn-js`, `file-saver-es`, `rxjs`)
4. Local relative imports (`../../services/...`, `../../components/...`)

### Component Patterns
- **Standalone components** — no NgModules. All dependencies listed in `imports: [...]` in `@Component`.
- Signal-based inputs: `public viewer = input(false)` (Angular 17+ style).
- Services injected via constructor with `private readonly`.
- SCSS stylesheets in separate files.
- `@Injectable({ providedIn: 'root' })` for singleton services.

### Naming Conventions
- Files: `kebab-case` (e.g., `analysis.component.ts`, `shared-state.service.ts`).
- Classes: `PascalCase` with suffix (`AnalysisComponent`, `SharedStateService`).
- Properties/methods: `camelCase`, no underscore prefix for private members.
- Booleans: descriptive names (e.g., `graphGrammarGenerationRunning`).

### Error Handling
- Subscribe with `{ error: (err) => { ... }, next: (data) => { ... } }`.
- Show user-facing errors via `MatSnackBar.open(message, 'close')`.
- Use `console.error()` for logging.

### Testing (Jasmine/Playwright)
- Unit test files co-located: `*.spec.ts` next to source files.
- E2E tests use Page Object pattern (`e2e/page-objects/`).
- Locators: prefer `getByTestId`, `getByRole`, `getByText`.
- API mocking via Playwright route interception (see `e2e/fixtures/`).

## Code Style — Java (generator & server)

### Formatting
- **Google Java Format** enforced by Spotless plugin (2-space indentation, ~100 char lines).
- **Error Prone** static analysis is enabled.
- Run `./gradlew spotlessApply` before committing.

### Imports
- Static imports first.
- Standard library, then third-party, then project-internal.
- Wildcard imports are acceptable for annotations (`org.springframework.web.bind.annotation.*`).

### Naming Conventions
- Packages: `no.tk.*` (e.g., `no.tk.groove.behaviortransformer.bpmn`).
- Classes: `PascalCase` (e.g., `BPMNToGrooveTransformer`, `BPMNModelChecker`).
- Constants: `UPPER_SNAKE_CASE` (e.g., `TYPE_GRAPH_FILE_NAME`).
- Methods: `camelCase`.
- Test classes: `*Test` suffix. Test methods: `test*` prefix.

### Language Features (Java 25)
- **Records** for DTOs: `public record ModelCheckingRequest(...)`.
- **Switch expressions** with arrow syntax.
- **Text blocks** for multi-line strings.
- **Streams** extensively for collection processing.
- Explicit types preferred over `var`.

### Error Handling
- Custom exceptions extend `RuntimeException` (e.g., `ModelCheckingException`).
- Global `@ControllerAdvice` exception handler (`RuntimeExceptionHandler`) returns structured JSON errors.
- SLF4J for logging (`LoggerFactory.getLogger(...)`).
- Checked exceptions declared on method signatures, not swallowed.

### Architecture (server)
- `@RestController` with `@PostMapping` endpoints.
- `@CrossOrigin(origins = "http://localhost:4200")` for local dev CORS.
- `@ModelAttribute` for multipart form binding.
- Controller delegates to helper/checker classes — keep controllers thin.

### Testing (JUnit 5)
- Abstract base classes for shared test setup (e.g., `BPMNToGrooveTestBase`).
- `@SpringBootTest(webEnvironment = RANDOM_PORT)` for integration tests.
- Test resources (`.bpmn` files) in `src/test/resources/`.
- Hamcrest matchers (`assertThat`) and JUnit assertions.
- Given/When/Then commenting style in test methods.

## CI/CD

CI runs on GitHub Actions. Key checks per sub-project:
- **generator:** `./gradlew check --info` + SonarCloud (`generator.yml`).
- **server:** `./gradlew check --info` + SonarCloud (`server.yml`).
- **UI:** Prettier check, Karma headless tests, Playwright E2E, SonarCloud (`ui.yml`).
- **Release:** Manual dispatch — builds bootJar, Docker image, deploys to Azure (`release.yml`).

Ensure all of the following pass before pushing:
```
# From generation-ui/
npm run check && npm test -- --watch=false --browsers=ChromeHeadless

# From generator/
./gradlew check

# From server/
./gradlew check
```
