import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LTlSyntaxComponent } from './ltl-syntax.component';

describe('LTlSyntaxComponent', () => {
  let component: LTlSyntaxComponent;
  let fixture: ComponentFixture<LTlSyntaxComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LTlSyntaxComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LTlSyntaxComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
