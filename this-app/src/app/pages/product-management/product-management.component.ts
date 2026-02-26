import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../../services/product-service/product.service';
import { StoreAdminServiceService } from '../../services/store-admin-service/store-admin-service.service';
import { AuthService } from '../../services/authentication-service/auth.service';
import { Product } from '../../models/store-admin-models/product-admin/product';
import { CreateProductDTO } from '../../models/store-admin-models/product-admin/CreateProductDTO';
import { Store } from '../../models/store-admin-models/store-admin/Store';

@Component({
  selector: 'app-product-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './product-management.component.html',
  styleUrl: './product-management.component.scss'
})
export class ProductManagementComponent implements OnInit {

  availableCategories: string[] = [
    'Shirts',
    'Shoes',
    'Pants',
    'Accessories',
    'Hats',
    'Jackets',
    'Dresses',
    'Skirts',
    'Shorts',
    'Sweaters'
  ];

  // Store and User data
  store: Store | null = null;
  currentUserId: number | null = null;
  storeOwnerId: number | null = null;
  
  // Product data
  products: Product[] = [];
  editingProduct: Product | null = null;

  totalProductCount: number = 0; // For statistics

  // UI state
  isLoading = false;
  errorMessage = '';
  showProductForm = false;

  // File upload
  selectedFile: File | null = null;
  previewUrl: string | ArrayBuffer | null = null;
  isDragOver = false;

  // Product form model - initialize with empty values
  productModel: CreateProductDTO = {
    productName: '',
    productDescription: '',
    productPrice: 0,
    stockQuantity: 0,
    category: '',
    storeId: 0,
    userId: 0
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private storeAdminService: StoreAdminServiceService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    // Get current user ID
    const currentUser = this.authService.currentUserValue;
    if (!currentUser?.id) {
      this.errorMessage = 'User not authenticated';
      this.router.navigate(['/login']);
      return;
    }
    this.currentUserId = Number(currentUser.id);

    this.productModel.userId = this.currentUserId;

    // Get store ID from route
    this.route.params.subscribe(params => {
      const storeId = +params['id'];
      if (isNaN(storeId) || storeId <= 0) {
        this.errorMessage = 'Invalid Store ID';
        console.error('Invalid storeId:', params['id']);
        return;
      }
      
      // Assign storeId to productModel immediately
      this.productModel.storeId = storeId;
      console.log('✅ Set productModel.storeId to:', this.productModel.storeId);
      
      this.loadStore(storeId);
      //this.loadProducts(storeId);
    });
  }

  // ---------------------- Store Loading ----------------------
  loadStore(storeId: number): void {
  console.log('Loading store with ID:', storeId);
  
  this.isLoading = true;
  this.errorMessage = '';
  
  this.storeAdminService.getStoreById(storeId).subscribe({
    next: (store) => {
      console.log('Store loaded successfully: for products', store);
      this.store = store;
      this.storeOwnerId = this.currentUserId ?? null;
      this.productModel.storeId = storeId;
      this.loadProducts(storeId);
      this.loadProductsCount(storeId);
      this.isLoading = false;
    },
    error: (error) => {
      this.errorMessage = 'Error loading store: ' + (error.error?.message || error.message);
      this.isLoading = false;
      console.error('Store loading error:', error);
    }
  });
}

  // ---------------------- Product Management ----------------------
  loadProducts(storeId: number): void {
    console.log('Loading products for store ID:', storeId);

    this.isLoading = true;
    this.errorMessage = '';
    
    this.productService.getStoreProducts(storeId).subscribe({
      next: (products) => {
        // Ensure products is always an array
        this.products = Array.isArray(products) ? products : [];
        console.log('✅ Loaded products:', this.products);
        console.log('✅ Products count:', this.products.length);
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Error loading products: ' + (error.error?.message || error.message);
        this.products = []; // Reset to empty array on error
        this.isLoading = false;
        console.error('❌ Product loading error:', error);
      }
    });
  }

  onSubmit(): void {
    console.log('Form submitted!');
    console.log('Editing product:', this.editingProduct);
    
    if (this.editingProduct) {
      this.updateProduct();
    } else {
      this.createProduct();
    }

    console.log("edited product:" , this.editingProduct);
  }

  createProduct(): void {
    console.log('Creating product for store ID:', this.productModel.storeId);
    console.log('Current user:', this.currentUserId);
    
    // Validate storeId
    if (!this.productModel?.storeId) {
      this.errorMessage = 'Store ID is missing';
      return;
    }

    // Validate required fields
    if (!this.productModel.productName || !this.productModel.category) {
      this.errorMessage = 'Product name and category are required';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    
    this.productService.createProduct(
      this.productModel.storeId,
      this.productModel,
      this.selectedFile
    ).subscribe({
      next: (product) => {
        console.log('✅ Product created successfully:', product);
        // Add the new product to the array
        this.products = [...this.products, product];
        this.resetProductForm();
        this.loadProducts(this.productModel.storeId);
        this.loadProductsCount(this.productModel.storeId);
        this.isLoading = false;
        alert('Product created successfully!');
        
      },
      error: (error) => {
        console.error('❌ Product creation failed:', error);
        this.errorMessage = error.error?.message || error.message || 'Error creating product';
        this.isLoading = false;
      }
    });
  }

  editProduct(product: Product): void {
     console.log('Called editing for product:', product);
  this.editingProduct = { ...product };
  
  // CRITICAL: Preserve the original storeId from the loaded store
  this.productModel = {
    productName: product.productName,
    productDescription: product.productDescription || '',
    productPrice: product.productPrice,
    stockQuantity: product.stockQuantity,
    category: product.category || '',
    storeId: this.productModel.storeId || product.storeId || 0, // Use current storeId first
    userId: this.currentUserId || 0
  };
  
  this.previewUrl = product.imageUrl || null;
  this.showProductForm = true;
  
  // Log to verify
  console.log('✅ Editing product with storeId:', this.productModel.storeId);

  }

  updateProduct(): void {
  console.log('🔍 UPDATE VALIDATION:');
  console.log('  - currentUserId:', this.currentUserId);
  console.log('  - productId:', this.editingProduct?.productId);
  console.log('  - storeId:', this.productModel?.storeId);
  
  // VALIDATION - Add these checks
  if (!this.editingProduct?.productId) {
    this.errorMessage = 'Product ID is missing';
    return;
  }
  
  if (!this.productModel?.storeId) {
    this.errorMessage = 'Store ID is missing';
    return;
  }
  
  if (!this.currentUserId) {
    this.errorMessage = 'User ID is missing';
    return;
  }
  
  // CRITICAL: Create a proper DTO with ALL required fields
  const updateDTO: CreateProductDTO = {
    productName: this.productModel.productName,
    productDescription: this.productModel.productDescription,
    productPrice: this.productModel.productPrice,
    stockQuantity: this.productModel.stockQuantity,
    category: this.productModel.category,
    storeId: this.productModel.storeId,  // Make sure this is set
    userId: this.currentUserId           // This is required!
  };
  
  console.log('📤 Calling updateProduct service with:', {
    productId: this.editingProduct.productId,
    dto: updateDTO,
    hasFile: !!this.selectedFile
  });
  
  this.isLoading = true;
  this.errorMessage = '';
  
  this.productService.updateProduct(
    this.editingProduct.productId,
    updateDTO,  // Use the proper DTO
    this.selectedFile
  ).subscribe({
    next: (updatedProduct) => {
      console.log('✅ Product updated successfully:', updatedProduct);
      this.loadProducts(this.productModel.storeId);
      this.loadProductsCount(this.productModel.storeId);


      this.resetProductForm();
      this.isLoading = false;
      alert('Product updated successfully!');
    },
    error: (error) => {
      console.error('❌ Product update failed:', error);
      console.error('❌ Error details:', error.error);
      console.error('❌ Status:', error.status);
      this.errorMessage = error.error?.message || error.message || 'Failed to update product';
      this.isLoading = false;
    }
  });
}

  deleteProduct(productId: number, userId: number, storeId: number): void {
    // if (!confirm('Are you sure you want to delete this product?')) return;

    // this.isLoading = true;
    // this.errorMessage = '';

    // this.productService.deleteProduct(storeId, productId).subscribe({
    //   next: () => {
    //     console.log('✅ Product deleted successfully');
    //     // Remove product from array immutably
    //     this.products = this.products.filter(p => p.productId !== productId);
    //     this.isLoading = false;
    //     alert('Product deleted successfully!');
    //   },
    //   error: (error) => {
    //     console.error('❌ Product deletion failed:', error);
    //     this.errorMessage = error.error?.message || error.message || 'Failed to delete product';
    //     this.isLoading = false;
    //   }
    // });
  }

  resetProductForm(): void {
   this.productModel = {
    productName: '',
    productDescription: '',
    productPrice: 0,
    stockQuantity: 0,
    category: '',
    storeId: this.productModel.storeId || this.store?.storeId || 0, // PRESERVE storeId
    userId: this.currentUserId || 0
  };
  this.editingProduct = null;
  this.showProductForm = false;
  this.resetUpload();
  this.errorMessage = '';
  }

  // ---------------------- File Upload ----------------------
  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.handleFileSelection(file);
    }
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

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver = false;
  }

  handleFileSelection(file: File): void {
    // Validate file size (5MB limit)
    const maxSize = 5 * 1024 * 1024;
    if (file.size > maxSize) {
      this.errorMessage = 'File size must be less than 5MB';
      return;
    }

    // Validate file type
    if (!this.isImageFile(file)) {
      this.errorMessage = 'Please select a valid image file (JPEG or PNG)';
      return;
    }

    this.selectedFile = file;
    const reader = new FileReader();
    
    reader.onload = () => {
      this.previewUrl = reader.result;
      this.errorMessage = '';
    };
    
    reader.onerror = () => {
      this.errorMessage = 'Error reading file';
    };
    
    reader.readAsDataURL(file);
    console.log('File selected:', file.name, 'Size:', file.size, 'Type:', file.type);
  }

  isImageFile(file: File): boolean {
    return file.type === 'image/jpeg' || 
           file.type === 'image/png' || 
           file.type === 'image/jpg';
  }

  resetUpload(): void {
    this.selectedFile = null;
    this.previewUrl = null;
    this.isDragOver = false;
  }

  goBackToStores(): void {
    this.router.navigate(['/dashboard']);
  }

  // ---------------------- Statistics ----------------------

  loadProductsCount(storeId: number): void {
      this.productService.getProductCountByStore(storeId).subscribe({
    next: (response) => {
      this.totalProductCount = response.totalProducts;
    },
    error: (error) => {
      console.error('Error loading product count:', error);
    }
  });
  }
  getTotalStock(): number {
    return this.products.reduce((total, product) => total + (product.stockQuantity || 0), 0);
  }

  getCategoriesCount(): number {
    const categories = new Set(this.products.map(product => product.category).filter(Boolean));
    return categories.size;
  }

  getAveragePrice(): number {
    if (this.products.length === 0) return 0;
    const total = this.products.reduce((sum, product) => sum + (product.productPrice || 0), 0);
    return total / this.products.length;
  }
}