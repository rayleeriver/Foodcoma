package com.swpbiz.foodcoma.models;

import com.swpbiz.foodcoma.models.Invitation;

import java.util.Comparator;

public class InvitationsComparator implements Comparator<Invitation>{
    @Override
    public int compare(Invitation lhs, Invitation rhs) {
        return (int) (rhs.getTimeOfEvent() - lhs.getTimeOfEvent());
    }
}
