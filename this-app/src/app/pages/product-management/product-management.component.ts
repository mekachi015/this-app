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

  store: Store | null = null;
  products: Product[] = [];
  isLoading = false;
  errorMessage = '';
  showProductForm = false;
  editingProduct: Product | null = null;

  //File management properties
  selectedFile: File | null = null;
  previewUrl: string | ArrayBuffer | null = null;
  isDragOver = false;

  productModel: CreateProductDTO = {
    productName: '',
    productDescription: '',
    productPrice: 0,
    stockQuantity: 0,
    category: '',
    imageUrl: '',
    storeId: 0 
  }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private storeAdminService: StoreAdminServiceService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const storeId = +params['id'];
     if (isNaN(storeId) || storeId <= 0) {
        this.errorMessage = 'Invalid Store ID';
        console.error('Invalid storeId:', params['id']);
        return;
      }
      
      this.loadStore(storeId);
    });
  }

  // ---------------------- Store Loading ----------------------
  loadStore(storeId: number): void {
    console.log('Loading store with ID:', storeId);
    
    // FIX: Use the correct service method to get store info
    this.isLoading = true;
    this.storeAdminService.getStoreById(storeId).subscribe({
    //this.productService.getStoreById(storeId).subscribe({
      next: (store) => {
        console.log('Store loaded successfully:', store);
        this.store = store;
        
        console.log('Store ID:', storeId);
        // FIX: Ensure storeId is valid before setting
        const validStoreId = Number(storeId);
        if (isNaN(validStoreId)) {
          this.errorMessage = 'Invalid store data received';
          this.isLoading = false;
          return;
        }
        
        this.productModel.storeId = validStoreId;
        console.log('Set productModel.storeId to:', this.productModel.storeId);
        console.log('Set productModel.storeId to:', this.productModel.storeId);
        this.loadProducts(validStoreId);
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Error loading store: ' + error.message;
        this.isLoading = false;
        console.error('Store loading error:', error);
      }
    });
  }

  // ---------------------- Product Management ----------------------
  loadProducts(storeId: number): void {
    console.log('Loading products for store ID:', storeId);
    //this.productService.getStoreById(storeId).subscribe({
    this.productService.getStoreProducts(storeId).subscribe({
      next: (products) => {
        this.products = products;
        console.log('Loaded products:', products);
      },
      error: (error) => {
        this.errorMessage = 'Error loading products';
        console.error('Product loading error:', error);
      }
    });
  }

  // Handle form submission
  onSubmit(): void {
    console.log('Form submitted!');
    console.log('Editing product:', this.editingProduct);
    
    if (this.editingProduct) {
      this.updateProduct();
    } else {
      this.createProduct();
      console.log('On submit call, Creating product with model:', this.productModel);
      console.log('selected store id:', this.productModel.storeId);
      this.showProductForm = false; 
    }
  }

  // FIX: Create product
  createProduct(): void {

     if (!this.store?.storeId) {
    this.errorMessage = 'Store information is not available';
    return;
  }

  this.isLoading = true;
  
  // Just pass the model and file - service handles FormData construction
  this.productService.createProductWithImage(this.store.storeId, this.productModel, this.selectedFile)
    .subscribe({
      next: (product) => {
        console.log('✅ Product created', product);
        this.products.push(product);
        this.resetProductForm();
        this.isLoading = false;
      },
      error: (err) => {
        console.error('❌ Product creation failed', err);
        this.errorMessage = err.error?.error || err.message || 'Error creating product';
        this.isLoading = false;
      }
    });
    console.log('Create product call, Creating product with model:', this.productModel);
  }


  editProduct(product: Product): void {
    this.editingProduct = product;
    this.productModel = {
      productName: product.productName,
      productDescription: product.productDescription,
      productPrice: product.productPrice,
      stockQuantity: product.stockQuantity,
      category: product.category,
      //imageUrl: product.imageUrl,
      storeId: product.storeId || 0
    };
    //this.previewUrl = product.imageUrl; // Show existing image
    this.showProductForm = true;
  }

  // FIX: Update product
  updateProduct(): void {
    if (!this.editingProduct?.productId) {
      this.errorMessage = 'Product ID is missing';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    // Use the same DTO structure as create
    this.productService.updateProduct(Number(this.editingProduct.productId), this.productModel).subscribe({
      next: (updatedProduct) => {
        const index = this.products.findIndex(p => p.productId === updatedProduct.productId);
        if (index !== -1) {
          this.products[index] = updatedProduct;
        }
        this.resetProductForm();
        this.isLoading = false;
        alert('Product updated successfully!');
      },
      error: (error) => {
        this.errorMessage = 'Failed to update product: ' + (error.error?.message || error.message);
        console.error('Update product error:', error);
        this.isLoading = false;
      }
    });
  }

  deleteProduct(productId: number): void {
    if (!confirm('Are you sure you want to delete this product?')) return;

    this.isLoading = true;
    this.errorMessage = '';

    this.productService.deleteProduct(productId).subscribe({
      next: () => {
        this.products = this.products.filter(p => p.productId !== productId);
        this.isLoading = false;
        alert('Product deleted successfully!');
      },
      error: (error) => {
        this.errorMessage = 'Failed to delete product: ' + (error.error?.message || error.message);
        console.error('Delete product error:', error);
        this.isLoading = false;
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
      imageUrl: '',
      storeId: 0
    };
    this.editingProduct = null;
    this.showProductForm = false;
    this.resetUpload();
    this.errorMessage = '';
  }

  // ---------------------- File Upload ----------------------
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

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver = false;
  }

  handleFileSelection(file: File): void {
    // Check file size (5MB limit)
    const maxSize = 5 * 1024 * 1024; // 5MB in bytes
    if (file.size > maxSize) {
      this.errorMessage = 'File size must be less than 5MB';
      return;
    }

    if (file && this.isImageFile(file)) {
      this.selectedFile = file;
      const reader = new FileReader();
      reader.onload = () => {
        this.previewUrl = reader.result;
        // Don't set imageUrl here - it will be set by the backend
      };
      reader.onerror = () => {
        this.errorMessage = 'Error reading file';
      };
      reader.readAsDataURL(file);
      this.errorMessage = '';
      console.log('File selected:', file.name, 'Size:', file.size, 'Type:', file.type);
    } else {
      this.errorMessage = 'Please select a valid image file (JPEG or PNG).';
    }
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
}
