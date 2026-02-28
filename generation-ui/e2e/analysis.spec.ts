import { test, expect } from '@playwright/test';
import {
    setupApiMocks,
    navigateToAnalysisStep,
    waitForAppReady,
} from './fixtures/helpers';
import {
    bpmnPropertiesAllValid,
    bpmnPropertiesMixed,
    ctlPropertyValid,
    ctlPropertyInvalid,
    ctlPropertyError,
} from './fixtures/mock-responses';

test.describe('Step 3 - Analysis', () => {
    test.beforeEach(async ({ page }) => {
        await setupApiMocks(page);
        await page.goto('/');
        await waitForAppReady(page);
        await navigateToAnalysisStep(page);
    });

    test.describe('General BPMN property checking', () => {
        test('shows snackbar when no properties selected', async ({ page }) => {
            await page.getByTestId('check-properties-btn').click();

            // Snackbar should show the validation message
            await expect(
                page.getByText(
                    'Please select at least one property for verification.',
                ),
            ).toBeVisible();
        });

        test('check all properties - all valid', async ({ page }) => {
            // Select all 4 property toggles (use getByRole to avoid tooltip text ambiguity)
            await page
                .getByRole('button', { name: 'Safeness', exact: true })
                .click();
            await page
                .getByRole('button', { name: 'Option to complete' })
                .click();
            await page
                .getByRole('button', { name: 'Proper completion' })
                .click();
            await page
                .getByRole('button', { name: 'No dead activities' })
                .click();

            await page.getByTestId('check-properties-btn').click();

            // Wait for results to appear
            const resultsList = page.getByTestId('bpmn-property-results');
            await expect(resultsList).toBeVisible();

            // All 4 properties should show green check icons
            const checkIcons = resultsList.locator('.color_green');
            await expect(checkIcons).toHaveCount(4);

            // Verify property names are shown
            await expect(resultsList.getByText('Safeness')).toBeVisible();
            await expect(
                resultsList.getByText('Option to complete'),
            ).toBeVisible();
            await expect(
                resultsList.getByText('Proper completion'),
            ).toBeVisible();
            await expect(
                resultsList.getByText('No dead activities'),
            ).toBeVisible();
        });

        test('check properties - mixed results', async ({ page }) => {
            // Override mock for this test
            await page.route(
                'http://localhost:8080/checkBPMNSpecificProperties',
                (route) => {
                    route.fulfill({
                        status: 200,
                        contentType: 'application/json',
                        body: JSON.stringify(bpmnPropertiesMixed),
                    });
                },
            );

            await page
                .getByRole('button', { name: 'Safeness', exact: true })
                .click();
            await page
                .getByRole('button', { name: 'Option to complete' })
                .click();
            await page.getByTestId('check-properties-btn').click();

            const resultsList = page.getByTestId('bpmn-property-results');
            await expect(resultsList).toBeVisible();

            // One green check (Safeness) and one red X (Option to complete)
            await expect(resultsList.locator('.color_green')).toHaveCount(1);
            await expect(resultsList.locator('.color_red')).toHaveCount(1);
        });

        test('check single property', async ({ page }) => {
            await page
                .getByRole('button', { name: 'Safeness', exact: true })
                .click();
            await page.getByTestId('check-properties-btn').click();

            const resultsList = page.getByTestId('bpmn-property-results');
            await expect(resultsList).toBeVisible();
            await expect(resultsList.getByText('Safeness')).toBeVisible();
        });

        test('shows error snackbar on server error', async ({ page }) => {
            await page.route(
                'http://localhost:8080/checkBPMNSpecificProperties',
                (route) => {
                    route.fulfill({
                        status: 500,
                        contentType: 'application/json',
                        body: JSON.stringify({
                            message:
                                'State space generation timed out after 60 seconds.',
                        }),
                    });
                },
            );

            await page
                .getByRole('button', { name: 'Safeness', exact: true })
                .click();
            await page.getByTestId('check-properties-btn').click();

            await expect(
                page.getByText(
                    'State space generation timed out after 60 seconds.',
                ),
            ).toBeVisible();
        });
    });

    test.describe('CTL property checking', () => {
        test.beforeEach(async ({ page }) => {
            // Switch to the CTL properties tab
            await page.getByRole('tab', { name: 'CTL properties' }).click();
        });

        test('check valid CTL property', async ({ page }) => {
            await page.getByTestId('ctl-property-input').fill('AG(!Unsafe)');
            await page.getByTestId('check-ctl-btn').click();

            const ctlResults = page.getByTestId('ctl-results');
            await expect(ctlResults).toBeVisible();
            await expect(
                ctlResults.getByTestId('ctl-result-valid'),
            ).toBeVisible();
            await expect(ctlResults.getByText('AG(!Unsafe)')).toBeVisible();
        });

        test('check invalid CTL property', async ({ page }) => {
            await page.route(
                'http://localhost:8080/checkTemporalLogic',
                (route) => {
                    route.fulfill({
                        status: 200,
                        contentType: 'application/json',
                        body: JSON.stringify(ctlPropertyInvalid),
                    });
                },
            );

            await page
                .getByTestId('ctl-property-input')
                .fill('AF(AllTerminated)');
            await page.getByTestId('check-ctl-btn').click();

            const ctlResults = page.getByTestId('ctl-results');
            await expect(ctlResults).toBeVisible();
            await expect(
                ctlResults.getByTestId('ctl-result-invalid'),
            ).toBeVisible();
        });

        test('check CTL property with error message', async ({ page }) => {
            await page.route(
                'http://localhost:8080/checkTemporalLogic',
                (route) => {
                    route.fulfill({
                        status: 200,
                        contentType: 'application/json',
                        body: JSON.stringify(ctlPropertyError),
                    });
                },
            );

            await page
                .getByTestId('ctl-property-input')
                .fill('INVALID_FORMULA');
            await page.getByTestId('check-ctl-btn').click();

            const ctlResults = page.getByTestId('ctl-results');
            await expect(ctlResults).toBeVisible();
            await expect(
                ctlResults.getByText('Parse error in CTL formula'),
            ).toBeVisible();
        });

        test('shows error snackbar on CTL server error', async ({ page }) => {
            await page.route(
                'http://localhost:8080/checkTemporalLogic',
                (route) => {
                    route.fulfill({
                        status: 500,
                        contentType: 'application/json',
                        body: JSON.stringify({
                            message: 'Internal server error',
                        }),
                    });
                },
            );

            await page.getByTestId('ctl-property-input').fill('AG(!Unsafe)');
            await page.getByTestId('check-ctl-btn').click();

            await expect(page.getByText('Internal server error')).toBeVisible();
        });
    });

    test.describe('Download GT-system', () => {
        test('download triggers and completes without error', async ({
            page,
        }) => {
            // Listen for the download event
            const downloadPromise = page.waitForEvent('download');

            await page.getByTestId('download-gg-btn').click();

            // The download event should fire
            const download = await downloadPromise;
            expect(download.suggestedFilename()).toContain('.gps.zip');
        });

        test('shows info snackbar when info button clicked', async ({
            page,
        }) => {
            await page
                .getByRole('button', {
                    name: 'Graph transformation system download info button',
                })
                .click();

            await expect(
                page.getByText('Graph transformation systems are generated'),
            ).toBeVisible();
        });
    });
});
