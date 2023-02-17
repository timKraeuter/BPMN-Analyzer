import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CTLResultSnackbarComponent } from './ctlresult-snackbar.component';

describe('CTLResultSnackbarComponent', () => {
    let component: CTLResultSnackbarComponent;
    let fixture: ComponentFixture<CTLResultSnackbarComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [CTLResultSnackbarComponent],
        }).compileComponents();

        fixture = TestBed.createComponent(CTLResultSnackbarComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
