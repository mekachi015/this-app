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
    imageUrl: ''
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
      const storeId = params['id'];
      if (storeId) {
        this.loadStore(storeId);
      }
    });
  }
  // ---------------------- Store Loading ----------------------
  loadStore(storeId: number): void{
    this.isLoading = true;

    this.storeAdminService.getStoreById(storeId).subscribe({
      next: (store) => {
        this.store = store;
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Error loading store details';
        console.error('Store loading error:', error);
        this.isLoading = false;
      }
    });
  }

  // ---------------------- Product Management ----------------------
  loadProducts(storeId: number): void{
    this.isLoading = true;

    this.productService.getStoreProducts(storeId).subscribe({
      next: (products) => {
        this.products = products;
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Error loading products';
        console.error('Product loading error:', error);
        this.isLoading = false;
      }
    });
  }

  //create product 
  createProduct(): void{ 
    if (!this.store?.storeId) {
      return;
    }

    this.isLoading = true;

    const formData = new FormData();

    const productBlob = new Blob([JSON.stringify(this.productModel)], { 
      type: 'application/json' 
    });
    formData.append('productData', productBlob);

    if(this.selectedFile){
      formData.append('image', this.selectedFile);
    }

    // Use createProductWithImage method
    this.productService.createProductWithImage(Number(this.store.storeId), formData).subscribe({
      next: (product) => {
        this.products.push(product);
        this.resetProductForm();
        this.isLoading = false;
        alert('Product created successfully!');
      },
      error: (error) => {
        this.errorMessage = 'Failed to create product: ' + error.message;
        this.isLoading = false;
      }
    });
  }

    editProduct(product: Product): void {
    this.editingProduct = product;
    this.productModel = {
      productName: product.productName,
      productDescription: product.productDescription,
      productPrice: product.productPrice,
      stockQuantity: product.stockQuantity,
      category: product.category,
      imageUrl: product.imageUrl
    };
    this.showProductForm = true;
  }

  updateProduct(): void {
    if (!this.editingProduct?.productId) return;

    this.isLoading = true;
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
        this.errorMessage = 'Failed to update product';
        this.isLoading = false;
      }
    });
  }

   deleteProduct(productId: number): void {
    if (!confirm('Are you sure you want to delete this product?')) return;

    this.isLoading = true;
    this.productService.deleteProduct(productId).subscribe({
      next: () => {
        this.products = this.products.filter(p => p.productId !== productId);
        this.isLoading = false;
        alert('Product deleted successfully!');
      },
      error: (error) => {
        this.errorMessage = 'Failed to delete product';
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
      imageUrl: ''
    };
    this.editingProduct = null;
    this.showProductForm = false;
    this.resetUpload();
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

  handleFileSelection(file: File): void {
    if (file && this.isImageFile(file)) {
      this.selectedFile = file;
      const reader = new FileReader();
      reader.onload = () => {
        this.previewUrl = reader.result;
        this.productModel.imageUrl = reader.result as string;
      };
      reader.readAsDataURL(file);
    } else {
      this.errorMessage = 'Please select a valid image file (JPEG or PNG).';
    }
  }

  isImageFile(file: File): boolean {
    return file.type === 'image/jpeg' || file.type === 'image/png';
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
