import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DiagramComponent } from './diagram.component';
import { BPMNModelerService } from '../../services/bpmnmodeler.service';

describe('DiagramComponent', () => {
    let fixture: ComponentFixture<DiagramComponent>;
    let component: DiagramComponent;
    let mockModelerService: jasmine.SpyObj<BPMNModelerService>;
    let mockModeler: any;

    beforeEach(async () => {
        mockModeler = {
            attachTo: jasmine.createSpy('attachTo'),
            destroy: jasmine.createSpy('destroy'),
            importXML: jasmine.createSpy('importXML'),
            get: jasmine.createSpy('get').and.returnValue({
                attachTo: jasmine.createSpy('propertiesPanelAttachTo'),
            }),
        };

        mockModelerService = jasmine.createSpyObj('BPMNModelerService', [
            'getModeler',
            'getViewer',
        ]);
        mockModelerService.getModeler.and.returnValue(mockModeler);
        mockModelerService.getViewer.and.returnValue(mockModeler);

        await TestBed.configureTestingModule({
            imports: [DiagramComponent],
            providers: [
                { provide: BPMNModelerService, useValue: mockModelerService },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(DiagramComponent);
        component = fixture.componentInstance;
    });

    afterEach(() => {
        fixture.destroy();
    });

    it('should create', () => {
        fixture.detectChanges();
        expect(component).toBeTruthy();
    });

    it('should use modeler when viewer is false', () => {
        component.viewer = false;
        fixture.detectChanges();

        expect(mockModelerService.getModeler).toHaveBeenCalled();
        expect(mockModeler.importXML).toHaveBeenCalled();
    });

    it('should use viewer when viewer is true', () => {
        component.viewer = true;
        fixture.detectChanges();

        expect(mockModelerService.getViewer).toHaveBeenCalled();
    });

    it('should attach modeler to element after content init', () => {
        fixture.detectChanges();

        expect(mockModeler.attachTo).toHaveBeenCalled();
    });

    it('should destroy modeler on component destroy', () => {
        fixture.detectChanges();
        component.ngOnDestroy();

        expect(mockModeler.destroy).toHaveBeenCalled();
    });

    it('should attach properties panel when propertiesPanel is true', () => {
        component.propertiesPanel = true;
        fixture.detectChanges();

        expect(mockModeler.get).toHaveBeenCalledWith('propertiesPanel');
    });
});
