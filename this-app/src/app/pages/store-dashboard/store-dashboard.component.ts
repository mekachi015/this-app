import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/authentication-service/auth.service';
import { Store } from "../../models/store-admin-models/store-admin/Store";
import { StoreAdminServiceService } from '../../services/store-admin-service/store-admin-service.service';
import { Router } from '@angular/router';
import { CreateProductDTO } from '../../models/store-admin-models/product-admin/CreateProductDTO';
import { ProductService } from '../../services/product-service/product.service';
import { Product } from '../../models/store-admin-models/product-admin/product';



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
  imports: [CommonModule, FormsModule],
  templateUrl: './store-dashboard.component.html',
  styleUrl: './store-dashboard.component.scss'
})
export class StoreDashboardComponent implements OnInit {
  // Example recent orders (static for now)
  recentOrders: Order[] = [
    { id: 'ORD-001', customer: 'John Doe', products: '2 items', total: 4999.99, status: 'Completed' },
    { id: 'ORD-002', customer: 'Jane Smith', products: '1 item', total: 2500.00, status: 'Pending' },
    { id: 'ORD-003', customer: 'Mike Johnson', products: '3 items', total: 7500.00, status: 'Cancelled' }
  ];

  // File upload properties
  selectedFile: File | null = null;
  previewUrl: string | ArrayBuffer | null = null;
  isDragOver = false;
  uploadContext: 'store' | 'product' = 'store';

  //New property for store logo upload
  selectedStoreFile : File | null = null;
  storePreviewUrl: string | ArrayBuffer | null = null;
  isStoreDragOver = false;

  // Store management properties
  isAdmin = false;
  stores: Store[] = [];
  products: Product[] = [];
  isLoading = false;
  errorMessage = '';
  editingStore: Store | null = null;
  selectedStore: Store | null = null;

  // Form visibility
  showStoreForm = false;
  showProductForm = false;

  // Store model for form binding
  storeModel: any = {
    storeName: '',
    storeDescription: '',
    storeAddress: '',
    storeEmail: '',
    storePhoneNumber: '',
    storeBusinessHours: '',
    storeLogo: '',
    ownerId: 0,
  };

  // Default models
  newStore: Store = {
    ownerId: undefined,
    storeName: '',
    storeDescription: '',
    storeAddress: '',
    storeEmail: '',
    storePhoneNumber: '',
    storeBusinessHours: '',
    storeLogo: '',
    createdAt: new Date(),
    updatedAt: new Date()
  };

  newProduct: CreateProductDTO = {
    productName: '',
    productDescription: '',
    productPrice: 0,
    stockQuantity: 0,
    category: '',
    storeId: 0,
    userId: 0
  };

  constructor(
    public authService: AuthService,
    private storeAdminService: StoreAdminServiceService,
    private productService: ProductService,
    private router: Router
  ) { }

  // ---------------------- Lifecycle ----------------------
  ngOnInit() {
    this.checkAdminStatus();

    if (!this.isAdmin) {
      this.errorMessage = 'Access denied. Admin privileges required.';
      return;
    }
    
    //set the owner Id for the store
    const user = this.authService.currentUserValue;
    if(user?.id){
      this.storeModel.ownerId = user.id;
    }

    this.loadStores();
  }

  navigateToProductManagement(store: Store): void {
     if (store.storeId) {
    this.router.navigate(['/product-management', store.storeId])
      .then(() => {
        console.log('Navigating to product management for store:', store.storeName);
        console.log('Navigating to product management for store:', store.storeId);

      })
      .catch(error => {
        console.error('Navigation error:', error);
        this.errorMessage = 'Failed to navigate to product management';
      });
  }
  }

  // ---------------------- Auth ----------------------
  checkAdminStatus() {
    const user = this.authService.currentUserValue;
    this.isAdmin = user?.userType === 'ADMIN';
  }

  // ---------------------- Store Management ----------------------
  loadStores(): void {
    this.isLoading = true;
    this.storeAdminService.getUserStores().subscribe({
      next: (stores) => {
        console.log('OwnerId is:', this.storeModel.ownerId);
        this.stores = stores;
        this.isLoading = false;
        // Automatically select first store if available
        if (this.stores.length > 0) {
          this.selectedStore = this.stores[0];
         // this.loadProducts();
        }

        console.log('Loaded stores:', stores);
      },
      error: () => {
        this.errorMessage = 'Failed to load stores. Please try again later.';
        this.isLoading = false;
      }
    });
  }

  createStore(): void {
    this.createStoreWithLogo();
    console.log('Creating store with data:', this.storeModel);
  }

  createStoreWithLogo(): void {
    // Validate required fields
  if (!this.storeModel.storeName || !this.storeModel.storeDescription) {
    this.errorMessage = 'Store name and description are required';
    return;
  }

  // Validate that all required fields are filled
  if (!this.storeModel.storeAddress || !this.storeModel.storeEmail || 
      !this.storeModel.storePhoneNumber || !this.storeModel.storeBusinessHours) {
    this.errorMessage = 'All store fields are required';
    return;
  }

  console.log('Creating store with data:', this.storeModel);
  console.log('Selected logo file:', this.selectedStoreFile);

  this.isLoading = true;

  // Pass storeModel and selectedStoreFile (can be null)
  this.storeAdminService.createStoreWithLogo(
    this.storeModel,
    this.selectedStoreFile  // This can be null - backend handles it
  ).subscribe({
    next: (store) => {
      console.log('Store created successfully:', store);
      this.stores.push(store);
      this.resetStoreForm();
      this.isLoading = false;
      alert('Store created successfully!');
      this.loadStores();
    },
    error: (error) => {
      console.error('Store creation failed:', error);
      console.error('Error status:', error.status);
      console.error('Error body:', error.error);
      
      // More detailed error message
      let errorMsg = 'Failed to create store: ';
      if (error.error && typeof error.error === 'string') {
        errorMsg += error.error;
      } else if (error.message) {
        errorMsg += error.message;
      } else {
        errorMsg += 'Unknown error occurred';
      }
      
      this.errorMessage = errorMsg;
      this.isLoading = false;
    }
  });
  }


  editStore(store: Store): void {
    this.editingStore = store;
    this.storeModel = this.storeAdminService.toComponentStore(store);
    this.showStoreForm = true;
    this.uploadContext = 'store';
  }

  updateStore(): void {
    if (!this.editingStore?.storeId) return;

    this.isLoading = true;
    const storeDto = this.storeAdminService.toStoreDTO(this.storeModel);

    this.storeAdminService.updateStore(Number(this.editingStore.storeId), storeDto).subscribe({
      next: (updatedStore) => {
        const index = this.stores.findIndex(s => s.storeId === updatedStore.storeId);
        if (index !== -1) this.stores[index] = updatedStore;
        this.resetStoreForm();
        this.isLoading = false;
        alert('Store updated successfully!');
      },
      error: () => {
        this.errorMessage = 'Failed to update store.';
        this.isLoading = false;
      }
    });
  }

  deleteStore(storeId: number): void {
    if (!confirm('Are you sure you want to delete this store?')) return;
    this.isLoading = true;

    this.storeAdminService.deleteStore(storeId).subscribe({
      next: () => {
        this.stores = this.stores.filter(s => s.storeId !== storeId);
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to delete store.';
        this.isLoading = false;
      }
    });
  }

  resetStoreForm(): void {
  const user = this.authService.currentUserValue;
  
  this.storeModel = {
    storeName: '',
    storeDescription: '',
    storeAddress: '',
    storeEmail: '',
    storePhoneNumber: '',
    storeBusinessHours: '',
    storeLogo: '',
    ownerId: user?.id || 0
  };
  
  this.editingStore = null;
  this.showStoreForm = false;
  this.resetStoreUpload();  // Clear the file upload
}

  // Add method to handle store selection
  onStoreSelected(store: Store): void {
    this.selectedStore = store;
   
 }

   createProduct(): void {
  
  }



  handleProductCreated(): void {
   // this.loadProducts();
    this.resetProductForm();
    alert('Product created successfully!');
    this.isLoading = false;
  }


  resetProductForm(): void {
    this.newProduct = {
      productName: '',
      productDescription: '',
      productPrice: 0,
      stockQuantity: 0,
      category: '',
      storeId: this.selectedStore?.storeId ?? 0,
      userId: 0
    };
    this.showProductForm = false;
    this.resetUpload();
  }

  selectStoreForProduct(store: Store): void {
    this.selectedStore = store;
    this.showProductForm = true;
    this.uploadContext = 'product';
  }

  // ---------------------- Upload Logic ----------------------
  onStoreFileSelected(event: any): void {
    const file = event.target.files[0];
    this.handleStoreFileSelection(file);
  }

  onStoreDrop(event: DragEvent): void {
    event.preventDefault();
    this.isStoreDragOver = false;
    if (event.dataTransfer?.files.length) {
      this.handleStoreFileSelection(event.dataTransfer.files[0]);
    }
  }

  onStoreDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isStoreDragOver = true;
  }

  handleStoreFileSelection(file: File): void {
    if (file && this.isImageFile(file)) {
      this.selectedStoreFile = file;
      const reader = new FileReader();
      reader.onload = () => (this.storePreviewUrl = reader.result);
      reader.readAsDataURL(file);
    } else {
      this.errorMessage = 'Please select a valid image file (JPEG or PNG).';
      this.resetStoreUpload();
    }
  }

  isImageFile(file: File): boolean {
    return file.type === 'image/jpeg' || file.type === 'image/png';
  }

  uploadFile(): void {
    if (this.selectedFile && this.uploadContext === 'store' && this.editingStore?.storeId) {
      this.uploadStoreLogo(Number(this.editingStore.storeId));
    } else if (this.uploadContext === 'product') {
      this.newProduct.imageUrl = this.previewUrl as string;
      alert('Product image set successfully!');
      this.resetUpload();
    }
  }

  public uploadStoreLogo(storeId: number): void {
    this.isLoading = true;
    this.storeAdminService.uploadStoreLogo(storeId, this.selectedStoreFile!).subscribe({
      next: () => {
        alert('Store logo uploaded successfully!');
        this.resetStoreUpload();
        this.loadStores();
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to upload logo.';
        this.isLoading = false;
      }
    });
  }

  resetStoreUpload(): void {
  this.selectedStoreFile = null;
  this.storePreviewUrl = null;
  this.isStoreDragOver = false;
}

  // UploadStoreLogo(): void {
  //   if (this.selectedFile && this.editingStore?.storeId) {
  //      this.uploadStoreLogo(Number(this.editingStore.storeId));
  //   }
  // }
  resetUpload(): void {
    this.selectedFile = null;
    this.previewUrl = null;
    this.isDragOver = false;
  }
}