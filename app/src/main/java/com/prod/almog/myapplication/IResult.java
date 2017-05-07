package com.prod.almog.myapplication;

/**
 * Created by shaul.almog on 04/04/2017.
 */

public interface IResult<T> {

    void accept(T t);
}
