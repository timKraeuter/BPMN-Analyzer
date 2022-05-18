import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VerificationResultComponentComponent } from './verification-result-component.component';

describe('VerificationResultComponentComponent', () => {
  let component: VerificationResultComponentComponent;
  let fixture: ComponentFixture<VerificationResultComponentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VerificationResultComponentComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VerificationResultComponentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});


