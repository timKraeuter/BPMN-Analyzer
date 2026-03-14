import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';

import { ModelingComponent } from './modeling.component';
import { BPMNModelerService } from '../../services/bpmnmodeler.service';
import { SharedStateService } from '../../services/shared-state.service';
import { DiagramComponent } from '../../components/diagram/diagram.component';

describe('ModelingComponent', () => {
    let component: ModelingComponent;
    let fixture: ComponentFixture<ModelingComponent>;
    let mockModelerService: jasmine.SpyObj<BPMNModelerService>;
    let sharedState: SharedStateService;
    let mockModeler: any;

    beforeEach(() => {
        mockModeler = {
            attachTo: jasmine.createSpy('attachTo'),
            destroy: jasmine.createSpy('destroy'),
            importXML: jasmine
                .createSpy('importXML')
                .and.returnValue(Promise.resolve({ warnings: [] })),
            saveSVG: jasmine
                .createSpy('saveSVG')
                .and.returnValue(Promise.resolve({ svg: '<svg/>' })),
            get: jasmine.createSpy('get').and.returnValue({
                attachTo: jasmine.createSpy('propertiesPanelAttachTo'),
            }),
        };

        mockModelerService = jasmine.createSpyObj('BPMNModelerService', [
            'getBPMNModelXMLBlob',
            'getModeler',
            'getViewer',
        ]);
        mockModelerService.getModeler.and.returnValue(mockModeler);
        mockModelerService.getViewer.and.returnValue(mockModeler);
        mockModelerService.getBPMNModelXMLBlob.and.returnValue(
            Promise.resolve(new Blob(['<xml/>'])),
        );

        sharedState = new SharedStateService();

        TestBed.configureTestingModule({
            imports: [ModelingComponent],
            providers: [
                {
                    provide: BPMNModelerService,
                    useValue: mockModelerService,
                },
                { provide: SharedStateService, useValue: sharedState },
            ],
        });

        TestBed.overrideComponent(ModelingComponent, {
            remove: { imports: [DiagramComponent] },
            add: { schemas: [NO_ERRORS_SCHEMA] },
        });

        fixture = TestBed.createComponent(ModelingComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    describe('downloadBPMN', () => {
        it('should call getBPMNModelXMLBlob', () => {
            component.downloadBPMN();

            expect(mockModelerService.getBPMNModelXMLBlob).toHaveBeenCalled();
        });
    });

    describe('uploadBPMN', () => {
        it('should import uploaded file and update model file name', async () => {
            const fileContent = '<definitions/>';
            const file = new File([fileContent], 'myProcess.bpmn', {
                type: 'text/xml',
            });
            const event = {
                target: { files: [file] },
            } as unknown as Event;

            await component.uploadBPMN(event);

            expect(mockModeler.importXML).toHaveBeenCalledWith(fileContent);
            expect(sharedState.modelFileName).toBe('myProcess');
        });
    });

    describe('downloadSVG', () => {
        it('should call saveSVG on modeler', () => {
            component.downloadSVG();

            expect(mockModeler.saveSVG).toHaveBeenCalled();
        });
    });
});
