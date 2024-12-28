import {Component, OnInit} from '@angular/core';
import {Router, RouterOutlet} from '@angular/router';
import {SharedModule} from './shared/shared.module';
import {ImageService} from './services/image.service';


@Component({
  selector: 'app-root',
  imports: [RouterOutlet, SharedModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit{
  selectedFile: File | null = null;

  searchQuery: string = '';
  searchResults: { url: string }[] = [];

  allImages: {url: string}[] = [];

  isLoading = false;

  constructor(private imageService: ImageService, private router: Router) {
  }

  onFileSelected(event: any) {
    this.selectedFile = event.target.files[0];
  }

  uploadImage() {
    this.isLoading = true;
    if (!this.selectedFile) return;

    this.imageService.uploadImage(this.selectedFile).subscribe({
      next: () => {
        alert('Image uploaded succssfully!');
        this.selectedFile = null;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error occurred while uploading image');
        if (err.error) {
          alert('Error occurred: ' + err.error); // err.error will contain the message sent from the backend
        } else {
          alert('An unexpected error occurred: ' + err.statusText);
        }
        this.selectedFile = null;
        this.isLoading = false;
      }
    })
  }

  searchImages() {
    if (!this.searchQuery.trim()) return;

    this.imageService.searchImages(this.searchQuery).subscribe({
      next: (results) => {
        this.searchResults = results
      },
      error: () => {
        alert('Error fetching search results.');
      }
    })
  }

  ngOnInit() {
    this.imageService.getAllImages().subscribe(
      (images) => {
        this.allImages = images;
      },
      (error) => {
        console.error('Error fetching images', error);
      }
    );
  }
}
