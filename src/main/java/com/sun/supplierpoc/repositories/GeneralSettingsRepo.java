package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.GeneralSettings;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GeneralSettingsRepo  extends MongoRepository<GeneralSettings, String> {
    GeneralSettings findByAccountIdAndDeleted(String accountId, boolean deleted);
}
