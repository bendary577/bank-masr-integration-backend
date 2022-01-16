package com.sun.supplierpoc.repositories.simphony;

import com.sun.supplierpoc.models.simphony.redeemVoucher.Voucher;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface VoucherRepository extends MongoRepository<Voucher, String> {

    ArrayList<Voucher> findAllByAccountIdAndDeleted(String id, boolean b);

    ArrayList<Voucher> findAllByAccountId(String id);

    boolean existsByAccountIdAndNameAndDeleted(String id, String name, boolean b);
}
