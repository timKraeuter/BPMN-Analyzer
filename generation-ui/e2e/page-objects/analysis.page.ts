import { type Locator, type Page, expect } from '@playwright/test';
import {
    bpmnPropertiesMixed,
    ctlPropertyInvalid,
    ctlPropertyError,
} from '../fixtures/mock-responses';
import { API_BASE } from '../fixtures/constants';

/**
 * Page object for Step 3 – Analysis.
 * Encapsulates BPMN property toggles, CTL input, check buttons,
 * result assertions, and the GT-system download button.
 */
export class AnalysisPage {
    readonly page: Page;

    // --- BPMN property toggles ---
    readonly safenessToggle: Locator;
    readonly optionToCompleteToggle: Locator;
    readonly properCompletionToggle: Locator;
    readonly noDeadActivitiesToggle: Locator;

    // --- Buttons ---
    readonly checkPropertiesBtn: Locator;
    readonly checkCtlBtn: Locator;
    readonly downloadGGBtn: Locator;
    readonly ggInfoBtn: Locator;

    // --- CTL ---
    readonly ctlTab: Locator;
    readonly ctlInput: Locator;
    readonly ctlResults: Locator;

    // --- BPMN results ---
    readonly bpmnPropertyResults: Locator;

    constructor(page: Page) {
        this.page = page;

        // Toggles – use getByRole to avoid matching tooltip text
        this.safenessToggle = page.getByRole('button', {
            name: 'Safeness',
            exact: true,
        });
        this.optionToCompleteToggle = page.getByRole('button', {
            name: 'Option to complete',
        });
        this.properCompletionToggle = page.getByRole('button', {
            name: 'Proper completion',
        });
        this.noDeadActivitiesToggle = page.getByRole('button', {
            name: 'No dead activities',
        });

        // Buttons
        this.checkPropertiesBtn = page.getByTestId('check-properties-btn');
        this.checkCtlBtn = page.getByTestId('check-ctl-btn');
        this.downloadGGBtn = page.getByTestId('download-gg-btn');
        this.ggInfoBtn = page.getByTestId('gg-info-btn');

        // CTL
        this.ctlTab = page.getByRole('tab', { name: 'CTL properties' });
        this.ctlInput = page.getByTestId('ctl-property-input');
        this.ctlResults = page.getByTestId('ctl-results');

        // BPMN results
        this.bpmnPropertyResults = page.getByTestId('bpmn-property-results');
    }

    // ----------------------------------------------------------------
    //  BPMN property helpers
    // ----------------------------------------------------------------

    /** Toggle all four BPMN property buttons on. */
    async selectAllProperties() {
        await this.safenessToggle.click();
        await this.optionToCompleteToggle.click();
        await this.properCompletionToggle.click();
        await this.noDeadActivitiesToggle.click();
    }

    /** Assert the BPMN property results list is visible with expected icon counts. */
    async expectBpmnResults(greenCount: number, redCount: number) {
        await expect(this.bpmnPropertyResults).toBeVisible();
        await expect(
            this.bpmnPropertyResults.locator('.color_green'),
        ).toHaveCount(greenCount);
        if (redCount > 0) {
            await expect(
                this.bpmnPropertyResults.locator('.color_red'),
            ).toHaveCount(redCount);
        }
    }

    /** Assert that specific property names appear in the results. */
    async expectBpmnPropertyNames(names: string[]) {
        for (const name of names) {
            await expect(
                this.bpmnPropertyResults.getByText(name),
            ).toBeVisible();
        }
    }

    // ----------------------------------------------------------------
    //  CTL helpers
    // ----------------------------------------------------------------

    /** Switch to the CTL properties tab. */
    async switchToCtlTab() {
        await this.ctlTab.click();
    }

    /** Enter a CTL formula and click the check button. */
    async checkCtlFormula(formula: string) {
        await this.ctlInput.fill(formula);
        await this.checkCtlBtn.click();
    }

    /** Assert the CTL result shows valid. */
    async expectCtlValid(formula: string) {
        await expect(this.ctlResults).toBeVisible();
        await expect(
            this.ctlResults.getByTestId('ctl-result-valid'),
        ).toBeVisible();
        await expect(this.ctlResults.getByText(formula)).toBeVisible();
    }

    /** Assert the CTL result shows invalid. */
    async expectCtlInvalid() {
        await expect(this.ctlResults).toBeVisible();
        await expect(
            this.ctlResults.getByTestId('ctl-result-invalid'),
        ).toBeVisible();
    }

    /** Assert the CTL result shows an error message. */
    async expectCtlError(errorText: string) {
        await expect(this.ctlResults).toBeVisible();
        await expect(this.ctlResults.getByText(errorText)).toBeVisible();
    }

    // ----------------------------------------------------------------
    //  Mock overrides (for per-test route overrides)
    // ----------------------------------------------------------------

    /** Override the BPMN properties endpoint to return mixed results. */
    async mockBpmnPropertiesMixed() {
        await this.page.route(
            `${API_BASE}checkBPMNSpecificProperties`,
            (route) => {
                route.fulfill({
                    status: 200,
                    contentType: 'application/json',
                    body: JSON.stringify(bpmnPropertiesMixed),
                });
            },
        );
    }

    /** Override the BPMN properties endpoint to return a server error. */
    async mockBpmnPropertiesServerError(message: string) {
        await this.page.route(
            `${API_BASE}checkBPMNSpecificProperties`,
            (route) => {
                route.fulfill({
                    status: 500,
                    contentType: 'application/json',
                    body: JSON.stringify({ message }),
                });
            },
        );
    }

    /** Override the CTL endpoint to return an invalid result. */
    async mockCtlInvalid() {
        await this.page.route(`${API_BASE}checkTemporalLogic`, (route) => {
            route.fulfill({
                status: 200,
                contentType: 'application/json',
                body: JSON.stringify(ctlPropertyInvalid),
            });
        });
    }

    /** Override the CTL endpoint to return an error result. */
    async mockCtlError() {
        await this.page.route(`${API_BASE}checkTemporalLogic`, (route) => {
            route.fulfill({
                status: 200,
                contentType: 'application/json',
                body: JSON.stringify(ctlPropertyError),
            });
        });
    }

    /** Override the CTL endpoint to return a server error. */
    async mockCtlServerError(message: string) {
        await this.page.route(`${API_BASE}checkTemporalLogic`, (route) => {
            route.fulfill({
                status: 500,
                contentType: 'application/json',
                body: JSON.stringify({ message }),
            });
        });
    }
}
