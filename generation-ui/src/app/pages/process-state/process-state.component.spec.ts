import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProcessStateComponent } from './process-state.component';

describe('ProcessStateComponent', () => {
    let component: ProcessStateComponent;
    let fixture: ComponentFixture<ProcessStateComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [ProcessStateComponent],
        });
        fixture = TestBed.createComponent(ProcessStateComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
