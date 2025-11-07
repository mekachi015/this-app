import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/authentication-service/auth.service';
import {Store} from "../../models/store-admin-models/store-admin/Store";
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
  ownerId: null
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
    category: ''
  };

  constructor(
    public authService: AuthService,
    private storeAdminService: StoreAdminServiceService,
    private productService: ProductService,
    private router: Router
  ) {}

  // ---------------------- Lifecycle ----------------------
  ngOnInit() {
    this.checkAdminStatus();

    if (!this.isAdmin) {
      this.errorMessage = 'Access denied. Admin privileges required.';
      return;
    }

    this.loadStores();
  }

  navigateToProductManagement(store: Store): void {
    if (store.storeId){
      this.router.navigate(['/product-management', store.storeId]);
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
        this.stores = stores;
        this.isLoading = false;
        // Automatically select first store if available
        if (this.stores.length > 0) {
          this.selectedStore = this.stores[0];
          this.loadProducts();
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
  }

  createStoreWithLogo(): void {
  if (!this.storeModel.storeName) {
    this.errorMessage = 'Store name is required';
    return;
  }

  this.isLoading = true;

   const logoFile = this.selectedFile || new File([""], "empty.png", { type: "image/png" });

  // Use the simple method that takes individual form fields
  this.storeAdminService.createStoreWithLogo(
    this.storeModel, 
    logoFile
  ).subscribe({
    next: (store) => {
      this.stores.push(store);
      this.resetStoreForm();
      this.isLoading = false;
      alert('Store created successfully!');
      this.loadStores(); // Reload stores list
    },
    error: (error) => {
      this.errorMessage = 'Failed to create store: ' + error.message;
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
    this.storeModel = {
      storeName: '',
      storeDescription: '',
      storeAddress: '',
      storeEmail: '',
      logoUrl: ''
    };
    this.editingStore = null;
    this.showStoreForm = false;
  }

  // Add method to handle store selection
  onStoreSelected(store: Store): void {
    this.selectedStore = store;
    this.loadProducts();
  }

  // ---------------------- Product Management ----------------------
  loadProducts(): void {
    if (!this.selectedStore?.storeId) return;
    this.isLoading = true;

    this.productService.getStoreProducts(Number(this.selectedStore.storeId)).subscribe({
      next: (products) => {
        this.products = products;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load products.';
        this.isLoading = false;
      }
    });
  }

  createProduct(): void {
    if (!this.selectedStore?.storeId) return;

    this.isLoading = true;
    this.productService.createProduct(Number(this.selectedStore.storeId), this.newProduct).subscribe({
      next: (product) => {
        if (this.selectedFile) {
          this.uploadProductImage(Number(product.productId));
        } else {
          this.handleProductCreated();
        }
      },
      error: () => {
        this.errorMessage = 'Failed to create product.';
        this.isLoading = false;
      }
    });
  }

  private uploadProductImage(productId: number): void {
    if (!this.selectedFile) return;

    this.productService.uploadProductImage(productId, this.selectedFile).subscribe({
      next: () => this.handleProductCreated(),
      error: () => {
        this.errorMessage = 'Product created but image upload failed.';
        this.handleProductCreated();
      }
    });
  }

  handleProductCreated(): void {
    this.loadProducts();
    this.resetProductForm();
    alert('Product created successfully!');
    this.isLoading = false;
  }

  deleteProduct(productId: number): void {
    if (!confirm('Are you sure you want to delete this product?')) return;

    this.isLoading = true;
    this.productService.deleteProduct(productId).subscribe({
      next: () => {
        this.products = this.products.filter(p => p.productId !== productId);
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to delete product.';
        this.isLoading = false;
      }
    });
  }

  resetProductForm(): void {
    this.newProduct = {
      productName: '',
      productDescription: '',
      productPrice: 0,
      stockQuantity: 0,
      category: ''
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
  onFileSelected(event: any): void {
    const file = event.target.files[0];
    this.handleFileSelection(file);
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver = false;
    if (event.dataTransfer?.files.length) {
      this.handleFileSelection(event.dataTransfer.files[0]);
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver = true;
  }

  handleFileSelection(file: File): void {
    if (file && this.isImageFile(file)) {
      this.selectedFile = file;
      const reader = new FileReader();
      reader.onload = () => (this.previewUrl = reader.result);
      reader.readAsDataURL(file);
    } else {
      this.errorMessage = 'Please select a valid image file (JPEG or PNG).';
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

  private uploadStoreLogo(storeId: number): void {
    this.isLoading = true;
    this.storeAdminService.uploadStoreLogo(storeId, this.selectedFile!).subscribe({
      next: () => {
        alert('Store logo uploaded successfully!');
        this.resetUpload();
        this.loadStores();
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to upload logo.';
        this.isLoading = false;
      }
    });
  }

  resetUpload(): void {
    this.selectedFile = null;
    this.previewUrl = null;
    this.isDragOver = false;
  }
}