package com.retail.discount.retaildiscountapplication.bill;

import com.retail.discount.retaildiscountapplication.bill.model.Bill;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillRepository extends MongoRepository<Bill, String> {
}