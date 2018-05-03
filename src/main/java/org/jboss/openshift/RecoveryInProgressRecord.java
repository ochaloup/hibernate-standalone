package org.jboss.openshift;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class RecoveryInProgressRecord {
    @EmbeddedId RecoveryInProgressRecordId id;

    RecoveryInProgressRecord(String applicationPodName, String recoveryPodName) {
        this.id = new RecoveryInProgressRecordId()
            .setApplicationPodName(applicationPodName)
            .setRecoveryPodName(recoveryPodName);
    }

    @Override
    public String toString() {
        return String.format("application pod: %s, recovery pod: %s",
            this.id.getApplicationPodName(), this.id.getRecoveryPodName());
    }
}

@Embeddable
class RecoveryInProgressRecordId implements Serializable {
    private static final long serialVersionUID = 1L;

    private String applicationPodName;
    private String recoveryPodName;

    RecoveryInProgressRecordId setRecoveryPodName(String recoveryPodName) {
        this.recoveryPodName = recoveryPodName;
        return this;
    }
    RecoveryInProgressRecordId setApplicationPodName(String applicationPodName) {
        this.applicationPodName = applicationPodName;
        return this;
    }

    public String getApplicationPodName() {
        return applicationPodName;
    }
    public String getRecoveryPodName() {
        return recoveryPodName;
    }
}