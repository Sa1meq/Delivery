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
    private int dailyCompletedOrders;
    private int totalCompletedOrders;
    public boolean isVerified;
    private long blockedUntil; // Время окончания
    private String status;
    private int bonusPoints; // Бонусные баллы
    private String enterCode;
    private String email;
    private String avatarUrl;
    private double tariffMultiplier = 1.0;
    private long tariffEndTime = 0;
    // Новые поля для анкеты
    private String previousJobs; // Предыдущие места работы
    private String drivingLicenseCategories; // Категории водительских прав
    private String additionalInfo; // Дополнительная информация
    private String attachedFiles;
    private boolean hasExperience; // Стаж более 2 лет

    public Courier() {
    }

    public Courier(String id, String firstName, String surName, String middleName, String phone, String balance, String typeOfCourier, float rating, int ratingCount, int dailyCompletedOrders, int totalCompletedOrders, boolean isVerified, long blockedUntil, String status, int bonusPoints, String enterCode, String email, String avatarUrl, String previousJobs, String drivingLicenseCategories, String attachedFiles, String additionalInfo, boolean hasExperience, double tariffMultiplier, long tariffEndTime) {
        this.id = id;
        this.firstName = firstName;
        this.surName = surName;
        this.middleName = middleName;
        this.phone = phone;
        this.balance = balance;
        this.typeOfCourier = typeOfCourier;
        this.rating = rating;
        this.ratingCount = ratingCount;
        this.totalCompletedOrders = totalCompletedOrders;
        this.dailyCompletedOrders = dailyCompletedOrders;
        this.isVerified = isVerified;
        this.blockedUntil = blockedUntil;
        this.status = status;
        this.bonusPoints = bonusPoints;
        this.enterCode = enterCode;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.previousJobs = previousJobs;
        this.drivingLicenseCategories = drivingLicenseCategories;
        this.attachedFiles = attachedFiles;
        this.additionalInfo = additionalInfo;
        this.hasExperience = hasExperience;
        this.tariffEndTime = tariffEndTime;
        this.tariffMultiplier = tariffMultiplier;
    }



    // Геттеры и сеттеры
    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getEmail() {
        return email;
    }

    public double getTariffMultiplier() {
        return tariffMultiplier;
    }

    public void setTariffMultiplier(double tariffMultiplier) {
        this.tariffMultiplier = tariffMultiplier;
    }

    public long getTariffEndTime() {
        return tariffEndTime;
    }

    public void setTariffEndTime(long tariffEndTime) {
        this.tariffEndTime = tariffEndTime;
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

    public int getDailyCompletedOrders() {
        return dailyCompletedOrders;
    }

    public void setDailyCompletedOrders(int dailyCompletedOrders) {
        this.dailyCompletedOrders = dailyCompletedOrders;
    }

    public int getTotalCompletedOrders() {
        return totalCompletedOrders;
    }

    public void setTotalCompletedOrders(int totalCompletedOrders) {
        this.totalCompletedOrders = totalCompletedOrders;
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

    public String getPreviousJobs() {
        return previousJobs;
    }

    public void setPreviousJobs(String previousJobs) {
        this.previousJobs = previousJobs;
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



    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public float getRating() {
        return rating;
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