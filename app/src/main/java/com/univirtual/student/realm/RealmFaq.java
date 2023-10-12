package com.univirtual.student.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by 2CLearning on 12/16/2017.
 */

public class RealmFaq extends RealmObject {

    private int id;
    @PrimaryKey
    private String faqid;
    private String title;
    private String description;

    public RealmFaq() {

    }

    public RealmFaq(String faqid, String title, String description) {
        this.faqid = faqid;
        this.title = title;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFaqid() {
        return faqid;
    }

    public void setFaqid(String faqid) {
        this.faqid = faqid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
