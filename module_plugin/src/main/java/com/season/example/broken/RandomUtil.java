package com.season.example.broken;

import java.util.Random;

public class RandomUtil {

    static Random random = new Random();

    static int nextInt(int a, int b){
        return Math.min(a,b) + random.nextInt(Math.abs(a - b));
    }

    static int nextInt(int a){
        return random.nextInt(a);
    }

    static float nextFloat(float a, float b){
        return Math.min(a,b) + random.nextFloat() * Math.abs(a - b);
    }

    static float nextFloat(float a){
        return random.nextFloat() * a;
    }

    static boolean nextBoolean(){
        return random.nextBoolean();
    }

}
