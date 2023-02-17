import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { GenerationComponent } from './generation/generation.component';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { HttpClientModule } from '@angular/common/http';
import { DiagramComponent } from './diagram/diagram.component';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';
import { MatTabsModule } from '@angular/material/tabs';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { FormsModule } from '@angular/forms';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { TemporalLogicSyntaxComponent } from './temporal-logic-syntax/temporal-logic-syntax.component';
import { VerificationResultComponentComponent } from './verification-result-component/verification-result-component.component';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatListModule } from '@angular/material/list';
import { CTLResultSnackbarComponent } from './ctlresult-snackbar/ctlresult-snackbar.component';

@NgModule({
    declarations: [
        AppComponent,
        GenerationComponent,
        DiagramComponent,
        TemporalLogicSyntaxComponent,
        VerificationResultComponentComponent,
        CTLResultSnackbarComponent,
    ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        BrowserAnimationsModule,
        MatIconModule,
        MatButtonModule,
        MatCardModule,
        HttpClientModule,
        MatTooltipModule,
        MatDividerModule,
        MatTabsModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonToggleModule,
        FormsModule,
        MatSnackBarModule,
        MatProgressSpinnerModule,
        MatListModule,
    ],
    providers: [],
    bootstrap: [AppComponent],
})
export class AppModule {}
