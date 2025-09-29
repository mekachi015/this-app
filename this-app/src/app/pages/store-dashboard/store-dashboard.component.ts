import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/authentication-service/auth.service';

interface Order {
  id: string;
  customer: string;
  products: string;
  total: number;
  status: 'Completed' | 'Pending' | 'Cancelled';
}

interface Store {
  id?: string;
  name: string;
  description: string;
  address: string;
  contactEmail: string;
  contactPhone: string;
  businessHours: string;
  logo?: string;
  createdAt?: Date;
  ownerId?: string;
}

interface Product {
  id?: string;
  name: string;
  description: string;
  price: number;
  category: string;
  image: string;
  stock: number;
  storeId?: string;
  createdAt?: Date;
}

@Component({
  selector: 'app-store-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './store-dashboard.component.html',
  styleUrl: './store-dashboard.component.scss'
})
export class StoreDashboardComponent implements OnInit {
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

  // File upload properties
  selectedFile: File | null = null;
  previewUrl: string | ArrayBuffer | null = null;
  isDragOver = false;
  uploadContext: 'store' | 'product' = 'store';

  // Store management properties
  isAdmin: boolean = false;
  stores: Store[] = [];
  products: Product[] = [];
  
  // Forms
  showStoreForm: boolean = false;
  showProductForm: boolean = false;
  selectedStore: Store | null = null;
  editingStore: Store | null = null;
  
  newStore: Store = {
    name: '',
    description: '',
    address: '',
    contactEmail: '',
    contactPhone: '',
    businessHours: ''
  };

  newProduct: Product = {
    name: '',
    description: '',
    price: 0,
    category: '',
    image: '',
    stock: 0
  };

  // Make authService public for template access
  constructor(public authService: AuthService) {}

  ngOnInit() {
    this.checkAdminStatus();
    this.loadStores();
    this.loadProducts();
  }

  checkAdminStatus() {
    const user = this.authService.currentUserValue;
    this.isAdmin = user?.userType === 'ADMIN';
  }

  loadStores() {
    if (this.isAdmin) {
      // Mock data - replace with actual API call
      this.stores = [
        {
          id: '1',
          name: 'Main Store',
          description: 'Primary store location',
          address: '123 Main St, City',
          contactEmail: 'store@example.com',
          contactPhone: '+27 11 123 4567',
          businessHours: 'Mon-Fri 9AM-6PM',
          logo: '',
          createdAt: new Date(),
          ownerId: '1'
        }
      ];
    }
  }

  loadProducts() {
    if (this.isAdmin && this.stores.length > 0) {
      // Mock data - replace with actual API call
      this.products = [
        {
          id: '1',
          name: 'Sample Product',
          description: 'A sample product description',
          price: 299.99,
          category: 'Electronics',
          image: '',
          stock: 50,
          storeId: '1',
          createdAt: new Date()
        }
      ];
    }
  }

  // Helper method to get product count for a store
  getProductCount(storeId: string | undefined): number {
    if (!storeId) return 0;
    return this.products.filter(p => p.storeId === storeId).length;
  }

  // File upload methods
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
      
      const reader = new FileReader();
      reader.onload = () => {
        this.previewUrl = reader.result;
      };
      reader.readAsDataURL(file);
    } else {
      console.error('Please select a valid image file (JPEG or PNG)');
    }
  }

  isImageFile(file: File): boolean {
    return file.type === 'image/jpeg' || file.type === 'image/png';
  }

  uploadFile(): void {
    if (this.selectedFile) {
      console.log('Uploading file:', this.selectedFile.name);
      
      // Simulate upload process
      setTimeout(() => {
        if (this.uploadContext === 'store' && this.editingStore) {
          this.editingStore.logo = this.previewUrl as string;
          alert('Store logo uploaded successfully!');
        } else if (this.uploadContext === 'product') {
          this.newProduct.image = this.previewUrl as string;
          alert('Product image uploaded successfully!');
        }
        this.resetUpload();
      }, 1500);
    }
  }

  resetUpload(): void {
    this.selectedFile = null;
    this.previewUrl = null;
    this.isDragOver = false;
  }

  // Store management methods
  createStore() {
    if (this.isAdmin) {
      const store: Store = {
        ...this.newStore,
        id: Date.now().toString(),
        createdAt: new Date(),
        ownerId: this.authService.currentUserValue?.id
      };
      
      this.stores.push(store);
      this.resetStoreForm();
      alert('Store created successfully!');
    }
  }

  updateStore() {
    if (this.isAdmin && this.editingStore) {
      const index = this.stores.findIndex(s => s.id === this.editingStore!.id);
      if (index !== -1) {
        this.stores[index] = { ...this.editingStore };
        this.cancelEditStore();
        alert('Store updated successfully!');
      }
    }
  }

  editStore(store: Store) {
    this.editingStore = { ...store };
    this.showStoreForm = true;
  }

  cancelEditStore() {
    this.editingStore = null;
    this.resetStoreForm();
  }

  resetStoreForm() {
    this.newStore = {
      name: '',
      description: '',
      address: '',
      contactEmail: '',
      contactPhone: '',
      businessHours: ''
    };
    this.showStoreForm = false;
    this.editingStore = null;
  }

  // Product management methods
  createProduct() {
    if (this.isAdmin && this.selectedStore) {
      const product: Product = {
        ...this.newProduct,
        id: Date.now().toString(),
        storeId: this.selectedStore.id,
        createdAt: new Date()
      };
      
      this.products.push(product);
      this.resetProductForm();
      alert('Product created successfully!');
    }
  }

  resetProductForm() {
    this.newProduct = {
      name: '',
      description: '',
      price: 0,
      category: '',
      image: '',
      stock: 0
    };
    this.showProductForm = false;
    this.resetUpload();
  }

  selectStoreForProduct(store: Store) {
    this.selectedStore = store;
    this.showProductForm = true;
    this.uploadContext = 'product';
  }

  openStoreLogoUpload(store: Store) {
    this.editingStore = store;
    this.uploadContext = 'store';
    this.showStoreForm = true;
  }

  deleteStore(store: Store) {
    if (confirm(`Are you sure you want to delete ${store.name}?`)) {
      this.stores = this.stores.filter(s => s.id !== store.id);
      this.products = this.products.filter(p => p.storeId !== store.id);
    }
  }

  deleteProduct(product: Product) {
    if (confirm(`Are you sure you want to delete ${product.name}?`)) {
      this.products = this.products.filter(p => p.id !== product.id);
    }
  }
}