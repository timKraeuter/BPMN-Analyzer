import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RenamePropositionDialogComponent } from './rename-proposition-dialog.component';

describe('RenamePropositionDialogComponent', () => {
  let component: RenamePropositionDialogComponent;
  let fixture: ComponentFixture<RenamePropositionDialogComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [RenamePropositionDialogComponent]
    });
    fixture = TestBed.createComponent(RenamePropositionDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
