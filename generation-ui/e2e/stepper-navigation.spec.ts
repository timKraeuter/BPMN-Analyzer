import { test, expect } from '@playwright/test';
import { setupApiMocks, waitForAppReady } from './fixtures/helpers';

test.describe('Stepper Navigation', () => {
    test.beforeEach(async ({ page }) => {
        await setupApiMocks(page);
        await page.goto('/');
        await waitForAppReady(page);
    });

    test('app loads on Step 1 by default', async ({ page }) => {
        const stepHeaders = page.locator('.mat-step-header');
        await expect(stepHeaders).toHaveCount(3);

        // Step 1 should be selected (aria-selected)
        await expect(stepHeaders.nth(0)).toHaveAttribute(
            'aria-selected',
            'true',
        );

        // The step labels should be visible
        await expect(page.getByText('Model your BPMN process')).toBeVisible();
        await expect(page.getByText('Add BPMN propositions')).toBeVisible();
        await expect(page.getByText('Analyze your BPMN process')).toBeVisible();
    });

    test('navigate forward with Next Step buttons', async ({ page }) => {
        const stepHeaders = page.locator('.mat-step-header');

        // Step 1 -> Step 2
        await page.getByTestId('step1-next-btn').click();
        await expect(stepHeaders.nth(1)).toHaveAttribute(
            'aria-selected',
            'true',
        );

        // Step 2 -> Step 3
        await page.getByTestId('step2-next-btn').click();
        await expect(stepHeaders.nth(2)).toHaveAttribute(
            'aria-selected',
            'true',
        );
    });

    test('navigate backward with Previous Step buttons', async ({ page }) => {
        const stepHeaders = page.locator('.mat-step-header');

        // Go to Step 3 first
        await page.getByTestId('step1-next-btn').click();
        await page.getByTestId('step2-next-btn').click();
        await expect(stepHeaders.nth(2)).toHaveAttribute(
            'aria-selected',
            'true',
        );

        // Step 3 -> Step 2
        await page.getByTestId('step3-prev-btn').click();
        await expect(stepHeaders.nth(1)).toHaveAttribute(
            'aria-selected',
            'true',
        );

        // Step 2 -> Step 1
        await page.getByTestId('step2-prev-btn').click();
        await expect(stepHeaders.nth(0)).toHaveAttribute(
            'aria-selected',
            'true',
        );
    });

    test('Back to the start resets to Step 1', async ({ page }) => {
        const stepHeaders = page.locator('.mat-step-header');

        // Go to Step 3
        await page.getByTestId('step1-next-btn').click();
        await page.getByTestId('step2-next-btn').click();
        await expect(stepHeaders.nth(2)).toHaveAttribute(
            'aria-selected',
            'true',
        );

        // Click "Back to the start"
        await page.getByTestId('back-to-start-btn').click();
        await expect(stepHeaders.nth(0)).toHaveAttribute(
            'aria-selected',
            'true',
        );
    });

    test('navigate by clicking step headers', async ({ page }) => {
        const stepHeaders = page.locator('.mat-step-header');

        // Jump to Step 3
        await stepHeaders.nth(2).click();
        await expect(stepHeaders.nth(2)).toHaveAttribute(
            'aria-selected',
            'true',
        );

        // Jump back to Step 1
        await stepHeaders.nth(0).click();
        await expect(stepHeaders.nth(0)).toHaveAttribute(
            'aria-selected',
            'true',
        );

        // Jump to Step 2
        await stepHeaders.nth(1).click();
        await expect(stepHeaders.nth(1)).toHaveAttribute(
            'aria-selected',
            'true',
        );
    });
});
