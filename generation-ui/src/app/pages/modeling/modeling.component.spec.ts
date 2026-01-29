import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModelingComponent } from './modeling.component';

describe('GenerationComponent', () => {
    let component: ModelingComponent;
    let fixture: ComponentFixture<ModelingComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [ModelingComponent],
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ModelingComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
