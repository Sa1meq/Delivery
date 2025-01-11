package com.example.delivery.model;


public class Card {
    private String id;
    private String cardNumber;
    private String cardExpiryDate;
    private int cardCVC;
    private String cardUserID;
    private boolean isMain;

    public Card() {
    }

    public Card(String id, String cardNumber, String cardExpiryDate, int cardCVC, String cardUserID, boolean isMain) {
        this.id = id;
        this.cardNumber = cardNumber;
        this.cardExpiryDate = cardExpiryDate;
        this.cardCVC = cardCVC;
        this.cardUserID = cardUserID;
        this.isMain = isMain;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardExpiryDate() {
        return cardExpiryDate;
    }

    public void setCardExpiryDate(String cardExpiryDate) {
        this.cardExpiryDate = cardExpiryDate;
    }

    public int getCardCVC() {
        return cardCVC;
    }

    public void setCardCVC(int cardCVC) {
        this.cardCVC = cardCVC;
    }

    public String getCardUserID() {
        return cardUserID;
    }

    public void setCardUserID(String cardUserID) {
        this.cardUserID = cardUserID;
    }

    public boolean isMain() {
        return isMain;
    }

    public void setMain(boolean main) {
        isMain = main;
    }
}
