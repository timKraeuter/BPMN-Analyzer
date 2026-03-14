import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ChangeDetectorRef, NO_ERRORS_SCHEMA } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';

import { AnalysisComponent } from './analysis.component';
import { BPMNModelerService } from '../../services/bpmnmodeler.service';
import { DiagramComponent } from '../../components/diagram/diagram.component';
import {
    ModelCheckingResponse,
    ModelCheckingService,
} from '../../services/model-checking.service';
import { SharedStateService } from '../../services/shared-state.service';

describe('AnalysisComponent', () => {
    let component: AnalysisComponent;
    let fixture: ComponentFixture<AnalysisComponent>;
    let mockBpmnModeler: jasmine.SpyObj<BPMNModelerService>;
    let mockSnackBar: jasmine.SpyObj<MatSnackBar>;
    let mockModelCheckingService: jasmine.SpyObj<ModelCheckingService>;
    let mockSharedState: SharedStateService;
    let mockCdr: jasmine.SpyObj<ChangeDetectorRef>;

    beforeEach(() => {
        const mockModeler = {
            attachTo: jasmine.createSpy('attachTo'),
            destroy: jasmine.createSpy('destroy'),
            importXML: jasmine.createSpy('importXML'),
            get: jasmine.createSpy('get').and.returnValue({
                attachTo: jasmine.createSpy('propertiesPanelAttachTo'),
            }),
        };

        mockBpmnModeler = jasmine.createSpyObj('BPMNModelerService', [
            'getBPMNModelXMLBlob',
            'getModeler',
            'getViewer',
            'updateViewerBPMNModel',
        ]);
        mockBpmnModeler.getModeler.and.returnValue(mockModeler as any);
        mockBpmnModeler.getViewer.and.returnValue(mockModeler as any);
        mockSnackBar = jasmine.createSpyObj('MatSnackBar', [
            'open',
            'openFromComponent',
        ]);
        mockModelCheckingService = jasmine.createSpyObj(
            'ModelCheckingService',
            ['downloadGG', 'checkBPMNSpecificProperties', 'checkTemporalLogic'],
        );
        mockSharedState = new SharedStateService();
        mockCdr = jasmine.createSpyObj('ChangeDetectorRef', ['detectChanges']);

        TestBed.configureTestingModule({
            imports: [AnalysisComponent],
            providers: [
                {
                    provide: BPMNModelerService,
                    useValue: mockBpmnModeler,
                },
                {
                    provide: ModelCheckingService,
                    useValue: mockModelCheckingService,
                },
                {
                    provide: SharedStateService,
                    useValue: mockSharedState,
                },
                { provide: ChangeDetectorRef, useValue: mockCdr },
            ],
        });

        TestBed.overrideComponent(AnalysisComponent, {
            remove: { imports: [DiagramComponent] },
            add: { schemas: [NO_ERRORS_SCHEMA] },
        });

        TestBed.overrideProvider(MatSnackBar, { useValue: mockSnackBar });

        fixture = TestBed.createComponent(AnalysisComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    describe('getPropositions', () => {
        it('should return proposition names from shared state', () => {
            mockSharedState.propositions = [
                { name: 'p1', xml: '' },
                { name: 'p2', xml: '' },
            ];
            expect(component.getPropositions()).toEqual(['p1', 'p2']);
        });

        it('should return empty array when no propositions', () => {
            expect(component.getPropositions()).toEqual([]);
        });
    });

    describe('getPropositionsNames', () => {
        it('should join proposition names with comma', () => {
            mockSharedState.propositions = [
                { name: 'alpha', xml: '' },
                { name: 'beta', xml: '' },
            ];
            expect(component.getPropositionsNames()).toBe('alpha, beta');
        });

        it('should return empty string when no propositions', () => {
            expect(component.getPropositionsNames()).toBe('');
        });
    });

    describe('createCTLProperty', () => {
        it('should create CTL property from one-proposition template', () => {
            component.selectedTemplate = component.ctlTemplates[0]; // AG(!proposition)
            component.selectedProposition1 = 'myProp';
            component.createCTLProperty();
            expect(component.ctlProperty).toBe('AG(!myProp)');
        });

        it('should create CTL property from two-proposition template', () => {
            component.selectedTemplate = component.ctlTemplates[3]; // AG(p1 -> AF(p2))
            component.selectedProposition1 = 'start';
            component.selectedProposition2 = 'end';
            component.createCTLProperty();
            expect(component.ctlProperty).toBe('AG(start -> AF(end))');
        });

        it('should create EF template correctly', () => {
            component.selectedTemplate = component.ctlTemplates[1]; // EF(proposition)
            component.selectedProposition1 = 'goal';
            component.createCTLProperty();
            expect(component.ctlProperty).toBe('EF(goal)');
        });

        it('should create AF template correctly', () => {
            component.selectedTemplate = component.ctlTemplates[2]; // AF(proposition)
            component.selectedProposition1 = 'done';
            component.createCTLProperty();
            expect(component.ctlProperty).toBe('AF(done)');
        });

        it('should not create property when no template selected', () => {
            component.selectedTemplate = undefined;
            component.selectedProposition1 = 'myProp';
            component.createCTLProperty();
            expect(component.ctlProperty).toBe('');
        });

        it('should not create property when no proposition selected', () => {
            component.selectedTemplate = component.ctlTemplates[0];
            component.selectedProposition1 = '';
            component.createCTLProperty();
            expect(component.ctlProperty).toBe('');
        });
    });

    describe('showCreateCTLPropertyButton', () => {
        it('should return false when no template selected', () => {
            component.selectedTemplate = undefined;
            expect(component.showCreateCTLPropertyButton()).toBeFalsy();
        });

        it('should return false when template selected but no proposition', () => {
            component.selectedTemplate = component.ctlTemplates[0];
            component.selectedProposition1 = '';
            expect(component.showCreateCTLPropertyButton()).toBeFalsy();
        });

        it('should return true for one-proposition template with proposition selected', () => {
            component.selectedTemplate = component.ctlTemplates[0];
            component.selectedProposition1 = 'myProp';
            expect(component.showCreateCTLPropertyButton()).toBeTruthy();
        });

        it('should return false for two-proposition template with only first proposition', () => {
            component.selectedTemplate = component.ctlTemplates[3];
            component.selectedProposition1 = 'start';
            component.selectedProposition2 = '';
            expect(component.showCreateCTLPropertyButton()).toBeFalse();
        });

        it('should return true for two-proposition template with both propositions', () => {
            component.selectedTemplate = component.ctlTemplates[3];
            component.selectedProposition1 = 'start';
            component.selectedProposition2 = 'end';
            expect(component.showCreateCTLPropertyButton()).toBeTrue();
        });
    });

    describe('stopEventPropagation', () => {
        it('should stop propagation for ArrowLeft', () => {
            const event = new KeyboardEvent('keydown', { key: 'ArrowLeft' });
            spyOn(event, 'stopPropagation');
            component.stopEventPropagation(event);
            expect(event.stopPropagation).toHaveBeenCalled();
        });

        it('should stop propagation for ArrowRight', () => {
            const event = new KeyboardEvent('keydown', { key: 'ArrowRight' });
            spyOn(event, 'stopPropagation');
            component.stopEventPropagation(event);
            expect(event.stopPropagation).toHaveBeenCalled();
        });

        it('should not stop propagation for other keys', () => {
            const event = new KeyboardEvent('keydown', { key: 'Enter' });
            spyOn(event, 'stopPropagation');
            component.stopEventPropagation(event);
            expect(event.stopPropagation).not.toHaveBeenCalled();
        });
    });

    describe('checkBPMNSpecificPropertiesClicked', () => {
        it('should show snackbar when no properties selected', async () => {
            component.bpmnSpecificPropertiesToBeChecked = [];
            await component.checkBPMNSpecificPropertiesClicked();
            expect(mockSnackBar.open).toHaveBeenCalledWith(
                'Please select at least one property for verification.',
                'close',
                { duration: 5000 },
            );
        });

        it('should call service and set results on success', async () => {
            component.bpmnSpecificPropertiesToBeChecked = ['Safeness'];
            mockBpmnModeler.getBPMNModelXMLBlob.and.returnValue(
                Promise.resolve(new Blob(['<xml/>'])),
            );
            mockModelCheckingService.checkBPMNSpecificProperties.and.returnValue(
                of({
                    propertyCheckingResults: [
                        { name: 'Safeness', valid: true },
                    ],
                }),
            );

            await component.checkBPMNSpecificPropertiesClicked();

            expect(
                mockModelCheckingService.checkBPMNSpecificProperties,
            ).toHaveBeenCalled();
            expect(component.bpmnPropertyCheckingResults.length).toBe(1);
            expect(component.bpmnPropertyCheckingResults[0].name).toBe(
                'Safeness',
            );
            expect(component.bpmnPropertyCheckingResults[0].valid).toBeTrue();
            expect(component.bpmnSpecificVerificationRunning).toBeFalse();
        });

        it('should handle error and show snackbar', async () => {
            component.bpmnSpecificPropertiesToBeChecked = ['Safeness'];
            mockBpmnModeler.getBPMNModelXMLBlob.and.returnValue(
                Promise.resolve(new Blob(['<xml/>'])),
            );
            mockModelCheckingService.checkBPMNSpecificProperties.and.returnValue(
                throwError(() => ({
                    error: { message: 'Server error' },
                })),
            );

            await component.checkBPMNSpecificPropertiesClicked();

            expect(mockSnackBar.open).toHaveBeenCalledWith(
                'Server error',
                'close',
            );
            expect(component.bpmnPropertyCheckingResults).toEqual([]);
            expect(component.bpmnSpecificVerificationRunning).toBeFalse();
        });
    });

    describe('checkCTLPropertyClicked', () => {
        it('should call service and set result on success', async () => {
            const mockResponse = new ModelCheckingResponse(
                'AG(!Unsafe)',
                true,
                '',
            );
            mockBpmnModeler.getBPMNModelXMLBlob.and.returnValue(
                Promise.resolve(new Blob(['<xml/>'])),
            );
            mockModelCheckingService.checkTemporalLogic.and.returnValue(
                of(mockResponse),
            );
            component.ctlProperty = 'AG(!Unsafe)';

            await component.checkCTLPropertyClicked();

            expect(
                mockModelCheckingService.checkTemporalLogic,
            ).toHaveBeenCalled();
            expect(component.ctlPropertyResult).toEqual(mockResponse);
            expect(component.bpmnSpecificVerificationRunning).toBeFalse();
        });

        it('should handle error and show snackbar', async () => {
            mockBpmnModeler.getBPMNModelXMLBlob.and.returnValue(
                Promise.resolve(new Blob(['<xml/>'])),
            );
            mockModelCheckingService.checkTemporalLogic.and.returnValue(
                throwError(() => ({
                    error: { message: 'CTL error' },
                })),
            );

            await component.checkCTLPropertyClicked();

            expect(mockSnackBar.open).toHaveBeenCalledWith(
                'CTL error',
                'close',
            );
            expect(component.bpmnSpecificVerificationRunning).toBeFalse();
        });
    });

    describe('info and LTL snackbars', () => {
        it('should show GG info snackbar', () => {
            component.ggInfoClicked();
            expect(mockSnackBar.open).toHaveBeenCalledWith(
                jasmine.stringContaining('Groove'),
                'close',
            );
        });

        it('should show temporal logic info snackbar', () => {
            component.temporalLogicInfoClicked();
            expect(mockSnackBar.openFromComponent).toHaveBeenCalled();
        });

        it('should show LTL not implemented snackbar', () => {
            component.checkLTLPropertyClicked();
            expect(mockSnackBar.open).toHaveBeenCalledWith(
                jasmine.stringContaining('LTL'),
                'close',
                { duration: 5000 },
            );
        });
    });

    describe('ctlTemplates', () => {
        it('should have four templates defined', () => {
            expect(component.ctlTemplates.length).toBe(4);
        });

        it('should have correct two-proposition flags', () => {
            expect(component.ctlTemplates[0].twoPropositions).toBeFalse();
            expect(component.ctlTemplates[1].twoPropositions).toBeFalse();
            expect(component.ctlTemplates[2].twoPropositions).toBeFalse();
            expect(component.ctlTemplates[3].twoPropositions).toBeTrue();
        });
    });
});
