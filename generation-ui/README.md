# Generation UI

The Angular frontend for the BPMN Analyzer.

## Development server

Run `npm start` for a dev server at `http://localhost:4200/`. The application will automatically reload if you change any of the source files.

## Build

Run `npm run build` to build the project. The build artifacts are output to `server/src/main/resources/public/`.

## Running unit tests

Run `npm test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `npm run e2e` to execute the end-to-end tests via [Playwright](https://playwright.dev/).

## Code formatting

Run `npm run format` to auto-format with [Prettier](https://prettier.io/), or `npm run check` to verify formatting.

## Dependency maintenance

Run `npm outdated --include=dev` to check available versions and `npm audit` to check security advisories. Run `npm update` for updates within the declared version ranges. Keep Angular framework packages on the same patch version and TypeScript within `@angular/compiler-cli`'s supported peer dependency range.

Keep Jasmine on 6.x: Jasmine 7 makes test interface properties read-only, which breaks the current `zone.js/testing` integration.

The remaining moderate audit findings stem from `minimatch` in Karma and its coverage tooling. These tools require the older callable API, so overriding them to `minimatch` 11 is not compatible. Avoid `npm audit fix --force`: its proposed Angular build tooling and coverage downgrades are incompatible with this project. Resolving these findings requires an upstream compatible fix or a separate test-runner migration.

## Further help

See the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) for more information.
