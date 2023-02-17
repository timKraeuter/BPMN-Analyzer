import { Component, Inject } from '@angular/core';
import {
    MAT_SNACK_BAR_DATA,
    MatSnackBarRef,
} from '@angular/material/snack-bar';
import { ModelCheckingResponse } from '../services/groove.service';

@Component({
    selector: 'app-ctlresult-snackbar',
    templateUrl: './ctlresult-snackbar.component.html',
    styleUrls: ['./ctlresult-snackbar.component.scss'],
})
export class CTLResultSnackbarComponent {
    public result: ModelCheckingResponse;
    constructor(
        public snackBarRef: MatSnackBarRef<CTLResultSnackbarComponent>,
        @Inject(MAT_SNACK_BAR_DATA) public data: any
    ) {
        this.result = data.result;
    }
}
