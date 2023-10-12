package com.univirtual.student.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by 2CLearning on 12/16/2017.
 */

public class RealmPayment extends RealmObject {

    private int id;
    @PrimaryKey
    private String paymentid;
    private String msisdn;
    private String countrycode;
    private String network;
    private String currency;
    private String amount;
    private String appuserdescription;
    private String description;
    private String duration;
    private String paymentref;
    private String externalreferenceno;
    private String message;
    private String status;
    private String transactionstatusreason;
    private String appuserfeeexpirydate;
    private String expirydate;
    private String institutionfeeexpirydate;
    private String payerid;
    private String enrolmentid;
    private String institutionid;
    private String feetype;
    private boolean expired;
    private boolean appuserfeeexpired;
    private boolean institutionfeeexpired;
    private String created_at;
    private String updated_at;


    private String coursepath;
    private String institutionname;

    public RealmPayment() {

    }

    public RealmPayment(String paymentid, String msisdn, String countrycode, String network, String currency, String amount, String appuserdescription, String description, String duration, String paymentref, String externalreferenceno, String message, String status, String transactionstatusreaso, String appuserfeeexpirydate, String expirydate, String institutionfeeexpirydate, String payerid, String enrolmentid, String institutionid, String feetype, boolean expired, boolean appuserfeeexpired, boolean institutionfeeexpired, String created_at, String updated_at) {
        this.paymentid = paymentid;
        this.msisdn = msisdn;
        this.countrycode = countrycode;
        this.network = network;
        this.currency = currency;
        this.amount = amount;
        this.appuserdescription = appuserdescription;
        this.description = description;
        this.duration = duration;
        this.paymentref = paymentref;
        this.externalreferenceno = externalreferenceno;
        this.message = message;
        this.status = status;
        this.transactionstatusreason = transactionstatusreaso;
        this.appuserfeeexpirydate = appuserfeeexpirydate;
        this.expirydate = expirydate;
        this.institutionfeeexpirydate = institutionfeeexpirydate;
        this.payerid = payerid;
        this.enrolmentid = enrolmentid;
        this.institutionid = institutionid;
        this.feetype = feetype;
        this.expired = expired;
        this.appuserfeeexpired = appuserfeeexpired;
        this.institutionfeeexpired = institutionfeeexpired;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPaymentid() {
        return paymentid;
    }

    public void setPaymentid(String paymentid) {
        this.paymentid = paymentid;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getCountrycode() {
        return countrycode;
    }

    public void setCountrycode(String countrycode) {
        this.countrycode = countrycode;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAppuserdescription() {
        return appuserdescription;
    }

    public void setAppuserdescription(String appuserdescription) {
        this.appuserdescription = appuserdescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getPaymentref() {
        return paymentref;
    }

    public void setPaymentref(String paymentref) {
        this.paymentref = paymentref;
    }

    public String getExternalreferenceno() {
        return externalreferenceno;
    }

    public void setExternalreferenceno(String externalreferenceno) {
        this.externalreferenceno = externalreferenceno;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransactionstatusreason() {
        return transactionstatusreason;
    }

    public void setTransactionstatusreason(String transactionstatusreason) {
        this.transactionstatusreason = transactionstatusreason;
    }

    public String getAppuserfeeexpirydate() {
        return appuserfeeexpirydate;
    }

    public void setAppuserfeeexpirydate(String appuserfeeexpirydate) {
        this.appuserfeeexpirydate = appuserfeeexpirydate;
    }

    public String getExpirydate() {
        return expirydate;
    }

    public void setExpirydate(String expirydate) {
        this.expirydate = expirydate;
    }

    public String getInstitutionfeeexpirydate() {
        return institutionfeeexpirydate;
    }

    public void setInstitutionfeeexpirydate(String institutionfeeexpirydate) {
        this.institutionfeeexpirydate = institutionfeeexpirydate;
    }

    public String getPayerid() {
        return payerid;
    }

    public void setPayerid(String payerid) {
        this.payerid = payerid;
    }

    public String getEnrolmentid() {
        return enrolmentid;
    }

    public void setEnrolmentid(String enrolmentid) {
        this.enrolmentid = enrolmentid;
    }

    public String getInstitutionid() {
        return institutionid;
    }

    public void setInstitutionid(String institutionid) {
        this.institutionid = institutionid;
    }

    public String getFeetype() {
        return feetype;
    }

    public void setFeetype(String feetype) {
        this.feetype = feetype;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public boolean isAppuserfeeexpired() {
        return appuserfeeexpired;
    }

    public void setAppuserfeeexpired(boolean appuserfeeexpired) {
        this.appuserfeeexpired = appuserfeeexpired;
    }

    public boolean isInstitutionfeeexpired() {
        return institutionfeeexpired;
    }

    public void setInstitutionfeeexpired(boolean institutionfeeexpired) {
        this.institutionfeeexpired = institutionfeeexpired;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getCoursepath() {
        return coursepath;
    }

    public void setCoursepath(String coursepath) {
        this.coursepath = coursepath;
    }

    public String getInstitutionname() {
        return institutionname;
    }

    public void setInstitutionname(String institutionname) {
        this.institutionname = institutionname;
    }
}
