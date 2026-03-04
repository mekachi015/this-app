import { Component, OnInit } from '@angular/core';
import Swal from 'sweetalert2';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AddressService } from '../../services/address-service/address.service';
import { Address } from '../../models/address-model/address';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-address-page',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],

  templateUrl: './address-page.component.html',
  styleUrls: ['./address-page.component.scss']
})

export class AddressPageComponent implements OnInit {
  addressForm: FormGroup;
  addresses: Address[] = [];
  editingAddressId: number | null = null;
  private returnTo: string | null = null;

  constructor(
    private fb: FormBuilder,
    private addressService: AddressService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.addressForm = this.fb.group({
      streetNumber: ['', Validators.required],
      streetName: [''],
      suburb: [''],
      city: ['', Validators.required],
      province: ['', Validators.required],
      postalCode: ['', Validators.required],
      addressType: ['SHIPPING', Validators.required],
      isDefault: [false]
    });
  }

  ngOnInit(): void {
    this.returnTo = this.route.snapshot.queryParamMap.get('returnTo');
    this.loadAddresses();
  }

  loadAddresses(): void {
   this.addressService.getUserAddresses().subscribe({
    next: (data: Address[]) => {
      this.addresses = data;
    },
    error: (error) => {
      console.error('Error loading addresses:', error);
    }
  });
  }


  onSubmit(): void {
    if (this.addressForm.valid) {
      if (this.editingAddressId !== null) {
        // Update existing address
        this.addressService.updateAddress(this.editingAddressId, this.addressForm.value).subscribe({
          next: () => {
            this.loadAddresses();
            Swal.fire({
              icon: 'success',
              title: 'Address updated',
              text: 'Your address has been updated successfully.'
            });
            this.addressForm.reset();
            this.editingAddressId = null;
          },
          error: (error) => {
            console.error('Error updating address:', error);
            Swal.fire({
              icon: 'error',
              title: 'Update failed',
              text: 'There was an error updating the address.'
            });
          }
        });
      } else {
        // Create new address
        this.addressService.createAddress(this.addressForm.value).subscribe({
          next: () => {
            this.loadAddresses();
            Swal.fire({
              icon: 'success',
              title: 'Address added',
              text: 'Your address has been added successfully.'
            }).then(() => {
              if (this.returnTo === 'checkout') {
                this.router.navigate(['/checkout']);
              }
            });
            this.addressForm.reset();
          },
          error: (error) => {
            console.error('Error creating address:', error);
            Swal.fire({
              icon: 'error',
              title: 'Add failed',
              text: 'There was an error adding the address.'
            });
          }
        });
      }
    }
  }

  deleteAddress(addressId: number): void {
    this.addressService.deleteAddress(addressId).subscribe({
    next: () => {
      this.loadAddresses();
      Swal.fire({
        icon: 'success',
        title: 'Address deleted',
        text: 'The address has been deleted.'
      });
    },
    error: (error) => {
      console.error('Error deleting address:', error);
      Swal.fire({
        icon: 'error',
        title: 'Delete failed',
        text: 'There was an error deleting the address.'
      });
    }
  });
  }

  editAddress(address: Address): void {
    this.addressForm.patchValue({
      streetNumber: address.streetNumber,
      streetName: address.streetName,
      suburb: address.suburb,
      city: address.city,
      province: address.province,
      postalCode: address.postalCode,
      addressType: address.addressType,
      isDefault: address.isDefault || false
    });
    this.editingAddressId = address.addressId;
  }
}
