import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Proposition } from '../../services/proposition.service';

@Component({
    selector: 'app-rename-proposition-dialog',
    templateUrl: './rename-proposition-dialog.component.html',
    styleUrls: ['./rename-proposition-dialog.component.scss'],
})
export class RenamePropositionDialogComponent {
    private readonly oldName: string;
    public newName: string;

    constructor(
        private dialogRef: MatDialogRef<RenamePropositionDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: RenamePropositionDialogData,
    ) {
        this.oldName = data.proposition.name;
        this.newName = data.proposition.name;
    }

    cancel() {
        this.data.proposition.name = this.oldName;
        this.dialogRef.close();
    }

    save() {
        this.data.proposition.name = this.newName;
        this.dialogRef.close();
    }
}

export interface RenamePropositionDialogData {
    proposition: Proposition;
}
