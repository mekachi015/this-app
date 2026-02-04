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
import { time } from 'console';



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
  recentOrders: any[] = [];
  orderCount: number = 0;

  //date and time
  selectedDays: string[] = [];
  openTime: string = '07:00';
  closeTime: string = '18:00';

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
  this.storeModel = { ...store }; // Create a copy of the store data
  this.showStoreForm = true;
  this.uploadContext = 'store';
  
  // If you have a logo, set the preview
  if (store.storeLogo) {
    this.storePreviewUrl = store.storeLogo;
  }
  
  console.log("Editing store:", this.storeModel);
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
       console.log('Store selected:', store.storeName, 'storeId:', store.storeId);
  
  this.selectedStore = store;
  
  // Use the store's ownerId for loading orders
  const ownerIdForOrders = store.ownerId;
  
  console.log('Store Owner ID (for orders):', ownerIdForOrders);
  console.log('Current storeModel ownerId (for store creation):', this.storeModel.ownerId);
  
  if (store.storeId && ownerIdForOrders) {
    // Pass the store's ownerId to load orders
    this.loadStoreOrders(store.storeId, ownerIdForOrders);
    // Load order count using storeId
    this.loadOrderCount(store.storeId);
  } else {
    console.error('Missing storeId or ownerId');
    console.log('storeId:', store.storeId);
    console.log('ownerId from store:', ownerIdForOrders);
    this.errorMessage = 'Cannot load orders: Missing store or owner information';
  }
  }

  loadStoreOrders(storeId: number,ownerId: number): void {
    console.log('Loading orders for storeId:', storeId, 'using ownerId:', ownerId);
  
  this.isLoading = true;
  
  if (!ownerId) {
    console.error('Owner ID is not available');
    this.errorMessage = 'Cannot load orders: Owner ID not found';
    this.isLoading = false;
    return;
  }
  
  // Use the passed ownerId (from store) instead of this.storeModel.ownerId
  this.storeAdminService.getStoreOrders(ownerId).subscribe({
    next: (orders) => {
      console.log('All orders received from API:', orders);
      
      // Filter orders to only show orders for this specific store
      const storeOrders = orders.filter(order => order.storeId === storeId);
      
      console.log('Filtered orders for store ' + storeId + ':', storeOrders);
      
      this.recentOrders = storeOrders;
      this.isLoading = false;
    },
    error: (error) => {
      console.error('Error loading orders:', error);
      console.error('Error details:', error.message, error.status);
      this.errorMessage = 'Failed to load orders: ' + error.message;
      this.isLoading = false;
    }
  });
}

loadOrderCount(storeId: number): void {
  this.storeAdminService.getStoreOrderCount(storeId).subscribe({
    next: (response) => {
      this.orderCount = response.count;
      console.log('Order count:', this.orderCount);
    },
    error: (error) => {
      console.error('Error loading order count:', error);
    }
  });
}

viewOrderDetails(orderId: number): void {
  if (!this.selectedStore?.storeId) return;
  
  this.storeAdminService.getStoreOrderById(this.selectedStore.storeId, orderId).subscribe({
    next: (order) => {
      console.log('Order details:', order);
      // You can display this in a modal or navigate to order details page
      alert(`Order Details:\nID: ${order.orderId}\nTotal: R${order.totalAmount}\nStatus: ${order.orderStatus}`);
    },
    error: (error) => {
      console.error('Error loading order details:', error);
      this.errorMessage = 'Failed to load order details';
    }
  });
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

  //helper function to format time
  private formatTimeForDisplay(time24hr: string): string{
    if(!time24hr) return '';

    const [hours, minutes] = time24hr.split(':').map(Number);

    const date = new Date();
    date.setHours(hours, minutes);

    return date.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: true
    });
  }

  //update business hours 
  updateBusinessHours(): void{
    if(this.selectedDays.length === 0 || this.openTime || this.closeTime){
      alert('Please select days and times for business hours.');
      return;
    }

    //Formate days string
    let dayString : string;
    if(this.selectedDays.length === 7){
      dayString = 'Everyday';
    } else if( 
      this.selectedDays.length === 5 &&
      !this.selectedDays.includes('Saturday') &&
      (!this.selectedDays.includes('Sunday')
    )){
      dayString = 'Mon - Fri';
    } else{
      dayString = this.selectedDays.join(', ');
    }

    //formate times for display
    const formattedOpenTime = this.formatTimeForDisplay(this.openTime);
    const formattedCloseTime = this.formatTimeForDisplay(this.closeTime);

    //combine and update store model
    const finalHours = `${dayString}: ${formattedOpenTime} - ${formattedCloseTime}`;

    this.storeModel.storeBusinessHours = finalHours;
    console.log('Updated business hours to:', finalHours);
  }
}