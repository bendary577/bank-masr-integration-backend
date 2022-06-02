package com.sun.supplierpoc.repositories.applications;

import com.sun.supplierpoc.models.applications.ApplicationUser;
import com.sun.supplierpoc.models.applications.Group;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.CountQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface ApplicationUserRepo extends MongoRepository<ApplicationUser, String> {
    ArrayList<ApplicationUser> findAll();
    ApplicationUser findByCode(String code);
    ApplicationUser findByCodeAndAccountIdAndDeleted(String code, String accountId, boolean deleted);
    ApplicationUser findByCodeAndAccountIdAndDeletedAndSuspended(String code, String accountId, boolean deleted, boolean suspended);

    ApplicationUser findFirstByEmailAndAccountId(String email, String accountId);

//    @Query(value = "SELECT * u from ApplicationUser ORDER BY u.top DESC LIMIT ?")
    List<ApplicationUser> findTop3ByAccountIdAndDeletedAndTopNotOrderByTopDesc(String accountId,boolean deleted, int top);

    ArrayList<ApplicationUser> findAllByAccountId(String accountId);
    ArrayList<ApplicationUser> findAllByAccountIdAndDeleted(String accountId, boolean deleted);

    ArrayList<ApplicationUser> findAllByAccountIdOrderByCreationDateDesc(String accountId);

    ArrayList<ApplicationUser> findAllByAccountIdOrderByCreationDateDesc(String accountId, Pageable pageable);
    @Query("{$and: [{'accountId' : {$regex: ?0 }}, {'name' : {$regex: ?1 }} ]}")
    ArrayList<ApplicationUser> findAllByAccountIdAndNameOrderByCreationDateDesc(String accountId, String name, Pageable pageable);
    @Query("{$and: [{'accountId' : {$regex: ?0 }}, {'code' : {$regex: ?1 }} ]}")
    ArrayList<ApplicationUser> findAllByAccountIdAndCodeOrderByCreationDateDesc(String accountId, String code, Pageable pageable);
    @Query("{$and: [{'accountId' : {$regex: ?0 }}, {'name' : {$regex: ?1 }, {'code' : {$regex: ?2 }} ]}")
    ArrayList<ApplicationUser> findAllByAccountIdAndNameAndCodeOrderByCreationDateDesc(String accountId, String name, String code, Pageable pageable);

    @Query("{$and: [{'accountId' : {$regex: ?0 }}, {'group._id' : ?1} ]}")
    ArrayList<ApplicationUser> findAllByAccountIdAndGroupOrderByCreationDateDesc(String accountId, String group, Pageable pageable);
    @Query("{$and: [{'accountId' : {$regex: ?0 }}, {'group._id' : ?1}, {'name' : {$regex: ?2 }} ]}")
    ArrayList<ApplicationUser> findAllByAccountIdAndGroupAndNameOrderByCreationDateDesc(String accountId, String group, String name, Pageable pageable);
    @Query("{$and: [{'accountId' : {$regex: ?0 }}, {'group._id' : ?1}, {'code' : {$regex: ?2 }} ]}")
    ArrayList<ApplicationUser> findAllByAccountIdAndGroupAndCodeOrderByCreationDateDesc(String accountId, String group, String code, Pageable pageable);
    @Query("{$and: [{'accountId' : {$regex: ?0 }}, {'group._id' : ?1}, {'name' : {$regex: ?2 }, {'code' : {$regex: ?3 }} ]}")
    ArrayList<ApplicationUser> findAllByAccountIdAndGroupAndNameAndCodeOrderByCreationDateDesc(String accountId, String group, String name, String code, Pageable pageable);


    int countAllByAccountId(String accountId);
    @CountQuery("{$and: [{'accountId' : {$regex: ?0 }}, {'name' : {$regex: ?1 }} ]}")
    int countAllByAccountIdAndName(String accountId, String name);
    @CountQuery("{$and: [{'accountId' : {$regex: ?0 }}, {'code' : {$regex: ?1 }} ]}")
    int countAllByAccountIdAndCode(String accountId, String code);
    @CountQuery("{$and: [{'accountId' : {$regex: ?0 }}, {'name' : {$regex: ?1 }, {'code' : {$regex: ?2 }} ]}")
    int countAllByAccountIdAndNameAndCode(String accountId, String name, String code);

    @CountQuery("{$and: [{'accountId' : {$regex: ?0 }}, {'group._id' : ?1} ]}")
    int countAllByAccountIdAndGroup(String accountId, String group);
    @CountQuery("{$and: [{'accountId' : {$regex: ?0 }}, {'group._id' : ?1}, {'name' : {$regex: ?2 }} ]}")
    int countAllByAccountIdAndGroupAndName(String accountId, String group, String name);
    @CountQuery("{$and: [{'accountId' : {$regex: ?0 }}, {'group._id' : ?1}, {'code' : {$regex: ?2 }} ]}")
    int countAllByAccountIdAndGroupAndCode(String accountId, String group, String code);
    @CountQuery("{$and: [{'accountId' : {$regex: ?0 }}, {'group._id' : ?1}, {'name' : {$regex: ?2 }, {'code' : {$regex: ?3 }} ]}")
    int countAllByAccountIdAndGroupAndNameAndCode(String accountId, String group, String name, String code);


    List<ApplicationUser> findAllByAccountIdAndGroupAndDeleted(String accountId, Group group, boolean deleted);

    List<ApplicationUser> findAllByAccountIdAndGroupId(String id, String groupId);

    boolean existsByEmailAndAccountId(String email, String id);
}
