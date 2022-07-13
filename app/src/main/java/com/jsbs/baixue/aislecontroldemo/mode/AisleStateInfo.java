package com.jsbs.baixue.aislecontroldemo.mode;

public class AisleStateInfo {
    private long cabinetId;//货柜号 //container number
    private String aisleNum;//货道编号 //Cargo Lane Number
    private int aisleState;//货道状态 //Cargo Lane Status

    public long getCabinetId() {
        return cabinetId;
    }

    public void setCabinetId(long cabinetId) {
        this.cabinetId = cabinetId;
    }

    public String getAisleNum() {
        return aisleNum;
    }

    public void setAisleNum(String aisleNum) {
        this.aisleNum = aisleNum;
    }

    public int getAisleState() {
        return aisleState;
    }

    public void setAisleState(int aisleState) {
        this.aisleState = aisleState;
    }
}
