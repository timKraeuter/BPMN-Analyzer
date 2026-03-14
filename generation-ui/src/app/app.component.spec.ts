import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';

import { AppComponent } from './app.component';
import { BPMNModelerService } from './services/bpmnmodeler.service';
import { StepperSelectionEvent } from '@angular/cdk/stepper';

describe('AppComponent', () => {
    let component: AppComponent;
    let fixture: ComponentFixture<AppComponent>;
    let mockModelerService: jasmine.SpyObj<BPMNModelerService>;

    beforeEach(() => {
        mockModelerService = jasmine.createSpyObj('BPMNModelerService', [
            'updateTokenBPMNModelIfNeeded',
            'updateViewerBPMNModel',
            'getModeler',
            'getViewer',
        ]);
        mockModelerService.updateTokenBPMNModelIfNeeded.and.returnValue(
            Promise.resolve(),
        );
        mockModelerService.updateViewerBPMNModel.and.returnValue(
            Promise.resolve(),
        );

        TestBed.configureTestingModule({
            imports: [AppComponent],
            providers: [
                {
                    provide: BPMNModelerService,
                    useValue: mockModelerService,
                },
            ],
        });

        TestBed.overrideComponent(AppComponent, {
            set: {
                schemas: [NO_ERRORS_SCHEMA],
                imports: [],
                template: '',
            },
        });

        fixture = TestBed.createComponent(AppComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create the app', () => {
        expect(component).toBeTruthy();
    });

    describe('stepChanged', () => {
        it('should update token model when changing to proposition step', async () => {
            const event = { selectedIndex: 1 } as StepperSelectionEvent;

            await component.stepChanged(event);

            expect(
                mockModelerService.updateTokenBPMNModelIfNeeded,
            ).toHaveBeenCalled();
            expect(
                mockModelerService.updateViewerBPMNModel,
            ).not.toHaveBeenCalled();
        });

        it('should save proposition and update viewer when changing to analyze step', async () => {
            // Mock the propositionComponent ViewChild
            const mockPropositionComponent = {
                saveCurrentProposition: jasmine
                    .createSpy('saveCurrentProposition')
                    .and.returnValue(Promise.resolve()),
            };
            (component as any).propositionComponent = mockPropositionComponent;

            const event = { selectedIndex: 2 } as StepperSelectionEvent;

            await component.stepChanged(event);

            expect(
                mockPropositionComponent.saveCurrentProposition,
            ).toHaveBeenCalled();
            expect(mockModelerService.updateViewerBPMNModel).toHaveBeenCalled();
        });

        it('should not call anything for modeling step', async () => {
            const event = { selectedIndex: 0 } as StepperSelectionEvent;

            await component.stepChanged(event);

            expect(
                mockModelerService.updateTokenBPMNModelIfNeeded,
            ).not.toHaveBeenCalled();
            expect(
                mockModelerService.updateViewerBPMNModel,
            ).not.toHaveBeenCalled();
        });
    });

    describe('stepForward', () => {
        it('should call stepper.next when target is not properties panel input', async () => {
            const mockStepper = { next: jasmine.createSpy('next') };
            (component as any).stepper = mockStepper;

            const event = {
                target: document.createElement('div'),
            } as unknown as Event;

            await component.stepForward(event);

            expect(mockStepper.next).toHaveBeenCalled();
        });

        it('should not call stepper.next when target is properties panel input', async () => {
            const mockStepper = { next: jasmine.createSpy('next') };
            (component as any).stepper = mockStepper;

            const el = document.createElement('input');
            el.classList.add('bio-properties-panel-input');
            const event = { target: el } as unknown as Event;

            await component.stepForward(event);

            expect(mockStepper.next).not.toHaveBeenCalled();
        });
    });

    describe('stepBackward', () => {
        it('should call stepper.previous', async () => {
            const mockStepper = {
                previous: jasmine.createSpy('previous'),
            };
            (component as any).stepper = mockStepper;

            const event = new Event('keydown');

            await component.stepBackward(event);

            expect(mockStepper.previous).toHaveBeenCalled();
        });
    });
});
