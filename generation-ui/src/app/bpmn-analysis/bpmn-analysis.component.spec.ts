import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BpmnAnalysisComponent } from './bpmn-analysis.component';

describe('ModelCheckingComponent', () => {
    let component: BpmnAnalysisComponent;
    let fixture: ComponentFixture<BpmnAnalysisComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [BpmnAnalysisComponent],
        });
        fixture = TestBed.createComponent(BpmnAnalysisComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
