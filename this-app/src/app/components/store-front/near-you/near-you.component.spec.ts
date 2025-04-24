import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NearYouComponent } from './near-you.component';

describe('NearYouComponent', () => {
  let component: NearYouComponent;
  let fixture: ComponentFixture<NearYouComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NearYouComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(NearYouComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
