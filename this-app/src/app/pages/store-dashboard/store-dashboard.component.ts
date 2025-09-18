import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

interface Order {
  id: string;
  customer: string;
  products: string;
  total: number;
  status: 'Completed' | 'Pending' | 'Cancelled';
}

@Component({
  selector: 'app-store-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './store-dashboard.component.html',
  styleUrl: './store-dashboard.component.scss'
})
export class StoreDashboardComponent {
  recentOrders: Order[] = [
    {
      id: 'ORD-001',
      customer: 'John Doe',
      products: '2 items',
      total: 4999.99,
      status: 'Completed'
    },
    {
      id: 'ORD-002',
      customer: 'Jane Smith',
      products: '1 item',
      total: 2500.00,
      status: 'Pending'
    },
    {
      id: 'ORD-003',
      customer: 'Mike Johnson',
      products: '3 items',
      total: 7500.00,
      status: 'Cancelled'
    }
  ];

  selectedFile: File | null = null;
previewUrl: string | ArrayBuffer | null = null;
isDragOver = false;

// Add these methods to your component class
onFileSelected(event: any): void {
  const file = event.target.files[0];
  this.handleFileSelection(file);
}

onDrop(event: DragEvent): void {
  event.preventDefault();
  this.isDragOver = false;
  
  if (event.dataTransfer?.files) {
    const file = event.dataTransfer.files[0];
    this.handleFileSelection(file);
  }
}

onDragOver(event: DragEvent): void {
  event.preventDefault();
  this.isDragOver = true;
}

handleFileSelection(file: File): void {
  if (file && this.isImageFile(file)) {
    this.selectedFile = file;
    
    // Create preview
    const reader = new FileReader();
    reader.onload = () => {
      this.previewUrl = reader.result;
    };
    reader.readAsDataURL(file);
  } else {
    // Handle invalid file type
    console.error('Please select a valid image file (JPEG or PNG)');
  }
}

isImageFile(file: File): boolean {
  return file.type === 'image/jpeg' || file.type === 'image/png';
}

uploadFile(): void {
  if (this.selectedFile) {
    // Here you would typically send the file to your server
    console.log('Uploading file:', this.selectedFile.name);
    
    // Simulate upload process
    setTimeout(() => {
      alert('Upload completed successfully!');
      this.resetUpload();
    }, 1500);
  }
}

resetUpload(): void {
  this.selectedFile = null;
  this.previewUrl = null;
  this.isDragOver = false;
}
}