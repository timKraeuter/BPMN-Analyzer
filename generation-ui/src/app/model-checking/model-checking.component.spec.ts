import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModelCheckingComponent } from './model-checking.component';

describe('ModelCheckingComponent', () => {
    let component: ModelCheckingComponent;
    let fixture: ComponentFixture<ModelCheckingComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [ModelCheckingComponent],
        });
        fixture = TestBed.createComponent(ModelCheckingComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
