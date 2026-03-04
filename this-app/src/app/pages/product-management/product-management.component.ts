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
import Swal from 'sweetalert2';

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
  isCreatingProduct = false;
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
        return;
      }
      // Assign storeId to productModel immediately
      this.productModel.storeId = storeId;
      this.loadStore(storeId);
      //this.loadProducts(storeId);
    });
  }

  // ---------------------- Store Loading ----------------------
  loadStore(storeId: number): void {
  this.isLoading = true;
  this.errorMessage = '';
  this.storeAdminService.getStoreById(storeId).subscribe({
    next: (store) => {
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
    }
  });
}

  // ---------------------- Product Management ----------------------
  loadProducts(storeId: number): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.productService.getStoreProducts(storeId).subscribe({
      next: (products) => {
        // Ensure products is always an array
        this.products = Array.isArray(products) ? products : [];
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Error loading products: ' + (error.error?.message || error.message);
        this.products = []; // Reset to empty array on error
        this.isLoading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.editingProduct) {
      this.updateProduct();
    } else {
      this.createProduct();
    }
  }

  createProduct(): void {
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

    this.isCreatingProduct = true;
    this.errorMessage = '';
    
    this.productService.createProduct(
      this.productModel.storeId,
      this.productModel,
      this.selectedFile
    ).subscribe({
      next: (product) => {
        // Add the new product to the array
        this.products = [...this.products, product];
        this.resetProductForm();
        this.loadProducts(this.productModel.storeId);
        this.loadProductsCount(this.productModel.storeId);
        this.isCreatingProduct = false;
         Swal.fire({
           icon: 'success',
           title: 'Product Created',
           text: 'Product has been created successfully!',
           timer: 2000,
           showConfirmButton: false,
         });
      },
      error: (error) => {
        this.errorMessage = error.error?.message || error.message || 'Error creating product';
        this.isCreatingProduct = false;
      }
    });
  }

  editProduct(product: Product): void {
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

  }

  updateProduct(): void {
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
  this.isLoading = true;
  this.errorMessage = '';
  this.productService.updateProduct(
    this.editingProduct.productId,
    updateDTO,  // Use the proper DTO
    this.selectedFile
  ).subscribe({
    next: (updatedProduct) => {
      this.loadProducts(this.productModel.storeId);
      this.loadProductsCount(this.productModel.storeId);


      this.resetProductForm();
      this.isLoading = false;
      Swal.fire({
        icon: 'success',
        title: 'Product Updated',
        text: 'Product has been updated successfully!',
        timer: 2000,
        showConfirmButton: false,
      });
    },
    error: (error) => {
      this.errorMessage = error.error?.message || error.message || 'Failed to update product';
      this.isLoading = false;
    }
  });
}

  deleteProduct(productId: number): void {
     Swal.fire({
    title: 'Delete Product?',
    text: 'This action cannot be undone.',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#ea5455',
    cancelButtonColor: '#edf2f7',
    confirmButtonText: 'Yes, delete it',
    cancelButtonText: 'Cancel'
  }).then((result) => {
    if (result.isConfirmed) {
      this.isLoading = true;
      this.productService.deleteProduct(productId).subscribe({
        next: () => {
          this.products = this.products.filter(p => p.productId !== productId);
          this.loadProductsCount(this.productModel.storeId);
          this.isLoading = false;
          Swal.fire({
            icon: 'success',
            title: 'Deleted',
            text: 'Product has been deleted.',
            timer: 2000,
            showConfirmButton: false
          });
        },
        error: (error) => {
          this.isLoading = false;
          Swal.fire({
            icon: 'error',
            title: 'Delete Failed',
            text: error.error?.message || 'Failed to delete product'
          });
        }
      });
    }
  });
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
        // Error loading product count
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