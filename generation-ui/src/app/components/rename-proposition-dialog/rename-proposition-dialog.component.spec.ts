import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { RenamePropositionDialogComponent } from './rename-proposition-dialog.component';

describe('RenamePropositionDialogComponent', () => {
    let component: RenamePropositionDialogComponent;
    let fixture: ComponentFixture<RenamePropositionDialogComponent>;
    let mockDialogRef: jasmine.SpyObj<
        MatDialogRef<RenamePropositionDialogComponent>
    >;

    beforeEach(() => {
        mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);

        TestBed.configureTestingModule({
            imports: [RenamePropositionDialogComponent],
            providers: [
                { provide: MatDialogRef, useValue: mockDialogRef },
                {
                    provide: MAT_DIALOG_DATA,
                    useValue: { proposition: { name: 'test-proposition' } },
                },
            ],
        });
        fixture = TestBed.createComponent(RenamePropositionDialogComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize with proposition name', () => {
        expect(component.newName).toBe('test-proposition');
    });

    it('should close dialog when closeDialog is called', () => {
        component.closeDialog();
        expect(mockDialogRef.close).toHaveBeenCalled();
    });
});
