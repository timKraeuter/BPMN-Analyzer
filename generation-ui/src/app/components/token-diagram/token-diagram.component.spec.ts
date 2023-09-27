import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TokenDiagramComponent } from './token-diagram.component';

describe('TokenDiagramComponent', () => {
  let component: TokenDiagramComponent;
  let fixture: ComponentFixture<TokenDiagramComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TokenDiagramComponent]
    });
    fixture = TestBed.createComponent(TokenDiagramComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
