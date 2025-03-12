package net.ramixin.util;

public interface QuadConsumer<T, K, M, N> {

    void accept(T t, K k, M m, N n);

}
