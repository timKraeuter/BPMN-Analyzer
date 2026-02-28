import { Page } from '@playwright/test';
import {
    bpmnPropertiesAllValid,
    ctlPropertyValid,
    fakeZipBuffer,
} from './mock-responses';

const API_BASE = 'http://localhost:8080/';

/**
 * Intercept all backend API requests and return canned responses.
 * Individual tests can override specific routes after calling this.
 */
export async function setupApiMocks(
    page: Page,
    overrides: {
        bpmnProperties?: object;
        ctlProperty?: object;
        generateGG?: ArrayBuffer;
    } = {},
) {
    await page.route(`${API_BASE}checkBPMNSpecificProperties`, (route) => {
        route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify(
                overrides.bpmnProperties ?? bpmnPropertiesAllValid,
            ),
        });
    });

    await page.route(`${API_BASE}checkTemporalLogic`, (route) => {
        route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify(overrides.ctlProperty ?? ctlPropertyValid),
        });
    });

    await page.route(`${API_BASE}generateGGAndZip`, (route) => {
        route.fulfill({
            status: 200,
            contentType: 'application/zip',
            body: Buffer.from(overrides.generateGG ?? fakeZipBuffer()),
        });
    });
}

/**
 * Navigate to a specific step (0-indexed) by clicking the step header.
 * Waits for the step content to become visible.
 */
export async function navigateToStep(page: Page, stepIndex: number) {
    const stepHeaders = page.locator('.mat-step-header');
    await stepHeaders.nth(stepIndex).click();
    // Wait for the step content to be visible
    await page
        .locator(`.mat-horizontal-stepper-content`)
        .nth(stepIndex)
        .waitFor({ state: 'visible' });
}

/**
 * Navigate to Step 3 (Analysis) by clicking through "Next Step" buttons.
 * This ensures all step-change side effects fire properly.
 */
export async function navigateToAnalysisStep(page: Page) {
    await page.getByTestId('step1-next-btn').click();
    await page.getByTestId('step2-next-btn').click();
}

/**
 * Wait for the BPMN modeler canvas to be ready.
 * The app loads a default BPMN model on startup; wait for the canvas to appear.
 */
export async function waitForAppReady(page: Page) {
    await page.waitForSelector('.bjs-container', { timeout: 15_000 });
}
