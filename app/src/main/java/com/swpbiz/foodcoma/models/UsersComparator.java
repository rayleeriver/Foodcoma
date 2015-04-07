package com.swpbiz.foodcoma.models;

import java.util.Comparator;

public class UsersComparator implements Comparator<User>{
    @Override
    public int compare(User lhs, User rhs) {
        String lname = (lhs.getName() == null)?lhs.getPhoneNumber():lhs.getName();
        String rname = (rhs.getName() == null)?rhs.getPhoneNumber():rhs.getName();
        return (rname.compareTo(lname));
    }
}
