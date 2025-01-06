import {Component, OnInit} from '@angular/core';
import {Router, RouterOutlet} from '@angular/router';
import {SharedModule} from './shared/shared.module';
import {ImageService} from './services/image.service';


@Component({
  selector: 'app-root',
  imports: [RouterOutlet, SharedModule],
  templateUrl: './app.component.html',
  standalone: true,
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit{
  selectedFiles: File[] = [];

  searchQuery: string = '';
  searchResults: { url: string }[] = [];

  allImages: {url: string}[] = [];

  isLoading = false;

  constructor(private imageService: ImageService) {
  }

  onFileSelected(event: any) {
    this.selectedFiles = Array.from(event.target.files);
  }

  uploadImages(): void {
    if (!this.selectedFiles || this.selectedFiles.length === 0) {
      return;
    }

    this.isLoading = true;

    this.imageService.uploadImages(this.selectedFiles).subscribe({
      next: () => {
        alert('Images uploaded successfully');
        this.selectedFiles = [];
        this.isLoading = false;
        window.location.reload();
      },
      error: (err) => {
        console.log('Error: ', err);
        alert(`An error occurred: ${err.message}`);
        this.selectedFiles = [];
        this.isLoading = false;
      },
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
