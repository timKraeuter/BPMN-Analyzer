import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { RenamePropositionDialogComponent } from './rename-proposition-dialog.component';

describe('RenamePropositionDialogComponent', () => {
    let component: RenamePropositionDialogComponent;
    let fixture: ComponentFixture<RenamePropositionDialogComponent>;
    let mockDialogRef: jasmine.SpyObj<
        MatDialogRef<RenamePropositionDialogComponent>
    >;
    let dialogData: { proposition: { name: string; xml: string } };

    beforeEach(() => {
        mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);
        dialogData = {
            proposition: { name: 'test-proposition', xml: '<xml/>' },
        };

        TestBed.configureTestingModule({
            imports: [RenamePropositionDialogComponent],
            providers: [
                { provide: MatDialogRef, useValue: mockDialogRef },
                {
                    provide: MAT_DIALOG_DATA,
                    useValue: dialogData,
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

    it('should update proposition name and close dialog when saveNameAndCloseDialog is called', () => {
        component.newName = 'renamed-proposition';
        component.saveNameAndCloseDialog();

        expect(dialogData.proposition.name).toBe('renamed-proposition');
        expect(mockDialogRef.close).toHaveBeenCalled();
    });

    it('should call saveNameAndCloseDialog when onDialogClick is called (Enter key)', () => {
        spyOn(component, 'saveNameAndCloseDialog');
        component.onDialogClick();
        expect(component.saveNameAndCloseDialog).toHaveBeenCalled();
    });

    [
        { key: 'ArrowLeft', shouldStop: true },
        { key: 'ArrowRight', shouldStop: true },
        { key: 'Enter', shouldStop: false },
        { key: 'ArrowUp', shouldStop: false },
    ].forEach(({ key, shouldStop }) => {
        it(`should ${shouldStop ? '' : 'not '}stop propagation for ${key} key`, () => {
            const event = new KeyboardEvent('keydown', { key });
            spyOn(event, 'stopPropagation');

            component.stopEventPropagation(event);

            if (shouldStop) {
                expect(event.stopPropagation).toHaveBeenCalled();
            } else {
                expect(event.stopPropagation).not.toHaveBeenCalled();
            }
        });
    });
});
