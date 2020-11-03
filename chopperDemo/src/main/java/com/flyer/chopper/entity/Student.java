package com.flyer.chopper.entity;

/**
 * create by huirong on 2020-10-06 11:21
 */

public class Student {
    private int age;

    private String name;

    private double score;

    public Student(){

    }


    public Student(int age, String name, double score){
        this.age=age;
        this.name=name;
        this.score=score;
    }



    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
