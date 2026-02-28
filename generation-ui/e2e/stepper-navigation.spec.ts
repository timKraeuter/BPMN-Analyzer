import { test, expect } from '@playwright/test';
import { setupApiMocks, waitForAppReady } from './fixtures/helpers';
import { StepperPage } from './page-objects/stepper.page';

test.describe('Stepper Navigation', () => {
    let stepper: StepperPage;

    test.beforeEach(async ({ page }) => {
        stepper = new StepperPage(page);
        await setupApiMocks(page);
        await page.goto('/');
        await waitForAppReady(page);
    });

    test('app loads on Step 1 by default', async ({ page }) => {
        await expect(stepper.stepHeaders).toHaveCount(3);
        await stepper.expectStepSelected(0);

        // The step labels should be visible
        await expect(page.getByText('Model your BPMN process')).toBeVisible();
        await expect(page.getByText('Add BPMN propositions')).toBeVisible();
        await expect(page.getByText('Analyze your BPMN process')).toBeVisible();
    });

    test('navigate forward with Next Step buttons', async () => {
        // Step 1 -> Step 2
        await stepper.goFromStep1ToStep2();
        await stepper.expectStepSelected(1);

        // Step 2 -> Step 3
        await stepper.goFromStep2ToStep3();
        await stepper.expectStepSelected(2);
    });

    test('navigate backward with Previous Step buttons', async () => {
        // Go to Step 3 first
        await stepper.goToAnalysisStep();
        await stepper.expectStepSelected(2);

        // Step 3 -> Step 2
        await stepper.step3PrevBtn.click();
        await stepper.expectStepSelected(1);

        // Step 2 -> Step 1
        await stepper.step2PrevBtn.click();
        await stepper.expectStepSelected(0);
    });

    test('Back to the start resets to Step 1', async () => {
        // Go to Step 3
        await stepper.goToAnalysisStep();
        await stepper.expectStepSelected(2);

        // Click "Back to the start"
        await stepper.backToStart();
        await stepper.expectStepSelected(0);
    });

    test('navigate by clicking step headers', async () => {
        // Jump to Step 3
        await stepper.clickStepHeader(2);
        await stepper.expectStepSelected(2);

        // Jump back to Step 1
        await stepper.clickStepHeader(0);
        await stepper.expectStepSelected(0);

        // Jump to Step 2
        await stepper.clickStepHeader(1);
        await stepper.expectStepSelected(1);
    });
});
