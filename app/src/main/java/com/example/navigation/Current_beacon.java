package com.example.navigation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Current_beacon {//DB 비콘 정보 클래스_Route document
    private ArrayList<String> inter_path = new ArrayList<String>();
    private String dest_name;
    private Map<String,String> navigation = new HashMap<>();

    public Map<String, String> getNavigation() {
        return navigation;
    }

    public void setNavigation(Map<String, String> navigation) {
        this.navigation = navigation;
    }

    public String getVoice(String key){
        return navigation.get(key);
    }

    public void setInter_path(ArrayList<String> intermediate_path) {
        this.inter_path = intermediate_path;
    }

    public ArrayList<String> getInter_path() {
        return inter_path;
    }

    public void setDest_name(String destination_name) {
        this.dest_name = destination_name;
    }

    public String getDest_name() {
        return dest_name;
    }

    /*public int getPath_length(){
        return intermediate_path.length;
    }*/

    /*public Current_beacon(){
        Arrays.fill(intermediate_path,null);
    }

    public void setIntermediate_path(String[] intermediate_path) {
        this.intermediate_path = Arrays.copyOf(intermediate_path,intermediate_path.length);
    }

     public String[] getIntermediate_path() {
        return Arrays.copyOf(intermediate_path,intermediate_path.length);
    }*/



}

