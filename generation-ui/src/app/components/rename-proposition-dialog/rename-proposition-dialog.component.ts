import { Component, HostListener, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Proposition } from '../../services/shared-state.service';

@Component({
    selector: 'app-rename-proposition-dialog',
    templateUrl: './rename-proposition-dialog.component.html',
    styleUrls: ['./rename-proposition-dialog.component.scss'],
    standalone: false,
})
export class RenamePropositionDialogComponent {
    public newName: string;

    constructor(
        private readonly dialogRef: MatDialogRef<RenamePropositionDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: RenamePropositionDialogData,
    ) {
        this.newName = data.proposition.name;
    }

    closeDialog() {
        this.dialogRef.close();
    }

    @HostListener('document:keyup.Enter')
    onDialogClick(): void {
        this.saveNameAndCloseDialog();
    }

    saveNameAndCloseDialog() {
        this.data.proposition.name = this.newName;
        this.closeDialog();
    }

    stopEventPropagation($event: KeyboardEvent) {
        // Stops event propagation so steps are not changed while inputting.
        if ($event.key === 'ArrowLeft' || $event.key === 'ArrowRight') {
            $event.stopPropagation();
        }
    }
}

export interface RenamePropositionDialogData {
    proposition: Proposition;
}
