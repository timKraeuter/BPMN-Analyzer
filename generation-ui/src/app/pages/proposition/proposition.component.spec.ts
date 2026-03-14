import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';

import { PropositionComponent } from './proposition.component';
import { BPMNModelerService } from '../../services/bpmnmodeler.service';
import { SharedStateService } from '../../services/shared-state.service';
import { TokenDiagramComponent } from '../../components/token-diagram/token-diagram.component';
import { RenamePropositionDialogComponent } from '../../components/rename-proposition-dialog/rename-proposition-dialog.component';

describe('PropositionComponent', () => {
    let component: PropositionComponent;
    let fixture: ComponentFixture<PropositionComponent>;
    let mockModelerService: jasmine.SpyObj<BPMNModelerService>;
    let mockSnackBar: jasmine.SpyObj<MatSnackBar>;
    let mockDialog: jasmine.SpyObj<MatDialog>;
    let sharedState: SharedStateService;
    let mockTokenModeler: any;

    beforeEach(() => {
        mockTokenModeler = {
            attachTo: jasmine.createSpy('attachTo'),
            destroy: jasmine.createSpy('destroy'),
            importXML: jasmine
                .createSpy('importXML')
                .and.returnValue(Promise.resolve({ warnings: [] })),
            saveSVG: jasmine
                .createSpy('saveSVG')
                .and.returnValue(Promise.resolve({ svg: '<svg/>' })),
        };

        mockModelerService = jasmine.createSpyObj('BPMNModelerService', [
            'getTokenModeler',
            'getModeler',
            'getViewer',
            'getBpmnXML',
            'getTokenXML',
            'getTokenModelXMLBlob',
            'updateTokenBPMNModelIfNeeded',
        ]);
        mockModelerService.getTokenModeler.and.returnValue(mockTokenModeler);
        mockModelerService.getModeler.and.returnValue({
            attachTo: jasmine.createSpy('attachTo'),
            destroy: jasmine.createSpy('destroy'),
            importXML: jasmine.createSpy('importXML'),
            get: jasmine.createSpy('get').and.returnValue({
                attachTo: jasmine.createSpy('propertiesPanelAttachTo'),
            }),
        } as any);
        mockModelerService.getViewer.and.returnValue({
            attachTo: jasmine.createSpy('attachTo'),
            destroy: jasmine.createSpy('destroy'),
        } as any);
        mockModelerService.getBpmnXML.and.returnValue(
            Promise.resolve('<bpmn/>'),
        );
        mockModelerService.getTokenXML.and.returnValue(
            Promise.resolve('<token/>'),
        );
        mockModelerService.getTokenModelXMLBlob.and.returnValue(
            Promise.resolve(new Blob(['<xml/>'])),
        );
        mockModelerService.updateTokenBPMNModelIfNeeded.and.returnValue(
            Promise.resolve(),
        );

        mockSnackBar = jasmine.createSpyObj('MatSnackBar', ['open']);
        mockDialog = jasmine.createSpyObj('MatDialog', ['open']);
        sharedState = new SharedStateService();

        TestBed.configureTestingModule({
            imports: [PropositionComponent],
            providers: [
                {
                    provide: BPMNModelerService,
                    useValue: mockModelerService,
                },
                { provide: SharedStateService, useValue: sharedState },
            ],
        });

        TestBed.overrideComponent(PropositionComponent, {
            remove: { imports: [TokenDiagramComponent] },
            add: { schemas: [NO_ERRORS_SCHEMA] },
        });

        TestBed.overrideProvider(MatSnackBar, { useValue: mockSnackBar });
        TestBed.overrideProvider(MatDialog, { useValue: mockDialog });

        fixture = TestBed.createComponent(PropositionComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize with one default proposition', () => {
        expect(component.propositions.length).toBe(1);
        expect(component.currentProposition.name).toBe('Proposition1');
    });

    describe('createNewProposition', () => {
        it('should add a new proposition and switch to it', async () => {
            await component.createNewProposition();

            expect(component.propositions.length).toBe(2);
            expect(component.currentProposition.name).toBe('newProposition');
            expect(mockModelerService.getBpmnXML).toHaveBeenCalled();
        });
    });

    describe('switchToProposition', () => {
        it('should switch to a different proposition', async () => {
            await component.createNewProposition();
            const firstProp = component.propositions[0];

            await component.switchToProposition(firstProp);

            expect(component.currentProposition).toBe(firstProp);
            expect(mockTokenModeler.importXML).toHaveBeenCalled();
        });

        it('should not switch when selecting current proposition', async () => {
            const current = component.currentProposition;
            mockModelerService.getTokenXML.calls.reset();

            await component.switchToProposition(current);

            expect(mockModelerService.getTokenXML).not.toHaveBeenCalled();
        });
    });

    describe('deleteProposition', () => {
        it('should show snackbar when trying to delete the last proposition', async () => {
            await component.deleteProposition(component.currentProposition);

            expect(mockSnackBar.open).toHaveBeenCalledWith(
                'There has to be at least one proposition.',
                'close',
                { duration: 5000 },
            );
            expect(component.propositions.length).toBe(1);
        });

        it('should delete a non-current proposition', async () => {
            await component.createNewProposition();
            const firstProp = component.propositions[0];
            const secondProp = component.propositions[1];

            await component.deleteProposition(firstProp);

            expect(component.propositions.length).toBe(1);
            expect(component.currentProposition).toBe(secondProp);
        });

        it('should delete the current proposition and switch to remaining', async () => {
            await component.createNewProposition();
            const secondProp = component.currentProposition;
            const firstProp = component.propositions[0];

            await component.deleteProposition(secondProp);

            expect(component.propositions.length).toBe(1);
            expect(component.currentProposition).toBe(firstProp);
        });
    });

    describe('editProposition', () => {
        it('should open rename dialog with proposition data', () => {
            const proposition = component.currentProposition;

            component.editProposition(proposition);

            expect(mockDialog.open).toHaveBeenCalledWith(
                RenamePropositionDialogComponent,
                { data: { proposition } },
            );
        });
    });

    describe('uploadTokenModel', () => {
        it('should import uploaded file and update proposition', async () => {
            const fileContent = '<token-xml/>';
            const file = new File([fileContent], 'myModel.bpmn', {
                type: 'text/xml',
            });
            const event = {
                target: { files: [file] },
            } as unknown as Event;

            await component.uploadTokenModel(event);

            expect(mockTokenModeler.importXML).toHaveBeenCalledWith(
                fileContent,
            );
            expect(component.currentProposition.xml).toBe(fileContent);
            expect(component.currentProposition.name).toBe('myModel');
        });
    });

    describe('downloadTokenModel', () => {
        it('should call getTokenModelXMLBlob', () => {
            component.downloadTokenModel();

            expect(mockModelerService.getTokenModelXMLBlob).toHaveBeenCalled();
        });
    });

    describe('downloadTokenModelSVG', () => {
        it('should call saveSVG on token modeler', () => {
            component.downloadTokenModelSVG();

            expect(mockTokenModeler.saveSVG).toHaveBeenCalled();
        });
    });

    describe('saveCurrentProposition', () => {
        it('should update token model when xml is empty and save xml', async () => {
            component.currentProposition.xml = '';

            await component.saveCurrentProposition();

            expect(
                mockModelerService.updateTokenBPMNModelIfNeeded,
            ).toHaveBeenCalled();
            expect(mockModelerService.getTokenXML).toHaveBeenCalled();
            expect(component.currentProposition.xml).toBe('<token/>');
        });

        it('should skip updateTokenBPMNModelIfNeeded when xml is not empty', async () => {
            component.currentProposition.xml = '<existing/>';

            await component.saveCurrentProposition();

            expect(
                mockModelerService.updateTokenBPMNModelIfNeeded,
            ).not.toHaveBeenCalled();
            expect(component.currentProposition.xml).toBe('<token/>');
        });
    });

    describe('propositionDown', () => {
        it('should navigate to next proposition on ArrowDown', async () => {
            await component.createNewProposition();
            const firstProp = component.propositions[0];
            await component.switchToProposition(firstProp);

            const event = {
                target: { classList: { contains: () => false } },
            } as unknown as Event;

            await component.propositionDown(event);

            expect(component.currentProposition).toBe(
                component.propositions[1],
            );
        });

        it('should not navigate when already at last proposition', async () => {
            const event = {
                target: { classList: { contains: () => false } },
            } as unknown as Event;
            const current = component.currentProposition;

            await component.propositionDown(event);

            expect(component.currentProposition).toBe(current);
        });

        it('should not navigate when target is properties panel input', async () => {
            await component.createNewProposition();
            const firstProp = component.propositions[0];
            await component.switchToProposition(firstProp);

            const event = {
                target: {
                    classList: {
                        contains: (cls: string) =>
                            cls === 'bio-properties-panel-input',
                    },
                },
            } as unknown as Event;

            await component.propositionDown(event);

            expect(component.currentProposition).toBe(firstProp);
        });
    });

    describe('propositionUp', () => {
        it('should navigate to previous proposition on ArrowUp', async () => {
            await component.createNewProposition();
            const firstProp = component.propositions[0];

            const event = {
                target: { classList: { contains: () => false } },
            } as unknown as Event;

            await component.propositionUp(event);

            expect(component.currentProposition).toBe(firstProp);
        });

        it('should not navigate when already at first proposition', async () => {
            await component.createNewProposition();
            const firstProp = component.propositions[0];
            await component.switchToProposition(firstProp);

            const event = {
                target: { classList: { contains: () => false } },
            } as unknown as Event;

            await component.propositionUp(event);

            expect(component.currentProposition).toBe(firstProp);
        });

        it('should not navigate when target is properties panel input', async () => {
            await component.createNewProposition();

            const event = {
                target: {
                    classList: {
                        contains: (cls: string) =>
                            cls === 'bio-properties-panel-input',
                    },
                },
            } as unknown as Event;
            const current = component.currentProposition;

            await component.propositionUp(event);

            expect(component.currentProposition).toBe(current);
        });
    });
});
