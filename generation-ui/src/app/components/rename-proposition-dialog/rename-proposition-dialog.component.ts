import { Component, HostListener, Inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
    MAT_DIALOG_DATA,
    MatDialogRef,
    MatDialogModule,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Proposition } from '../../services/shared-state.service';

@Component({
    selector: 'app-rename-proposition-dialog',
    templateUrl: './rename-proposition-dialog.component.html',
    styleUrls: ['./rename-proposition-dialog.component.scss'],
    imports: [
        FormsModule,
        MatDialogModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule,
    ],
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
