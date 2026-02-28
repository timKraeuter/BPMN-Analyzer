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
 * Wait for the BPMN modeler canvas to be ready.
 * The app loads a default BPMN model on startup; wait for the canvas to appear.
 */
export async function waitForAppReady(page: Page) {
    await page.waitForSelector('.bjs-container', { timeout: 15_000 });
}
