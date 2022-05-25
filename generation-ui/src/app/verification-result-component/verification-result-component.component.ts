import {Component, Input} from '@angular/core';

@Component({
  selector: 'verification-result-component',
  templateUrl: './verification-result-component.component.html',
  styleUrls: ['./verification-result-component.component.scss']
})
export class VerificationResultComponentComponent {
  @Input()
  public running!: boolean

  @Input()
  public properties!: BPMNProperty[];
}

export class BPMNProperty {
  public name: string;
  public holds: boolean;
  public additionalInfo: string;


  constructor(name: string, holds: boolean, additionalInfo: string = "") {
    this.name = name;
    this.holds = holds;
    this.additionalInfo = additionalInfo;
  }
}
