import { TestBed } from '@angular/core/testing';

import { StoreAdminServiceService } from './store-admin-service.service';

describe('StoreAdminServiceService', () => {
  let service: StoreAdminServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(StoreAdminServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
