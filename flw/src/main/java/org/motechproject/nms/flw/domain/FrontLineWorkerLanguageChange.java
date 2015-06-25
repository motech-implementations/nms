package org.motechproject.nms.flw.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.nms.region.domain.Language;

import javax.jdo.annotations.Column;
import javax.validation.constraints.NotNull;

@Entity(tableName = "nms_front_line_worker_changes_language")
public class FrontLineWorkerLanguageChange {

    @Field
    @Column(allowsNull = "false")
    @NotNull
    private FrontLineWorker frontLineWorker;

    @Field
    private Language oldLanguage;

    @Field
    private Language newLanguage;

    public FrontLineWorkerLanguageChange() {
    }

    public FrontLineWorkerLanguageChange(Language oldLanguage, Language newLanguage) {
        this.oldLanguage = oldLanguage;
        this.newLanguage = newLanguage;
    }

    public FrontLineWorker getFrontLineWorker() {
        return frontLineWorker;
    }

    public void setFrontLineWorker(FrontLineWorker frontLineWorker) {
        this.frontLineWorker = frontLineWorker;
    }

    public Language getOldLanguage() {
        return oldLanguage;
    }

    public void setOldLanguage(Language oldLanguage) {
        this.oldLanguage = oldLanguage;
    }

    public Language getNewLanguage() {
        return newLanguage;
    }

    public void setNewLanguage(Language newLanguage) {
        this.newLanguage = newLanguage;
    }
}
