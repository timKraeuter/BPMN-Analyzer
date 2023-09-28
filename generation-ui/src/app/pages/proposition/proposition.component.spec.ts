import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PropositionComponent } from './proposition.component';

describe('ProcessStateComponent', () => {
    let component: PropositionComponent;
    let fixture: ComponentFixture<PropositionComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [PropositionComponent],
        });
        fixture = TestBed.createComponent(PropositionComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
