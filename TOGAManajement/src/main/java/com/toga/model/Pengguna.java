package com.toga.model;

import org.apache.commons.lang3.StringUtils;

public class Pengguna {
    private int id;
    private String nama;
    private String alamat;

    public Pengguna(String nama, String alamat) {
        this.nama = nama;
        this.alamat = alamat;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNama() { return nama; }
    public boolean setNama(String nama) {
        if (StringUtils.isBlank(nama)) return false;
        this.nama = nama;
        return true;
    }

    public String getAlamat() { return alamat; }
    public boolean setAlamat(String alamat) {
        if (StringUtils.isBlank(alamat)) return false;
        this.alamat = alamat;
        return true;
    }

    String getInfoSingkat() { return nama + " - " + alamat; }

    public void tampilInfo() {
        System.out.println("Nama   : " + getNama());
        System.out.println("Alamat : " + getAlamat());
    }

    @Override
    public String toString() { return nama; }
}
