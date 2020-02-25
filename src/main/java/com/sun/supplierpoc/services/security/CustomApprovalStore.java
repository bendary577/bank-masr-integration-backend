package com.sun.supplierpoc.services.security;


import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.sun.supplierpoc.models.auth.Approval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;

import java.util.*;

/**
 * Created by jeebb on 11/10/14.
 */
public class CustomApprovalStore implements ApprovalStore {

    @Autowired
    private MongoTemplate mongoTemplate;

    private boolean handleRevocationsAsExpiry = false;


    public boolean addApprovals(Collection<org.springframework.security.oauth2.provider.approval.Approval> approvals) {
        boolean isSuccess = true;
        Iterator<org.springframework.security.oauth2.provider.approval.Approval> iterator = approvals.iterator();
        while (iterator.hasNext()) {
            org.springframework.security.oauth2.provider.approval.Approval approval = iterator.next();
            if (!updateApproval(approval) && !addApproval(approval)) {
                isSuccess = false;
            }
        }
        return isSuccess;
    }

    private boolean updateApproval(org.springframework.security.oauth2.provider.approval.Approval approval) {
        Query query = new Query();
        query.addCriteria(Criteria.where(Approval.USER_ID).is(approval.getUserId()));
        query.addCriteria(Criteria.where(Approval.CLIENT_ID).is(approval.getClientId()));
        query.addCriteria(Criteria.where(Approval.SCOPE).is(approval.getScope()));

        Update update = new Update();
        update.set(Approval.EXPIRE_AT, approval.getExpiresAt().getTime());
        update.set(Approval.STATUS, approval.getStatus() != null ? approval.getStatus() : org.springframework.security.oauth2.provider.approval.Approval.ApprovalStatus.APPROVED);
        update.set(Approval.LAST_MODIFIED_AT, approval.getLastUpdatedAt().getTime());

        UpdateResult writeResult = mongoTemplate.updateFirst(query, update, Approval.class);
        return (writeResult.getMatchedCount() > 0);
    }

    private boolean addApproval(org.springframework.security.oauth2.provider.approval.Approval approval) {
        Approval mongoApproval = new Approval();
        mongoApproval.setUserId(approval.getUserId());
        mongoApproval.setClientId(approval.getClientId());
        mongoApproval.setScope(approval.getScope());
        mongoApproval.setExpireAt(approval.getExpiresAt().getTime());
        mongoApproval.setLastModifiedAt(approval.getLastUpdatedAt().getTime());
        mongoApproval.setStatus(approval.getStatus() != null ? approval.getStatus() : org.springframework.security.oauth2.provider.approval.Approval.ApprovalStatus.APPROVED);
        try {
            mongoTemplate.save(mongoApproval);
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    public boolean revokeApprovals(Collection<org.springframework.security.oauth2.provider.approval.Approval> approvals) {
        boolean isSuccess = true;
        Iterator<org.springframework.security.oauth2.provider.approval.Approval> iterator = approvals.iterator();

        while (iterator.hasNext()) {
            org.springframework.security.oauth2.provider.approval.Approval approval = iterator.next();
            if (handleRevocationsAsExpiry) {
                Query query = new Query();
                query.addCriteria(Criteria.where(Approval.USER_ID).is(approval.getUserId()));
                query.addCriteria(Criteria.where(Approval.CLIENT_ID).is(approval.getClientId()));
                query.addCriteria(Criteria.where(Approval.SCOPE).is(approval.getScope()));

                Update update = new Update();
                update.set(Approval.EXPIRE_AT, System.currentTimeMillis());

                UpdateResult writeResult = mongoTemplate.updateFirst(query, update, Approval.class);
                isSuccess = (writeResult.getMatchedCount() == 1);
            } else {
                Query query = new Query();
                query.addCriteria(Criteria.where(Approval.USER_ID).is(approval.getUserId()));
                query.addCriteria(Criteria.where(Approval.CLIENT_ID).is(approval.getClientId()));
                query.addCriteria(Criteria.where(Approval.SCOPE).is(approval.getScope()));

                DeleteResult writeResult = mongoTemplate.remove(query, Approval.class);
                isSuccess = (writeResult.getDeletedCount() == 1);
            }
        }
        return isSuccess;
    }


    public Collection<org.springframework.security.oauth2.provider.approval.Approval> getApprovals(String username, String clientId) {
        Collection<org.springframework.security.oauth2.provider.approval.Approval> approvals = new ArrayList<org.springframework.security.oauth2.provider.approval.Approval>();

        Query query = new Query();
        query.addCriteria(Criteria.where(Approval.CLIENT_ID).is(clientId));
        query.addCriteria(Criteria.where(Approval.USER_ID).is(username));

        List<Approval> mongoApprovals = mongoTemplate.find(query, Approval.class);

        for (Approval approval : mongoApprovals) {
            approvals.add(new org.springframework.security.oauth2.provider.approval.Approval(approval.getUserId(), approval.getClientId(),
                    approval.getScope(), new Date(approval.getExpireAt()), approval.getStatus(),
                    new Date(approval.getLastModifiedAt())));
        }

        return approvals;
    }

    public boolean isHandleRevocationsAsExpiry() {
        return handleRevocationsAsExpiry;
    }

    public void setHandleRevocationsAsExpiry(boolean handleRevocationsAsExpiry) {
        this.handleRevocationsAsExpiry = handleRevocationsAsExpiry;
    }
}
