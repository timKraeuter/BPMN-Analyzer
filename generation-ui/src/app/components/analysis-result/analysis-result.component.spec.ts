import { ComponentFixture, TestBed } from '@angular/core/testing';

import {
    AnalysisResultComponent,
    BPMNProperty,
} from './analysis-result.component';

describe('AnalysisResultComponent', () => {
    let component: AnalysisResultComponent;
    let fixture: ComponentFixture<AnalysisResultComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [AnalysisResultComponent],
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

    it('should have running default to false', () => {
        expect(component.running()).toBeFalse();
    });

    it('should have empty properties by default', () => {
        expect(component.properties()).toEqual([]);
    });

    it('should have undefined ctlPropertyResult by default', () => {
        expect(component.ctlPropertyResult()).toBeUndefined();
    });
});

describe('BPMNProperty', () => {
    it('should construct with name and valid', () => {
        const prop = new BPMNProperty('Safeness', true);

        expect(prop.name).toBe('Safeness');
        expect(prop.valid).toBeTrue();
        expect(prop.additionalInfo).toBe('');
    });

    it('should construct with additionalInfo', () => {
        const prop = new BPMNProperty(
            'No dead activities',
            false,
            'Activity_1,Activity_2',
        );

        expect(prop.name).toBe('No dead activities');
        expect(prop.valid).toBeFalse();
        expect(prop.additionalInfo).toBe('Activity_1,Activity_2');
    });
});
