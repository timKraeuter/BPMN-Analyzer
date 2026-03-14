import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ChangeDetectorRef, NO_ERRORS_SCHEMA } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';

import { AnalysisComponent } from './analysis.component';
import { BPMNModelerService } from '../../services/bpmnmodeler.service';
import { DiagramComponent } from '../../components/diagram/diagram.component';
import { ModelCheckingService } from '../../services/model-checking.service';
import { ModelCheckingResponse } from '../../models/model-checking-response';
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
        [
            {
                templateIndex: 0,
                prop1: 'myProp',
                prop2: '',
                expected: 'AG(!myProp)',
            },
            {
                templateIndex: 1,
                prop1: 'goal',
                prop2: '',
                expected: 'EF(goal)',
            },
            {
                templateIndex: 2,
                prop1: 'done',
                prop2: '',
                expected: 'AF(done)',
            },
            {
                templateIndex: 3,
                prop1: 'start',
                prop2: 'end',
                expected: 'AG(start -> AF(end))',
            },
        ].forEach(({ templateIndex, prop1, prop2, expected }) => {
            it(`should create "${expected}" from template ${templateIndex}`, () => {
                component.selectedTemplate =
                    component.ctlTemplates[templateIndex];
                component.selectedProposition1 = prop1;
                component.selectedProposition2 = prop2;
                component.createCTLProperty();
                expect(component.ctlProperty).toBe(expected);
            });
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
        [
            {
                desc: 'no template selected',
                templateIndex: undefined as number | undefined,
                prop1: '',
                prop2: '',
                expected: false,
            },
            {
                desc: 'template selected but no proposition',
                templateIndex: 0,
                prop1: '',
                prop2: '',
                expected: false,
            },
            {
                desc: 'one-proposition template with proposition',
                templateIndex: 0,
                prop1: 'myProp',
                prop2: '',
                expected: true,
            },
            {
                desc: 'two-proposition template with only first',
                templateIndex: 3,
                prop1: 'start',
                prop2: '',
                expected: false,
            },
            {
                desc: 'two-proposition template with both',
                templateIndex: 3,
                prop1: 'start',
                prop2: 'end',
                expected: true,
            },
        ].forEach(({ desc, templateIndex, prop1, prop2, expected }) => {
            it(`should return ${expected} when ${desc}`, () => {
                component.selectedTemplate =
                    templateIndex === undefined
                        ? undefined
                        : component.ctlTemplates[templateIndex];
                component.selectedProposition1 = prop1;
                component.selectedProposition2 = prop2;
                if (expected) {
                    expect(
                        component.showCreateCTLPropertyButton(),
                    ).toBeTruthy();
                } else {
                    expect(component.showCreateCTLPropertyButton()).toBeFalsy();
                }
            });
        });
    });

    describe('stopEventPropagation', () => {
        [
            { key: 'ArrowLeft', shouldStop: true },
            { key: 'ArrowRight', shouldStop: true },
            { key: 'Enter', shouldStop: false },
            { key: 'ArrowUp', shouldStop: false },
        ].forEach(({ key, shouldStop }) => {
            it(`should ${shouldStop ? '' : 'not '}stop propagation for ${key}`, () => {
                const event = new KeyboardEvent('keydown', { key });
                spyOn(event, 'stopPropagation');
                component.stopEventPropagation(event);
                if (shouldStop) {
                    expect(event.stopPropagation).toHaveBeenCalled();
                } else {
                    expect(event.stopPropagation).not.toHaveBeenCalled();
                }
            });
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
            const mockResponse: ModelCheckingResponse = {
                property: 'AG(!Unsafe)',
                valid: true,
                error: '',
            };
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

    describe('info snackbars', () => {
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
    });

    describe('ctlTemplates', () => {
        it('should have four templates defined', () => {
            expect(component.ctlTemplates.length).toBe(4);
        });

        [
            { index: 0, description: 'AG(!proposition)', twoProps: false },
            { index: 1, description: 'EF(proposition)', twoProps: false },
            { index: 2, description: 'AF(proposition)', twoProps: false },
            { index: 3, description: 'AG(p1 -> AF(p2))', twoProps: true },
        ].forEach(({ index, description, twoProps }) => {
            it(`template ${index} (${description}) should have twoPropositions=${twoProps}`, () => {
                expect(component.ctlTemplates[index].twoPropositions).toBe(
                    twoProps,
                );
            });
        });
    });
});
