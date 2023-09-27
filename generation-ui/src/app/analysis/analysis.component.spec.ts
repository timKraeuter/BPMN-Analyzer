import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AnalysisComponent } from './analysis.component';

describe('ModelCheckingComponent', () => {
    let component: AnalysisComponent;
    let fixture: ComponentFixture<AnalysisComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [AnalysisComponent],
        });
        fixture = TestBed.createComponent(AnalysisComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
