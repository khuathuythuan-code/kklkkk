package org.example.bai6;

import java.util.Date;

public class DonHang {
    private int id;
    private String tenHang;
    private int soLuong;
    private float donGia;
    private float triGia;
    private float thue;
    private float cuocChuyenCho;
    private float tongCong;
    private Date createdAt;
    private Date updatedAt;

    public DonHang() {
    }

    public DonHang(int id, String tenHang, int soLuong, float donGia) {
        this.id = id;
        this.tenHang = tenHang;
        this.soLuong = soLuong;
        this.donGia = donGia;
        tinhtoan(false);
    }

    public DonHang(String tenHang, int soLuong, float donGia) {
        this.tenHang = tenHang;
        this.soLuong = soLuong;
        this.donGia = donGia;
        tinhtoan(true);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTenHang() {
        return tenHang;
    }

    public void setTenHang(String tenHang) {
        this.tenHang = tenHang;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public float getDonGia() {
        return donGia;
    }

    public void setDonGia(float donGia) {
        this.donGia = donGia;
    }

    public float getTriGia() {
        return triGia;
    }

    public void setTriGia(float triGia) {
        this.triGia = triGia;
    }

    public float getThue() {
        return thue;
    }

    public void setThue(float thue) {
        this.thue = thue;
    }

    public float getCuocChuyenCho() {
        return cuocChuyenCho;
    }

    public void setCuocChuyenCho(float cuocChuyenCho) {
        this.cuocChuyenCho = cuocChuyenCho;
    }

    public float getTongCong() {
        return tongCong;
    }

    public void setTongCong(float tongCong) {
        this.tongCong = tongCong;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }


    public void tinhtoan(boolean isCreated){
        triGia = soLuong * donGia;
        thue = triGia * 0.05f;
        cuocChuyenCho = soLuong * 1500;
        tongCong = triGia + thue + cuocChuyenCho;
        if (isCreated){
            createdAt = new Date();
        }
        updatedAt = new Date();
    }
}
