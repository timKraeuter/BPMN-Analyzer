import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TemporalLogicSyntaxComponent } from './temporal-logic-syntax.component';

describe('LTlSyntaxComponent', () => {
    let component: TemporalLogicSyntaxComponent;
    let fixture: ComponentFixture<TemporalLogicSyntaxComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [TemporalLogicSyntaxComponent],
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(TemporalLogicSyntaxComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
