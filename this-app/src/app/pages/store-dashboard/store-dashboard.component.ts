  
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/authentication-service/auth.service';
import { Store } from '../../models/store-admin-models/store-admin/Store';
import { StoreAdminServiceService } from '../../services/store-admin-service/store-admin-service.service';
import { Router } from '@angular/router';
import { CreateProductDTO } from '../../models/store-admin-models/product-admin/CreateProductDTO';
import { ProductService } from '../../services/product-service/product.service';
import { Product } from '../../models/store-admin-models/product-admin/product';

import Swal from 'sweetalert2';

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
  styleUrl: './store-dashboard.component.scss',
})
export class StoreDashboardComponent implements OnInit {

  // --- Photon address autocomplete ---
  addressSuggestions: { label: string; lat: number; lng: number }[] = [];
  private addressDebounce: any;
  addressConfirmed = false;

  // --- Product creation loader ---
  isCreatingProduct = false;
  daysOfWeek: string[] = [
    'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'
  ];
  // Example recent orders (static for now)
  recentOrders: any[] = [];
  orderCount: number = 0;

  //date and time
  selectedDays: string[] = [];
  openTime: string = '07:00';
  closeTime: string = '18:00';

  totalProducts: number = 0;
  totalStores: number = 0;

  // File upload properties
  selectedFile: File | null = null;
  previewUrl: string | ArrayBuffer | null = null;
  isDragOver = false;
  uploadContext: 'store' | 'product' = 'store';

  //New property for store logo upload
  selectedStoreFile: File | null = null;
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
    startDay: 'Monday',
    endDay: 'Friday',
    startTime: '08:00',
    endTime: '17:00',
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
    updatedAt: new Date(),
  };

  newProduct: CreateProductDTO = {
    productName: '',
    productDescription: '',
    productPrice: 0,
    stockQuantity: 0,
    category: '',
    storeId: 0,
    userId: 0,
  };

  constructor(
    public authService: AuthService,
    private storeAdminService: StoreAdminServiceService,
    private productService: ProductService,
    private router: Router,
    private http: HttpClient,
  ) {}

  // ---------------------- Address Autocomplete ----------------------
  onAddressInput(query: string): void {
    this.addressConfirmed = false;
    clearTimeout(this.addressDebounce);
    if (!query || query.length < 4) { this.addressSuggestions = []; return; }
    this.addressDebounce = setTimeout(() => {
      const url = `https://photon.komoot.io/api/?q=${encodeURIComponent(query + ' South Africa')}&limit=5`;
      this.http.get<any>(url).subscribe({
        next: (res) => {
          this.addressSuggestions = (res.features || []).map((f: any) => {
            const p = f.properties;
            const parts = [
              p.housenumber,
              p.street,
              p.suburb ?? p.district,
              p.city ?? p.county,
              p.state,
              p.postcode
            ].filter(Boolean).join(', ');
            return {
              label: parts || p.name || 'Unknown',
              lat: f.geometry.coordinates[1],
              lng: f.geometry.coordinates[0]
            };
          });
        },
        error: () => this.addressSuggestions = []
      });
    }, 350);
  }

  selectAddress(suggestion: { label: string; lat: number; lng: number }): void {
    this.storeModel.storeAddress = suggestion.label;
    this.addressSuggestions = [];
    this.addressConfirmed = true;
  }

  clearAddressSuggestions(): void {
    setTimeout(() => this.addressSuggestions = [], 200);
  }

  // ---------------------- Lifecycle ----------------------
  ngOnInit() {
    this.checkAdminStatus();

    if (!this.isAdmin) {
      this.errorMessage = 'Access denied. Admin privileges required.';
      return;
    }

    const user = this.authService.currentUserValue;
    if (user?.id) {
      this.storeModel.ownerId = user.id;
      this.loadOrderCount(Number(user.id));
    }

    this.loadStores();
    this.loadAllOrdersByUser();
  }

  navigateToProductManagement(store: Store): void {
    if (store.storeId) {
      this.router
        .navigate(['/product-management', store.storeId])
        .then(() => {})
        .catch((error) => {
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
        this.stores = stores;
        this.isLoading = false;
        this.loadStoresCount();

        // Auto-select first store and load its data
        if (this.stores.length > 0) {
          this.selectedStore = this.stores[0];
          this.loadOrdersByStore(this.stores[0].storeId!);
          this.loadProductsCount(this.stores[0].storeId!);
        }
      },
      error: () => {
        this.errorMessage = 'Failed to load stores. Please try again later.';
        this.isLoading = false;
      },
    });
  }

  createStore(): void {
    this.createStoreWithLogo();
    // ...existing code...
  }

  createStoreWithLogo(): void {
    // Validate required fields
    if (!this.storeModel.storeName || !this.storeModel.storeDescription) {
      this.errorMessage = 'Store name and description are required';
      return;
    }

    // Validate that all required fields are filled
    if (
      !this.storeModel.storeAddress ||
      !this.storeModel.storeEmail ||
      !this.storeModel.storePhoneNumber ||
      !this.storeModel.storeBusinessHours
    ) {
      this.errorMessage = 'All store fields are required';
      return;
    }

    this.isLoading = true;

    // Pass storeModel and selectedStoreFile (can be null)
    this.storeAdminService
      .createStoreWithLogo(
        this.storeModel,
        this.selectedStoreFile, // This can be null - backend handles it
      )
      .subscribe({
        next: (store) => {
          this.stores.push(store);
          this.resetStoreForm();
          this.isLoading = false;
          Swal.fire({
            icon: 'success',
            title: 'Store created successfully!',
          });
          this.loadStores();
        },
        error: (error) => {
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
          Swal.fire({
            icon: 'error',
            title: 'Store creation failed',
            text: errorMsg,
          });
        },
      });
  }

  editStore(store: Store): void {
    this.editingStore = store;
    this.storeModel = { ...store }; // Create a copy of the store data
    this.showStoreForm = true;
    this.uploadContext = 'store';

    // If you have a logo, set the preview
    if (store.storeLogo) {
      this.storePreviewUrl = store.storeLogo;
    }

    // ...existing code...
  }

  updateStore(): void {
    if (!this.editingStore?.storeId) return;

    this.isLoading = true;
    const storeDto = this.storeAdminService.toStoreDTO(this.storeModel);

    this.storeAdminService
      .updateStore(Number(this.editingStore.storeId), storeDto)
      .subscribe({
        next: (updatedStore) => {
          const index = this.stores.findIndex(
            (s) => s.storeId === updatedStore.storeId,
          );
          if (index !== -1) this.stores[index] = updatedStore;
          this.resetStoreForm();
          this.isLoading = false;
          Swal.fire({
            icon: 'success',
            title: 'Store updated successfully!',
          });
        },
        error: () => {
          this.errorMessage = 'Failed to update store.';
          this.isLoading = false;
        },
      });
  }

  deleteStore(storeId: number): void {
    Swal.fire({
      title: 'Are you sure you want to delete this store?',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Yes, delete it!',
      cancelButtonText: 'Cancel',
    }).then((result) => {
      if (!result.isConfirmed) return;
      this.isLoading = true;
      this.storeAdminService.deleteStore(storeId).subscribe({
        next: () => {
          this.stores = this.stores.filter((s) => s.storeId !== storeId);
          this.isLoading = false;
          Swal.fire({
            icon: 'success',
            title: 'Store deleted successfully!',
          });
        },
        error: () => {
          this.errorMessage = 'Failed to delete store.';
          this.isLoading = false;
          Swal.fire({
            icon: 'error',
            title: 'Failed to delete store.',
          });
        },
      });
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
      ownerId: user?.id || 0,
    };

    this.editingStore = null;
    this.showStoreForm = false;
    this.resetStoreUpload(); // Clear the file upload
  }

  // Add method to handle store selection
  onStoreSelected(store: Store): void {
    this.selectedStore = store;

    if (store.storeId) {
      this.loadOrdersByStore(store.storeId);
      this.loadProductsCount(store.storeId);
    } else {
      this.errorMessage = 'Cannot load store data: Missing store information';
    }
  }

  loadOrdersByStore(storeId: number): void {
    this.isLoading = true;
    this.storeAdminService.getOrdersByStoreId(storeId).subscribe({
      next: (response) => {
        this.recentOrders = response.data ?? [];
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load orders for this store';
        this.isLoading = false;
      },
    });
  }

  loadOrderCount(userId: number): void {
    this.storeAdminService.getOrderCountByUserId(userId).subscribe({
      next: (response) => {
        this.orderCount = response.totalOrders;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load total order count.';
      },
    });
  }

  viewOrderDetails(orderId: number): void {
    if (!this.selectedStore?.storeId) return;

    this.storeAdminService
      .getStoreOrderById(this.selectedStore.storeId, orderId)
      .subscribe({
        next: (order) => {
          Swal.fire({
            icon: 'info',
            title: 'Order Details',
            html: `<b>ID:</b> ${order.orderId}<br><b>Total:</b> R${order.totalAmount}<br><b>Status:</b> ${order.orderStatus}`,
          });
        },
        error: (error) => {
          // ...existing code...
          this.errorMessage = 'Failed to load order details';
        },
      });
  }

  createProduct(): void {
    if (!this.selectedStore?.storeId) {
      this.errorMessage = 'No store selected.';
      return;
    }

    this.isCreatingProduct = true;
    this.errorMessage = '';

    this.productService
      .createProduct(this.selectedStore.storeId, this.newProduct, this.selectedFile)
      .subscribe({
        next: () => {
          this.isCreatingProduct = false;
          this.handleProductCreated();
        },
        error: (err) => {
          this.isCreatingProduct = false;
          this.errorMessage = err?.error?.message || err?.message || 'Failed to create product. Please try again.';
        }
      });
  }

  handleProductCreated(): void {
    // this.loadProducts();
    this.resetProductForm();
    Swal.fire({
      icon: 'success',
      title: 'Product created successfully!',
    });
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
      userId: 0,
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
    if (
      this.selectedFile &&
      this.uploadContext === 'store' &&
      this.editingStore?.storeId
    ) {
      this.uploadStoreLogo(Number(this.editingStore.storeId));
    } else if (this.uploadContext === 'product') {
      this.newProduct.imageUrl = this.previewUrl as string;
      Swal.fire({
        icon: 'success',
        title: 'Product image set successfully!',
      });
      this.resetUpload();
    }
  }

  public uploadStoreLogo(storeId: number): void {
    this.isLoading = true;
    this.storeAdminService
      .uploadStoreLogo(storeId, this.selectedStoreFile!)
      .subscribe({
        next: () => {
          Swal.fire({
            icon: 'success',
            title: 'Store logo uploaded successfully!',
          });
          this.resetStoreUpload();
          this.loadStores();
          this.isLoading = false;
        },
        error: () => {
          this.errorMessage = 'Failed to upload logo.';
          Swal.fire({
            icon: 'error',
            title: 'Failed to upload logo.',
          });
          this.isLoading = false;
        },
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

  //helper function to format time
  private formatTimeForDisplay(time24hr: string): string {
    if (!time24hr) return '';

    const [hours, minutes] = time24hr.split(':').map(Number);

    const date = new Date();
    date.setHours(hours, minutes);

    return date.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: true,
    });
  }

  //update business hours
  updateBusinessHours(): void {
    if (!this.storeModel.startDay || !this.storeModel.endDay || !this.storeModel.startTime || !this.storeModel.endTime) {
      Swal.fire({
        icon: 'warning',
        title: 'Please select days and times for business hours.',
      });
      return;
    }

    // Format days and times
    const formattedStartTime = this.formatTimeForDisplay(this.storeModel.startTime);
    const formattedEndTime = this.formatTimeForDisplay(this.storeModel.endTime);
    const finalHours = `${this.storeModel.startDay} - ${this.storeModel.endDay}, ${formattedStartTime} - ${formattedEndTime}`;
    this.storeModel.storeBusinessHours = finalHours;
  }

  loadProductsCount(storeId: number): void {
    this.productService.getProductCountByStore(storeId).subscribe({
      next: (response) => {
        this.totalProducts = response.totalProducts;
      },
      error: (error) => {
        // ...existing code...
      },
    });
  }

  loadStoresCount(): void {
    const user = this.authService.currentUserValue;
    if (!user?.id) return;

    this.storeAdminService.getStoreCountByOwner(Number(user.id!)).subscribe({
      next: (response) => {
        this.totalStores = response.totalStores;
      },
      error: (error) => {
        // ...existing code...
      },
    });
  }

  loadAllOrdersByUser(): void {
  const user = this.authService.currentUserValue;
  if (!user?.id) return;

  this.isLoading = true;
  this.storeAdminService.getAllOrdersByUserId(Number(user.id)).subscribe({
    next: (response) => {
      console.log('Orders response:', response); // Debug log
      // Assign the orders array from the response
      this.recentOrders = response.data || [];
      this.orderCount = response.count || 0;
      this.isLoading = false;
    },
    error: (error) => {
      this.errorMessage = 'Failed to load orders';
      this.isLoading = false;
    }
  });
}

  clearStoreFilter(): void{
    this.selectedStore = null;
    this.recentOrders = [];
    this.totalProducts = 0;
  }

  viewAllOrders(): void {
    this.selectedStore = null;
    this.loadAllOrdersByUser();
  }
}
