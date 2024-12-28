import {ComponentFixture, TestBed} from '@angular/core/testing';
import { AppComponent } from './app.component';
import {ImageService} from './services/image.service';


describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  let imageServiceMock: jasmine.SpyObj<ImageService>;

  beforeEach(() => {
    imageServiceMock = jasmine.createSpyObj('ImageService', ['uploadImage', 'getAllImages']);

    TestBed.configureTestingModule({
      declarations: [AppComponent],
      providers: [
        { provide: ImageService, useValue: imageServiceMock }
      ]
    });

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the app', () => {
    expect(component).toBeTruthy();
  });
});
