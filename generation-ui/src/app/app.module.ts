import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ModelingComponent } from './modeling/modeling.component';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { HttpClientModule } from '@angular/common/http';
import { DiagramComponent } from './modeling/diagram/diagram.component';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';
import { MatTabsModule } from '@angular/material/tabs';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { FormsModule } from '@angular/forms';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { TemporalLogicSyntaxComponent } from './analysis/temporal-logic-syntax/temporal-logic-syntax.component';
import { AnalysisResultComponent } from './analysis/analysis-result/analysis-result.component';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatListModule } from '@angular/material/list';
import { AnalysisComponent } from '."./analysis/analysis.component"import { MatStepperModule } from '@angular/material/stepper';
import { STEPPER_GLOBAL_OPTIONS } from '@angular/cdk/stepper';
import { ProcessStateComponent } from './process-state/process-state.component';

@NgModule({
    declarations: [
        AppComponent,
        ModelingComponent,
        DiagramComponent,
        TemporalLogicSyntaxComponent,
        AnalysisResultComponent,
      AnalysisComponent,
        ProcessStateComponent,
    ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        BrowserAnimationsModule,
        HttpClientModule,
        FormsModule,
        MatIconModule,
        MatButtonModule,
        MatCardModule,
        MatTooltipModule,
        MatDividerModule,
        MatTabsModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonToggleModule,
        MatSnackBarModule,
        MatProgressSpinnerModule,
        MatListModule,
        MatStepperModule,
    ],
    providers: [
        {
            provide: STEPPER_GLOBAL_OPTIONS,
            useValue: { displayDefaultIndicatorType: false },
        },
    ],
    bootstrap: [AppComponent],
})
export class AppModule {}
