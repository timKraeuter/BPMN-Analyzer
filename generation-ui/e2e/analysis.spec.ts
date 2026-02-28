import { test, expect } from '@playwright/test';
import { setupApiMocks, waitForAppReady } from './fixtures/helpers';
import { StepperPage } from './page-objects/stepper.page';
import { AnalysisPage } from './page-objects/analysis.page';

test.describe('Step 3 - Analysis', () => {
    let stepper: StepperPage;
    let analysis: AnalysisPage;

    test.beforeEach(async ({ page }) => {
        stepper = new StepperPage(page);
        analysis = new AnalysisPage(page);
        await setupApiMocks(page);
        await page.goto('/');
        await waitForAppReady(page);
        await stepper.goToAnalysisStep();
    });

    test.describe('General BPMN property checking', () => {
        test('shows snackbar when no properties selected', async ({ page }) => {
            await analysis.checkPropertiesBtn.click();

            // Snackbar should show the validation message
            await expect(
                page.getByText(
                    'Please select at least one property for verification.',
                ),
            ).toBeVisible();
        });

        test('check all properties - all valid', async () => {
            await analysis.selectAllProperties();
            await analysis.checkPropertiesBtn.click();

            await analysis.expectBpmnResults(4, 0);
            await analysis.expectBpmnPropertyNames([
                'Safeness',
                'Option to complete',
                'Proper completion',
                'No dead activities',
            ]);
        });

        test('check properties - mixed results', async () => {
            await analysis.mockBpmnPropertiesMixed();

            await analysis.safenessToggle.click();
            await analysis.optionToCompleteToggle.click();
            await analysis.checkPropertiesBtn.click();

            await analysis.expectBpmnResults(1, 1);
        });

        test('check single property', async () => {
            await analysis.safenessToggle.click();
            await analysis.checkPropertiesBtn.click();

            await expect(analysis.bpmnPropertyResults).toBeVisible();
            await expect(
                analysis.bpmnPropertyResults.getByText('Safeness'),
            ).toBeVisible();
        });

        test('shows error snackbar on server error', async ({ page }) => {
            await analysis.mockBpmnPropertiesServerError(
                'State space generation timed out after 60 seconds.',
            );

            await analysis.safenessToggle.click();
            await analysis.checkPropertiesBtn.click();

            await expect(
                page.getByText(
                    'State space generation timed out after 60 seconds.',
                ),
            ).toBeVisible();
        });
    });

    test.describe('CTL property checking', () => {
        test.beforeEach(async () => {
            await analysis.switchToCtlTab();
        });

        test('check valid CTL property', async () => {
            await analysis.checkCtlFormula('AG(!Unsafe)');
            await analysis.expectCtlValid('AG(!Unsafe)');
        });

        test('check invalid CTL property', async () => {
            await analysis.mockCtlInvalid();
            await analysis.checkCtlFormula('AF(AllTerminated)');
            await analysis.expectCtlInvalid();
        });

        test('check CTL property with error message', async () => {
            await analysis.mockCtlError();
            await analysis.checkCtlFormula('INVALID_FORMULA');
            await analysis.expectCtlError('Parse error in CTL formula');
        });

        test('shows error snackbar on CTL server error', async ({ page }) => {
            await analysis.mockCtlServerError('Internal server error');
            await analysis.checkCtlFormula('AG(!Unsafe)');

            await expect(page.getByText('Internal server error')).toBeVisible();
        });
    });

    test.describe('Download GT-system', () => {
        test('download triggers and completes without error', async ({
            page,
        }) => {
            const downloadPromise = page.waitForEvent('download');
            await analysis.downloadGGBtn.click();

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
