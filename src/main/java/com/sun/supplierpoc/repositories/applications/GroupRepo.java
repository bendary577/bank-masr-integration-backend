package com.sun.supplierpoc.repositories.applications;

import com.sun.supplierpoc.models.applications.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepo extends MongoRepository<Group, String> {
    ArrayList<Group> findAllByAccountId(String accountId);
    ArrayList<Group> findAllByAccountIdAndDeleted(String accountId, boolean deleted);
    List<Group> findTop3ByAccountIdAndDeletedAndTopNotOrderByTopDesc(String accountId, boolean deleted, int top);
    List<Group> findAllByAccountIdAndDeletedAndTopNotOrderByTopDesc(String accountId, boolean deleted, int top);
    ArrayList<Group> findAllByAccountIdAndParentGroupId(String id, String group);
    ArrayList<Group> findAllByAccountIdAndParentGroupIdAndDeleted(String id, String group, boolean deleted);
    Optional<Group> findByNameAndAccountId(String name, String accountId);
    Optional<Group> findByIdAndAccountId(String group, String id);

    Optional<Group> findByAccountIdAndDeletedAndName(String id, boolean b, String generic);
}
