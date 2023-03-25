package com.example.encodemessagesapp;

import android.util.Base64;

class SimInfo {
    private int id_;
    private String display_name;
    private String icc_id;
    private int sim_id;

    public SimInfo(int id_, String display_name, String icc_id, int sim_id) {
        this.id_ = id_;
        this.display_name = display_name;
        this.icc_id = icc_id;
        this.sim_id = sim_id;
    }

    public int getId_() {
        return id_;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public String getIcc_id() {
        return icc_id;
    }

    public int getSim_id() {
        return sim_id;
    }

    @Override
    public String toString() {
        return "SimInfo{" +
                "id_=" + id_ +
                ", display_name='" + display_name + '\'' +
                ", icc_id='" + icc_id + '\'' +
                ", sim_id= " + sim_id +
                '}';
    }
}

class StringXORer {

    public String encode(String s, String key) {
        return Base64.encodeToString(xorWithKey(s.getBytes(), key.getBytes()) , Base64.DEFAULT);
    }

    public String decode(String s, String key) {
        return new String(xorWithKey(Base64.decode(s , Base64.DEFAULT), key.getBytes()));
    }

    private byte[] xorWithKey(byte[] a, byte[] key) {
        byte[] out = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = (byte) (a[i] ^ key[i%key.length]);
        }
        return out;
    }
}