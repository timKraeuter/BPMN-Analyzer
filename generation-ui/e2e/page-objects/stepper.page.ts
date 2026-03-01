import { type Locator, type Page, expect } from '@playwright/test';

/**
 * Page object for the Material Stepper that drives navigation
 * across the three app steps: Modeling, Propositions, Analysis.
 */
export class StepperPage {
    readonly page: Page;

    /** All three step headers */
    readonly stepHeaders: Locator;

    /** "Next Step" buttons inside each step */
    readonly step1NextBtn: Locator;
    readonly step2NextBtn: Locator;

    /** "Previous Step" buttons */
    readonly step2PrevBtn: Locator;
    readonly step3PrevBtn: Locator;

    /** "Back to the start" button (visible on Step 3) */
    readonly backToStartBtn: Locator;

    constructor(page: Page) {
        this.page = page;
        this.stepHeaders = page.locator('.mat-step-header');
        this.step1NextBtn = page.getByTestId('step1-next-btn');
        this.step2NextBtn = page.getByTestId('step2-next-btn');
        this.step2PrevBtn = page.getByTestId('step2-prev-btn');
        this.step3PrevBtn = page.getByTestId('step3-prev-btn');
        this.backToStartBtn = page.getByTestId('back-to-start-btn');
    }

    /** Assert that a given step (0-indexed) is the currently selected one. */
    async expectStepSelected(stepIndex: number) {
        await expect(this.stepHeaders.nth(stepIndex)).toHaveAttribute(
            'aria-selected',
            'true',
        );
    }

    /** Click a step header to jump directly to that step. */
    async clickStepHeader(stepIndex: number) {
        await this.stepHeaders.nth(stepIndex).click();
    }

    /** Navigate forward from Step 1 to Step 2. */
    async goFromStep1ToStep2() {
        await this.step1NextBtn.click();
    }

    /** Navigate forward from Step 2 to Step 3. */
    async goFromStep2ToStep3() {
        await this.step2NextBtn.click();
    }

    /** Navigate from Step 1 all the way to Step 3 (Analysis). */
    async goToAnalysisStep() {
        await this.goFromStep1ToStep2();
        await this.goFromStep2ToStep3();
    }

    /** Click "Back to the start" to reset to Step 1. */
    async backToStart() {
        await this.backToStartBtn.click();
    }
}
