package com.sun.supplierpoc.services.security;


import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.sun.supplierpoc.models.auth.OauthApproval;
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
        query.addCriteria(Criteria.where(OauthApproval.USER_ID).is(approval.getUserId()));
        query.addCriteria(Criteria.where(OauthApproval.CLIENT_ID).is(approval.getClientId()));
        query.addCriteria(Criteria.where(OauthApproval.SCOPE).is(approval.getScope()));

        Update update = new Update();
        update.set(OauthApproval.EXPIRE_AT, approval.getExpiresAt().getTime());
        update.set(OauthApproval.STATUS, approval.getStatus() != null ? approval.getStatus() : org.springframework.security.oauth2.provider.approval.Approval.ApprovalStatus.APPROVED);
        update.set(OauthApproval.LAST_MODIFIED_AT, approval.getLastUpdatedAt().getTime());

        UpdateResult writeResult = mongoTemplate.updateFirst(query, update, OauthApproval.class);
        return (writeResult.getMatchedCount() > 0);
    }

    private boolean addApproval(org.springframework.security.oauth2.provider.approval.Approval approval) {
        OauthApproval mongoOauthApproval = new OauthApproval();
        mongoOauthApproval.setUserId(approval.getUserId());
        mongoOauthApproval.setClientId(approval.getClientId());
        mongoOauthApproval.setScope(approval.getScope());
        mongoOauthApproval.setExpireAt(approval.getExpiresAt().getTime());
        mongoOauthApproval.setLastModifiedAt(approval.getLastUpdatedAt().getTime());
        mongoOauthApproval.setStatus(approval.getStatus() != null ? approval.getStatus() : org.springframework.security.oauth2.provider.approval.Approval.ApprovalStatus.APPROVED);
        try {
            mongoTemplate.save(mongoOauthApproval);
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
                query.addCriteria(Criteria.where(OauthApproval.USER_ID).is(approval.getUserId()));
                query.addCriteria(Criteria.where(OauthApproval.CLIENT_ID).is(approval.getClientId()));
                query.addCriteria(Criteria.where(OauthApproval.SCOPE).is(approval.getScope()));

                Update update = new Update();
                update.set(OauthApproval.EXPIRE_AT, System.currentTimeMillis());

                UpdateResult writeResult = mongoTemplate.updateFirst(query, update, OauthApproval.class);
                isSuccess = (writeResult.getMatchedCount() == 1);
            } else {
                Query query = new Query();
                query.addCriteria(Criteria.where(OauthApproval.USER_ID).is(approval.getUserId()));
                query.addCriteria(Criteria.where(OauthApproval.CLIENT_ID).is(approval.getClientId()));
                query.addCriteria(Criteria.where(OauthApproval.SCOPE).is(approval.getScope()));

                DeleteResult writeResult = mongoTemplate.remove(query, OauthApproval.class);
                isSuccess = (writeResult.getDeletedCount() == 1);
            }
        }
        return isSuccess;
    }


    public Collection<org.springframework.security.oauth2.provider.approval.Approval> getApprovals(String username, String clientId) {
        Collection<org.springframework.security.oauth2.provider.approval.Approval> approvals = new ArrayList<org.springframework.security.oauth2.provider.approval.Approval>();

        Query query = new Query();
        query.addCriteria(Criteria.where(OauthApproval.CLIENT_ID).is(clientId));
        query.addCriteria(Criteria.where(OauthApproval.USER_ID).is(username));

        List<OauthApproval> mongoOauthApprovals = mongoTemplate.find(query, OauthApproval.class);

        for (OauthApproval oauthApproval : mongoOauthApprovals) {
            approvals.add(new org.springframework.security.oauth2.provider.approval.Approval(oauthApproval.getUserId(), oauthApproval.getClientId(),
                    oauthApproval.getScope(), new Date(oauthApproval.getExpireAt()), oauthApproval.getStatus(),
                    new Date(oauthApproval.getLastModifiedAt())));
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
