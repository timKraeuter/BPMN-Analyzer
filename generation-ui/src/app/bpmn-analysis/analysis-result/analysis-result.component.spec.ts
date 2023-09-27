import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AnalysisResultComponent } from './analysis-result.component';

describe('VerificationResultComponentComponent', () => {
    let component: AnalysisResultComponent;
    let fixture: ComponentFixture<AnalysisResultComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [AnalysisResultComponent],
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(AnalysisResultComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
