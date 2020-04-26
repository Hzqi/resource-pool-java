package com.jackywong.pool.functional;

/**
 * Created by huangziqi on 2020/4/26
 */
public class Tuple<A,B> {
    private A a;
    private B b;

    public Tuple(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public static <A,B> Tuple<A,B> of(A a, B b) {
        return new Tuple<>(a,b);
    }

    public A getA() {
        return a;
    }

    public void setA(A a) {
        this.a = a;
    }

    public B getB() {
        return b;
    }

    public void setB(B b) {
        this.b = b;
    }
}
