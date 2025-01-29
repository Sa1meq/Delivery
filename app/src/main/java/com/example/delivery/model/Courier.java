package com.example.delivery.model;

public class Courier {
    public String id;
    public String firstName;
    public String surName;
    public String middleName; // Отчество
    public String phone;
    public String balance;
    public String typeOfCourier;
    private float rating;
    private int ratingCount;
    public String countOfDayDeliveres;
    public boolean isVerified;
    private long blockedUntil; // Время окончания
    private String status;
    private int bonusPoints; // Бонусные баллы
    private String enterCode;
    private String email;

    // Новые поля для анкеты
    private String hobbies; // Хобби
    private String previousJobs; // Предыдущие места работы
    private boolean hasDrivingLicense; // Наличие водительских прав
    private String drivingLicenseCategories; // Категории водительских прав
    private String additionalInfo; // Дополнительная информация
    private String attachedFiles;
    private boolean hasExperience;

    public Courier() {
    }

    public Courier(String id, String firstName, String surName, String middleName, String phone, String balance, String typeOfCourier, float rating, int ratingCount, String countOfDayDeliveres, boolean isVerified, long blockedUntil, String status, int bonusPoints, String hobbies, String previousJobs, boolean hasDrivingLicense, String drivingLicenseCategories, String additionalInfo, String attachedFiles, boolean hasExperience, String enterCode, String email) {
        this.id = id;
        this.firstName = firstName;
        this.surName = surName;
        this.middleName = middleName;
        this.phone = phone;
        this.balance = balance;
        this.typeOfCourier = typeOfCourier;
        this.rating = rating;
        this.ratingCount = ratingCount;
        this.countOfDayDeliveres = countOfDayDeliveres;
        this.isVerified = isVerified;
        this.blockedUntil = blockedUntil;
        this.status = status;
        this.bonusPoints = bonusPoints;
        this.hobbies = hobbies;
        this.previousJobs = previousJobs;
        this.hasDrivingLicense = hasDrivingLicense;
        this.drivingLicenseCategories = drivingLicenseCategories;
        this.additionalInfo = additionalInfo;
        this.attachedFiles = attachedFiles;
        this.hasExperience = hasExperience;
        this.enterCode = enterCode;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEnterCode() {
        return enterCode;
    }

    public void setEnterCode(String enterCode) {
        this.enterCode = enterCode;
    }

    public boolean isHasExperience() {
        return hasExperience;
    }

    public void setHasExperience(boolean hasExperience) {
        this.hasExperience = hasExperience;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getHobbies() {
        return hobbies;
    }

    public void setHobbies(String hobbies) {
        this.hobbies = hobbies;
    }

    public String getPreviousJobs() {
        return previousJobs;
    }

    public void setPreviousJobs(String previousJobs) {
        this.previousJobs = previousJobs;
    }

    public boolean isHasDrivingLicense() {
        return hasDrivingLicense;
    }

    public void setHasDrivingLicense(boolean hasDrivingLicense) {
        this.hasDrivingLicense = hasDrivingLicense;
    }

    public String getDrivingLicenseCategories() {
        return drivingLicenseCategories;
    }

    public void setDrivingLicenseCategories(String drivingLicenseCategories) {
        this.drivingLicenseCategories = drivingLicenseCategories;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getAttachedFiles() {
        return attachedFiles;
    }

    public void setAttachedFiles(String attachedFiles) {
        this.attachedFiles = attachedFiles;
    }

    // Остальные геттеры и сеттеры
    public long getBlockedUntil() {
        return blockedUntil;
    }

    public void setBlockedUntil(long blockedUntil) {
        this.blockedUntil = blockedUntil;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getBonusPoints() {
        return bonusPoints;
    }

    public void setBonusPoints(int bonusPoints) {
        this.bonusPoints = bonusPoints;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getSurName() {
        return surName;
    }

    public String getPhone() {
        return phone;
    }

    public String getTypeOfCourier() {
        return typeOfCourier;
    }

    public String getCountOfDayDeliveres() {
        return countOfDayDeliveres;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public float getRating() {
        return rating;
    }

    public void setCountOfDayDeliveres(String countOfDayDeliveres) {
        this.countOfDayDeliveres = countOfDayDeliveres;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setTypeOfCourier(String typeOfCourier) {
        this.typeOfCourier = typeOfCourier;
    }
}